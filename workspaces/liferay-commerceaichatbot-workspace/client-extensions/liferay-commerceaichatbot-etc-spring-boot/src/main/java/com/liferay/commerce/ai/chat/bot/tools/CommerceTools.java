/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.adk.tools.Annotations;

import com.liferay.commerce.ai.chat.bot.constants.CommerceOrderConstants;
import com.liferay.commerce.ai.chat.bot.model.Account;
import com.liferay.commerce.ai.chat.bot.model.AccountBrief;
import com.liferay.commerce.ai.chat.bot.model.Order;
import com.liferay.commerce.ai.chat.bot.model.OrderItem;
import com.liferay.commerce.ai.chat.bot.model.PageResult;
import com.liferay.commerce.ai.chat.bot.model.Settings;
import com.liferay.commerce.ai.chat.bot.model.Summary;
import com.liferay.commerce.ai.chat.bot.model.UserAccount;
import com.liferay.commerce.ai.chat.bot.service.CommerceService;
import com.liferay.commerce.ai.chat.bot.service.SettingsService;
import com.liferay.commerce.ai.chat.bot.util.SecurityUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * @author Ivica Cardic
 */
@Service
public class CommerceTools {

	public CommerceTools(
		CommerceService commerceService, ObjectMapper objectMapper,
		SettingsService settingsService) {

		_commerceService = commerceService;
		_objectMapper = objectMapper;
		_settingsService = settingsService;
	}

	@Annotations.Schema(
		description = "Finds an order by identifier", name = "findOrderTool"
	)
	public Map<String, Object> findOrderTool(
		@Annotations.Schema(
			description = "the order id or external reference code",
			name = "identifier"
		)
		String identifier) {

		try {
			Order order = _getOrder(identifier);

			if (order != null) {
				return Map.of("order", order);
			}

			return Map.of(
				"error",
				"I could not find an order with identifier '" + identifier +
					"'. Please check the identifier and try again.");
		}
		catch (Exception exception) {
			return Map.of(
				"error", "Error finding order: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves a summary of orders for all available accounts",
		name = "getAllAccountsOrderSummaryTool"
	)
	public Map<String, Object> getAllAccountsOrderSummaryTool() {
		try {
			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				SecurityUtils.getEmail());

			List<Account> accounts = userAccount.getAccounts();

			if (accounts.isEmpty()) {
				return Map.of("error", "No accounts available.");
			}

			List<Summary> accountSummaries = new ArrayList<>();
			int totalOrders = 0;

			long channelId = _getChannelId();

			for (Account account : accounts) {
				try {
					List<Order> orders =
						_commerceService.getAllPlacedOrdersByAccount(
							channelId, account.getId(), null, "", null);

					totalOrders += orders.size();

					Summary summary = new Summary();

					summary.setId(account.getId());
					summary.setName(
						(account.getName() != null) ? account.getName() :
							"Unknown Account");
					summary.setType(
						(account.getType() != null) ? account.getType() :
							"N/A");
					summary.setOrderCount(orders.size());

					accountSummaries.add(summary);
				}
				catch (Exception exception) {
					Summary summary = new Summary();

					summary.setId(account.getId());
					summary.setName(
						(account.getName() != null) ? account.getName() :
							"Unknown Account");
					summary.setType("N/A");
					summary.setType(
						(account.getType() != null) ? account.getType() :
							"N/A");
					summary.setOrderCount(0);
					summary.setError(exception.getMessage());

					accountSummaries.add(summary);
				}
			}

			accountSummaries.sort(
				(a, b) -> Integer.compare(
					b.getOrderCount(), a.getOrderCount()));

			return Map.of(
				"accountSummaries", accountSummaries, "channelId", channelId,
				"totalOrders", totalOrders);
		}
		catch (Exception exception) {
			return Map.of(
				"error", _getAllAccountsOrderSummaryErrorMessage(exception));
		}
	}

	@Annotations.Schema(
		description = "Retrieves the current date and year",
		name = "getCurrentDateTool"
	)
	public Map<String, Object> getCurrentDateTool() {
		LocalDate localDate = LocalDate.now();

		return Map.of(
			"current_year", String.valueOf(localDate.getYear()), "full_date",
			localDate.toString());
	}

	@Annotations.Schema(
		description = "Retrieves orders for a specific customer account",
		name = "getCustomerOrdersByAccountTool"
	)
	public Map<String, Object> getCustomerOrdersByAccountTool(
		@Annotations.Schema(
			description = "the name of the account", name = "accountName"
		)
		String accountName,
		@Annotations.Schema(
			description = "whether to sort the orders in ascending order",
			name = "asc"
		)
		boolean asc) {

		try {
			long channelId = _getChannelId();

			Account account = _fetchAccount(channelId, accountName);

			if (account == null) {
				return Map.of("error", _getAccountNotFoundMessage(accountName));
			}

			List<Order> orders;

			try {
				String orderBy = "desc";

				if (asc) {
					orderBy = "asc";
				}

				orders = _commerceService.getAllPlacedOrdersByAccount(
					channelId, account.getId(), null, "createDate:" + orderBy,
					null);
			}
			catch (Exception exception) {
				return Map.of(
					"error",
					new StringBuilder(
					).append(
						"**Error retrieving orders for "
					).append(
						accountName
					).append(
						"**: "
					).append(
						exception.getMessage()
					).toString());
			}

			if (orders.isEmpty()) {
				return Map.of(
					"error",
					new StringBuilder(
					).append(
						"I found the account for '"
					).append(
						accountName
					).append(
						"' but no orders are available."
					).toString());
			}

			return Map.of("account", account, "orders", orders);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error getting customer orders: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves orders for a customer",
		name = "getCustomerOrdersTool"
	)
	public Map<String, Object> getCustomerOrdersTool(
		@Annotations.Schema(
			description = "whether to sort the orders in ascending order",
			name = "asc"
		)
		boolean asc) {

		try {
			String email = SecurityUtils.getEmail();

			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				email);

			if (userAccount == null) {
				return Map.of("error", _getUserAccountNotFoundMessage(email));
			}

			long channelId = _getChannelId();

			List<Order> orders = new ArrayList<>();

			try {
				String orderBy = "desc";

				if (asc) {
					orderBy = "asc";
				}

				for (AccountBrief accountBrief :
						userAccount.getAccountBriefs()) {

					orders.addAll(
						_commerceService.getAllPlacedOrdersByAccount(
							channelId, accountBrief.getId(), null,
							"createDate:" + orderBy, null));
				}
			}
			catch (Exception exception) {
				return Map.of(
					"error",
					new StringBuilder(
					).append(
						"**Error retrieving orders for "
					).append(
						email
					).append(
						"**: "
					).append(
						String.valueOf(exception.getMessage())
					).toString());
			}

			if (orders.isEmpty()) {
				return Map.of(
					"error",
					new StringBuilder(
					).append(
						"I found the account for '"
					).append(
						email
					).append(
						"' but no orders are available."
					).toString());
			}

			return Map.of("orders", orders, "userAccount", userAccount);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error getting customer orders: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves information from the Frequently Asked Questions (FAQ) based on a query. Use this tool for any general inquiries about orders, shipping, returns, account management, and other non-order specific questions.",
		name = "getFaqInformationTool"
	)
	public Map<String, Object> getFaqInformationTool(
		@Annotations.Schema(
			description = "the search query for the FAQ (e.g., 'return policy'). Use 'all' or empty string to see all available topics.",
			name = "query"
		)
		String query) {

		Map<String, Map<String, String>> faqData = _getFaqData();

		try {
			if (StringUtils.isBlank(query) || Strings.CI.equals(query, "all") ||
				Strings.CI.equals(query, "faq") ||
				Strings.CI.equals(query, "general")) {

				return Map.of("response", _getNullQueryMessage(faqData));
			}

			String lowerCasedQuery = StringUtils.lowerCase(query);

			List<Map<String, String>> matchingAnswers = new ArrayList<>();

			for (Map.Entry<String, Map<String, String>> entry :
					faqData.entrySet()) {

				String categoryKey = entry.getKey();

				String categoryTitle = _formatTitle(
					Strings.CS.replace(categoryKey, "_", " "));

				Map<String, String> categoriesMap = entry.getValue();

				for (Map.Entry<String, String> question :
						categoriesMap.entrySet()) {

					String questionText = question.getKey();
					String answerText = question.getValue();

					String lowerCasedQuestionText = StringUtils.lowerCase(
						questionText);

					boolean questionMatches = Strings.CS.contains(
						lowerCasedQuestionText, lowerCasedQuery);

					boolean answerMatches = false;

					String[] words = StringUtils.split(lowerCasedQuery);

					for (String word : words) {
						String lowerCasedAnswerText = StringUtils.lowerCase(
							answerText);

						if ((StringUtils.length(word) > 3) &&
							Strings.CS.contains(lowerCasedAnswerText, word)) {

							answerMatches = true;

							break;
						}
					}

					if (questionMatches || answerMatches) {
						matchingAnswers.add(
							Map.of(
								"question", questionText, "answer", answerText,
								"category", categoryTitle));
					}
				}
			}

			if (matchingAnswers.isEmpty()) {
				return Map.of("error", _getFaqErrorMessage(query));
			}

			return Map.of(
				"response", _getFaqResponseMessage(query, matchingAnswers));
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"**Error retrieving FAQ information**: " +
					exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves the items contained in a specific order",
		name = "getOrderItemsTool"
	)
	public Map<String, Object> getOrderItemsTool(
		@Annotations.Schema(
			description = "the order id or external reference code",
			name = "identifier"
		)
		String identifier) {

		try {
			Order order = _getOrder(identifier);

			if (order == null) {
				return Map.of(
					"error",
					"Order " + identifier + " not found or not accessible.");
			}

			return Map.of(
				"order", order, "orderItems",
				_commerceService.getPlacedOrderItems(order.getId()),
				"shipments",
				_commerceService.getOrderShipmentsDto(order.getId()));
		}
		catch (Exception exception) {
			return Map.of(
				"error", _getOrderItemsErrorMessage(exception, identifier));
		}
	}

	@Annotations.Schema(
		description = "Retrieves shipping and tracking information for a specific order",
		name = "getOrderShippingTool"
	)
	public Map<String, Object> getOrderShippingTool(
		@Annotations.Schema(
			description = "the order id or external reference code",
			name = "identifier"
		)
		String identifier) {

		try {
			Order order = _getOrder(identifier);

			if (order == null) {
				return Map.of(
					"error",
					"Order " + identifier + " not found or not accessible.");
			}

			return Map.of(
				"order", order, "shipments",
				_commerceService.getOrderShipmentsDto(order.getId()));
		}
		catch (Exception exception) {
			return Map.of(
				"error", _getOrderShippingErrorMessage(exception, identifier));
		}
	}

	@Annotations.Schema(
		description = "Lists the configured commerce channel and its associated accounts",
		name = "listAvailableChannelsAndAccountsTool"
	)
	public Map<String, Object> listAvailableChannelsAndAccountsTool() {
		try {
			long channelId = _getChannelId();

			return Map.of(
				"accounts", _getAccounts(channelId, null), "channelId",
				channelId);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"**Error listing resources**: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Searches for orders within a specific date range for a specific account",
		name = "searchAccountOrdersByDateRangeTool"
	)
	public Map<String, Object> searchAccountOrdersByDateRangeTool(
		@Annotations.Schema(
			description = "the start date of the range (YYYY-MM-DD)",
			name = "startDate"
		)
		String startDate,
		@Annotations.Schema(
			description = "the end date of the range (YYYY-MM-DD)",
			name = "endDate"
		)
		String endDate,
		@Annotations.Schema(
			description = "the name of the account", name = "accountName"
		)
		String accountName) {

		try {
			long channelId = _getChannelId();

			Account account = _fetchAccount(channelId, accountName);

			if (account == null) {
				return Map.of("error", _getAccountNotFoundMessage(accountName));
			}

			return Map.of(
				"orders",
				_getOrdersByDateRangeMap(
					account.getId(), channelId, _getEndOffsetDateTime(endDate),
					null, _getOffsetDateTime(startDate)));
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error searching orders by date: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Searches for orders within a specific date range for a customer",
		name = "searchOrdersByDateRangeTool"
	)
	public Map<String, Object> searchOrdersByDateRangeTool(
		@Annotations.Schema(
			description = "the start date of the range (YYYY-MM-DD)",
			name = "startDate"
		)
		String startDate,
		@Annotations.Schema(
			description = "the end date of the range (YYYY-MM-DD)",
			name = "endDate"
		)
		String endDate) {

		try {
			String email = SecurityUtils.getEmail();

			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				email);

			if (userAccount == null) {
				return Map.of("error", _getUserAccountNotFoundMessage(email));
			}

			List<AccountBrief> accountBriefs = userAccount.getAccountBriefs();

			if (accountBriefs.isEmpty()) {
				return Map.of(
					"error", "No accounts found for the given email.");
			}

			long channelId = _getChannelId();
			List<Order> orders = new ArrayList<>();

			for (AccountBrief accountBrief : accountBriefs) {
				Long accountId = accountBrief.getId();

				orders.addAll(
					_getOrdersByDateRangeMap(
						accountId, channelId, _getEndOffsetDateTime(endDate),
						null, _getOffsetDateTime(startDate)));
			}

			return Map.of("orders", orders);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error searching orders by date: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Searches for orders containing a specific product for a customer",
		name = "searchOrdersByProductTool"
	)
	public Map<String, Object> searchOrdersByProductTool(
		@Annotations.Schema(
			description = "the name of the account", name = "accountName"
		)
		String accountName,
		@Annotations.Schema(
			description = "a description or name of the product",
			name = "productDescription"
		)
		String productDescription) {

		try {
			List<Order> orders = new ArrayList<>();

			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				SecurityUtils.getEmail());

			if (userAccount == null) {
				return Map.of("orders", orders);
			}

			long channelId = _getChannelId();

			for (AccountBrief accountBrief : userAccount.getAccountBriefs()) {
				orders.addAll(
					_getOrders(
						accountBrief.getId(), channelId, productDescription,
						"orderDate:desc", null));
			}

			if (orders.isEmpty()) {
				return Map.of("error", "No orders found");
			}

			List<AbstractMap.SimpleEntry<Order, List<OrderItem>>> matches =
				new ArrayList<>();

			for (Order order : orders) {
				try {
					long orderId = order.getId();

					List<OrderItem> orderItems =
						_commerceService.getPlacedOrderItems(orderId);

					List<OrderItem> matchedOrderItems = new ArrayList<>();

					for (OrderItem orderItem : orderItems) {
						String name = orderItem.getName();

						if (name != null) {
							name = StringUtils.lowerCase(name);
						}
						else {
							name = "";
						}

						String sku = orderItem.getSku();

						if (sku != null) {
							sku = StringUtils.lowerCase(sku);
						}
						else {
							sku = "";
						}

						String term = StringUtils.lowerCase(productDescription);

						if (StringUtils.isEmpty(term)) {
							term = "";
						}

						if (Strings.CS.contains(name, term) ||
							Strings.CS.contains(sku, term) ||
							Strings.CS.contains(term, name)) {

							matchedOrderItems.add(orderItem);
						}
					}

					if (!matchedOrderItems.isEmpty()) {
						matches.add(
							new AbstractMap.SimpleEntry<>(
								order, matchedOrderItems));
					}
				}
				catch (Exception exception) {
					if (_log.isInfoEnabled()) {
						_log.info(exception);
					}
				}
			}

			if (matches.isEmpty()) {
				return Map.of(
					"error", _getNoOrdersByProductMessage(productDescription));
			}

			return Map.of("orders", matches);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error searching orders by product: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Searches for orders by shipping address for a customer",
		name = "searchOrdersByShippingAddressTool"
	)
	public Map<String, Object> searchOrdersByShippingAddressTool(
		@Annotations.Schema(
			description = "the address or part of the address to search for",
			name = "addressQuery"
		)
		String addressQuery) {

		try {
			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				SecurityUtils.getEmail());

			if (userAccount == null) {
				return Map.of("orders", Map.of());
			}

			List<Order> orders = new ArrayList<>();

			List<Long> accountIds = new ArrayList<>();

			for (AccountBrief accountBrief : userAccount.getAccountBriefs()) {
				accountIds.add(accountBrief.getId());
			}

			long channelId = _getChannelId();

			for (long accountId : accountIds) {
				int page = 1;
				int pageSize = 50;

				while (true) {
					PageResult<Order> pageResult =
						_commerceService.getPlacedOrdersByAccount(
							channelId, accountId, page, pageSize, null,
							"createDate:desc", null,
							"placedOrderShippingAddress");

					if (pageResult == null) {
						break;
					}

					List<Order> pageResultOrders = pageResult.getItems();

					if ((pageResult.getItems() == null) ||
						pageResultOrders.isEmpty()) {

						break;
					}

					orders.addAll(pageResult.getItems());

					if (page >= pageResult.getLastPage()) {
						break;
					}

					page++;
				}
			}

			if (orders.isEmpty()) {
				return Map.of("error", "No orders found");
			}

			List<Order> orderList = new ArrayList<>();

			for (Order order : orders) {
				if (_isMatch(order.getShippingAddress(), addressQuery)) {
					orderList.add(order);
				}
			}

			if (orderList.isEmpty()) {
				return Map.of(
					"error",
					_getNoOrdersByShippingAddressMessage(addressQuery));
			}

			return Map.of("orderList", orderList);
		}
		catch (Exception exception) {
			return Map.of(
				"error", "Error retrieving orders: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Searches for orders by their status for a customer",
		name = "searchOrdersByStatusTool"
	)
	public Map<String, Object> searchOrdersByStatusTool(
		@Annotations.Schema(
			description = "The order status must be exactly one of: [awaiting pickup, cancelled, completed, declined, disputed, in progress, on hold, open, partially refunded, partially shipped, pending, processing, quote processed, quote requested, refunded, shipped, subscription]",
			name = "orderStatus"
		)
		String orderStatus) {

		try {
			List<Order> orders = new ArrayList<>();

			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				SecurityUtils.getEmail());

			if (userAccount == null) {
				return Map.of("orders", orders);
			}

			long channelId = _getChannelId();
			Map<String, Integer> statusMap =
				CommerceOrderConstants.getOrderStatusMap();

			for (AccountBrief accountBrief : userAccount.getAccountBriefs()) {
				orders.addAll(
					_commerceService.getAllPlacedOrdersByAccount(
						channelId, accountBrief.getId(), null,
						"createDate:desc",
						"(orderStatus/any(x:(x eq " +
							statusMap.get(orderStatus) + ")))"));
			}

			if (orders.isEmpty()) {
				return Map.of("error", "No orders found");
			}

			return Map.of("orders", orders);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error searching orders by status: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "General search for orders based on a query. It can handle order IDs, date ranges, and product searches by routing to more specific tools.",
		name = "searchOrdersTool"
	)
	public Map<String, Object> searchOrdersTool(
		@Annotations.Schema(description = "the search query", name = "query")
			String query) {

		try {
			String queryLower = "";

			if (query != null) {
				queryLower = StringUtils.lowerCase(query);
			}

			Matcher matcher = _orderIdPattern.matcher("");

			if (query != null) {
				matcher = _orderIdPattern.matcher(query);
			}

			if (matcher.find()) {
				String orderId = matcher.group();

				return findOrderTool(orderId);
			}

			if (Strings.CS.contains(queryLower, "from") ||
				Strings.CS.contains(queryLower, "between") ||
				Strings.CS.contains(queryLower, "since") ||
				Strings.CS.contains(queryLower, "after") ||
				Strings.CS.contains(queryLower, "before") ||
				Strings.CS.contains(queryLower, "date range") ||
				Strings.CS.contains(queryLower, "last ")) {

				String doc =
					"Search for orders within a specific date range for a " +
						"specific user";

				StringBuilder sb = new StringBuilder();

				sb.append("**Fast Route: Date Range Search**");
				sb.append("\n\n");
				sb.append(
					"I detected a date-related search. Let me route this ");
				sb.append(
					"to the optimized date range search tool for better ");
				sb.append("performance.");
				sb.append("\n\n");
				sb.append(doc);
				sb.append("\n\n");
				sb.append("**Please provide your email address to continue:**");
				sb.append(
					"\n- \"Search my orders from 2024-01-01 to 2024-01-31 ");
				sb.append("using your.email@example.com\"");
				sb.append("\n- \"Find orders ");
				sb.append("since 2024-01-01 for customer@company.com\"");

				return Map.of("search", sb.toString());
			}

			if (Strings.CS.contains(queryLower, "product") ||
				Strings.CS.contains(queryLower, "item") ||
				Strings.CS.contains(queryLower, "contains") ||
				Strings.CS.contains(queryLower, "battery") ||
				Strings.CS.contains(queryLower, "tire") ||
				Strings.CS.contains(queryLower, "brake") ||
				Strings.CS.contains(queryLower, "oil")) {

				StringBuilder sb = new StringBuilder();

				sb.append("**Slow Route: Product Search**");
				sb.append("\n\n");
				sb.append(
					"I detected a product-related search. This will take " +
						"about 1-2 minutes to complete.");
				sb.append("\n\n");
				sb.append("**Available options:**");
				sb.append("\n");
				sb.append(
					"1. **Fast alternative**: If you know the order ID, use " +
						"\"Find order [ID]\" (1 second)");
				sb.append("\n");
				sb.append(
					"2. **Continue with product search**: Provide your email " +
						"address");
				sb.append("\n\n");
				sb.append("**Please provide your email address:**");
				sb.append("\n");
				sb.append(
					"- \"Search my orders for brake pads using " +
						"your.email@example.com\"");

				return Map.of("search", sb.toString());
			}

			if (Strings.CS.contains(queryLower, "pending") ||
				Strings.CS.contains(queryLower, "shipped") ||
				Strings.CS.contains(queryLower, "completed") ||
				Strings.CS.contains(queryLower, "canceled") ||
				Strings.CS.contains(queryLower, "processing") ||
				Strings.CS.contains(queryLower, "hold") ||
				Strings.CS.contains(queryLower, "status")) {

				StringBuilder sb = new StringBuilder();

				sb.append("**Slow Route: Status Search**");
				sb.append("\n\n");
				sb.append(
					"I detected a status-related search. This will take " +
						"about 1-2 minutes to complete.");
				sb.append("\n\n");
				sb.append("**Available options:**");
				sb.append("\n");
				sb.append(
					"1. **Fast alternative**: If you know the order ID, use " +
						"\"Find order [ID]\" (1 second)");
				sb.append("\n");
				sb.append(
					"2. **Continue with status search**: Provide your email " +
						"address");
				sb.append("\n\n");
				sb.append("**Please provide your email address:**");
				sb.append("\n");
				sb.append(
					"- \"Search my pending orders using " +
						"your.email@example.com\"");

				return Map.of("search", sb.toString());
			}

			if (Strings.CS.contains(queryLower, "address") ||
				Strings.CS.contains(queryLower, "shipped to") ||
				Strings.CS.contains(queryLower, "delivery") ||
				Strings.CS.contains(queryLower, "city") ||
				Strings.CS.contains(queryLower, "state") ||
				Strings.CS.contains(queryLower, "zip") ||
				Strings.CS.contains(queryLower, "postal")) {

				StringBuilder sb = new StringBuilder();

				sb.append("**Extremely Slow Route: Address Search**");
				sb.append("\n\n");
				sb.append(
					"I detected an address-related search. This will take 5+ " +
						"minutes to complete.");
				sb.append("\n\n");
				sb.append("**Available options:**");
				sb.append("\n");
				sb.append(
					"1. **Fast alternative**: If you know the order ID, use " +
						"\"Find order [ID]\" (1 second)");
				sb.append("\n");
				sb.append(
					"2. **Medium speed**: Try date range search if you know " +
						"the timeframe (6 seconds)");
				sb.append("\n");
				sb.append(
					"3. **Continue with address search**: Provide your email " +
						"address (5+ minutes)");
				sb.append("\n\n");
				sb.append("**Please provide your email address:**");
				sb.append("\n");
				sb.append(
					"- \"Search orders shipped to New York using " +
						"your.email@example.com\"");

				return Map.of("search", sb.toString());
			}

			StringBuilder sb = new StringBuilder();

			sb.append("**🔍 General Order Search**");
			sb.append("\n");
			sb.append(
				"I can help you search for orders, but I need more specific " +
					"information to route you to the fastest tool.");
			sb.append("\n\n");
			sb.append("**Fastest Options (1-6 seconds):**");
			sb.append("\n");
			sb.append(
				"- **Direct order lookup**: \"Find order 12345\" (1 second)");
			sb.append("\n");
			sb.append(
				"- **Date range search**: \"Orders from 2024-01-01 to " +
					"2024-01-31\" (6 seconds)");
			sb.append("\n");
			sb.append(
				"- **Customer orders**: \"Get orders for " +
					"customer@email.com\" (6 seconds)");
			sb.append("\n\n");
			sb.append("**Slower Options (1-5 minutes):**");
			sb.append("\n");
			sb.append(
				"- **Product search**: \"Orders containing 'battery'\" (1m " +
					"47s)");
			sb.append("\n");
			sb.append("- **Status search**: \"Pending orders\" (1m 45s)");
			sb.append("\n");
			sb.append(
				"- **Address search**: \"Orders shipped to Dallas\" (5m+)");
			sb.append("\n\n");
			sb.append("**Please specify:**");
			sb.append("\n");
			sb.append("1. What type of search you need");
			sb.append("\n");
			sb.append("2. Your email address (if searching your orders)");
			sb.append("\n");
			sb.append(
				"3. Any specific criteria (dates, products, status, etc.)");

			return Map.of("search", sb.toString());
		}
		catch (Exception exception) {
			return Map.of(
				"error", "Error searching orders: " + exception.getMessage());
		}
	}

	private Account _fetchAccount(long channelId, String search) {
		List<Account> accounts = _getAccounts(channelId, search);

		if (!accounts.isEmpty()) {
			return accounts.get(0);
		}

		return null;
	}

	private String _formatTitle(String title) {
		if ((title == null) || title.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		String[] words = StringUtils.split(StringUtils.lowerCase(title), " ");

		for (int i = 0; i < words.length; i++) {
			String character = words[i];

			if (!StringUtils.isEmpty(character)) {
				sb.append(Character.toUpperCase(character.charAt(0)));

				if (StringUtils.length(character) > 1) {
					sb.append(StringUtils.substring(character, 1));
				}
			}

			if (i < (words.length - 1)) {
				sb.append(" ");
			}
		}

		return sb.toString();
	}

	private String _getAccountNotFoundMessage(String accountName) {
		StringBuilder sb = new StringBuilder();

		sb.append("**Account not found**");
		sb.append("\n\n");
		sb.append("The account '");
		sb.append(accountName);
		sb.append("' was not found in our system.");
		sb.append("\n\n");
		sb.append("**Please check:**");
		sb.append("\n");
		sb.append("- Account spelling and format");
		sb.append("\n");
		sb.append("- If you have an account with us");
		sb.append("\n");
		sb.append("- Contact customer support if you believe this is an error");
		sb.append("\n\n");
		sb.append("**💡 Tip:** Make sure you are using the same ");
		sb.append("account you used when placing your ");
		sb.append("orders.");

		return sb.toString();
	}

	private List<Account> _getAccounts(long channelId, String search) {
		UserAccount userAccount = _commerceService.getUserAccountByEmail(
			SecurityUtils.getEmail());

		List<Account> accounts = _commerceService.getAccounts(
			channelId, search);

		accounts.removeIf(
			account -> !userAccount.containsAccount(account.getId()));

		return accounts;
	}

	private String _getAllAccountsOrderSummaryErrorMessage(
		Exception exception) {

		StringBuilder sb = new StringBuilder();

		sb.append("**Error Retrieving Account Summary**");
		sb.append("\n\n");
		sb.append("**Error**: " + exception.getMessage());
		sb.append("\n");

		String simpleName = exception.getClass(
		).getSimpleName();

		sb.append("**Error Type**: " + simpleName);

		sb.append("\n\n");
		sb.append("**Try These Alternatives:**");
		sb.append("\n");
		sb.append("- \"Show me system status\" - Basic system information");
		sb.append("\n");
		sb.append("- \"What channels and accounts are available?\" ");
		sb.append("- List available resources");
		sb.append("\n");
		sb.append("- Check individual accounts one by one");

		return sb.toString();
	}

	private long _getChannelId() {
		SecurityContext securityContext = SecurityContextHolder.getContext();

		Authentication authentication = securityContext.getAuthentication();

		Jwt jwt = (Jwt)authentication.getPrincipal();

		Settings settings = _settingsService.getActiveSettings(jwt);

		return settings.channelId;
	}

	private OffsetDateTime _getEndOffsetDateTime(String endDate) {
		OffsetDateTime endOffsetDateTime;

		if ((endDate != null) &&
			!StringUtils.isEmpty(StringUtils.trim(endDate))) {

			endOffsetDateTime = _getOffsetDateTime(endDate);
		}
		else {
			endOffsetDateTime = OffsetDateTime.now();
		}

		if ((endOffsetDateTime.getHour() == 0) &&
			(endOffsetDateTime.getMinute() == 0)) {

			endOffsetDateTime = endOffsetDateTime.withHour(
				23
			).withMinute(
				59
			).withSecond(
				59
			);
		}

		return endOffsetDateTime;
	}

	private Map<String, Map<String, String>> _getFaqData() {
		SecurityContext securityContext = SecurityContextHolder.getContext();

		Authentication authentication = securityContext.getAuthentication();

		Jwt jwt = (Jwt)authentication.getPrincipal();

		Settings settings = _settingsService.getActiveSettings(jwt);

		try {
			return _objectMapper.readValue(
				settings.faq,
				new TypeReference<>() {
				});
		}
		catch (JsonProcessingException jsonProcessingException) {
			_log.error("Unable to read FAQ data", jsonProcessingException);

			return Map.of();
		}
	}

	private String _getFaqErrorMessage(String query) {
		StringBuilder sb = new StringBuilder();

		sb.append("**FAQ Search Results**");
		sb.append("\n\n");
		sb.append("I could not find specific information about \"");
		sb.append(query);
		sb.append("\" in our FAQ database.");
		sb.append("\n\n");
		sb.append("**Try these alternatives:**");
		sb.append("\n");
		sb.append("- Ask about specific topics like \"return policy\", ");
		sb.append("\"shipping\", \"payment\", etc.");
		sb.append("\n");
		sb.append("- Use \"Get FAQ information\" to see all available topics");
		sb.append("\n");
		sb.append("- Contact customer support for specific questions");
		sb.append("\n\n");
		sb.append("**Popular topics:**");
		sb.append("\n");
		sb.append("- Ordering and checkout");
		sb.append("\n");
		sb.append("- Shipping and delivery");
		sb.append("\n");
		sb.append("- Returns and refunds");
		sb.append("\n");
		sb.append("- Parts and compatibility");
		sb.append("\n");
		sb.append("- Account management");

		return sb.toString();
	}

	private String _getFaqResponseMessage(
		String query, List<Map<String, String>> matchingAnswers) {

		StringBuilder sb = new StringBuilder();

		sb.append(
			"**FAQ Search Results for: '"
		).append(
			query
		).append(
			"'**\n\n"
		);

		for (int i = 0; i < Math.min(5, matchingAnswers.size()); i++) {
			Map<String, String> match = matchingAnswers.get(i);

			sb.append(
				"**"
			).append(
				i + 1
			).append(
				". "
			).append(
				match.get("question")
			).append(
				"**\n"
			);

			sb.append(
				"*Category: "
			).append(
				match.get("category")
			).append(
				"*\n"
			);

			sb.append(
				match.get("answer")
			).append(
				"\n\n"
			);
		}

		if (matchingAnswers.size() > 5) {
			sb.append(
				"... and "
			).append(
				matchingAnswers.size() - 5
			).append(
				" more results.\n\n"
			);
		}

		sb.append(
			"**💡 Source:** [Official FAQ](https://webserver-lct66degrees-");
		sb.append("uat.lfr.cloud/web/minium-demo/faq)");

		return sb.toString();
	}

	private String _getNoOrdersByProductMessage(String productDescription) {
		StringBuilder sb = new StringBuilder();

		sb.append("**🔍 No Orders Found**\n\n");

		sb.append(
			"No orders found containing products matching: **\""
		).append(
			productDescription
		).append(
			"\"**\n\n"
		);

		sb.append(
			"**Search Term**: "
		).append(
			productDescription
		).append(
			"\n\n"
		);

		sb.append("**💡 Try:**\n- ");
		sb.append("Different product names or keywords\n- ");
		sb.append("Partial product names (e.g., \"brake\" instead of ");
		sb.append("\"brake pads\")\n- SKU codes if you know them\n");
		sb.append(
			"- More general terms (e.g., \"parts\" instead of specific part " +
				"names)\n");

		return sb.toString();
	}

	private String _getNoOrdersByShippingAddressMessage(String addressQuery) {
		StringBuilder sb = new StringBuilder();

		sb.append("**No Orders Found**");
		sb.append("\n\n");
		sb.append("No orders found with shipping address containing: **\"");
		sb.append(addressQuery);
		sb.append("\"**");
		sb.append("\n\n");
		sb.append("**Address Search**: ");
		sb.append(addressQuery);
		sb.append("\n\n");
		sb.append("\n"); // Extra newline from original code
		sb.append("**Try These Search Terms:**");
		sb.append("\n");
		sb.append("- **City**: \"New York\", \"Los Angeles\", \"Chicago\"");
		sb.append("\n");
		sb.append(
			"- **State**: \"CA\", \"NY\", \"TX\", \"California\", \"New " +
				"York\"");
		sb.append("\n");
		sb.append("- **Street**: \"Main Street\", \"Oak Avenue\", \"123\"");
		sb.append("\n");
		sb.append("- **Postal Code**: \"90210\", \"10001\", \"60601\"");
		sb.append("\n");
		sb.append("- **Country**: \"US\", \"United States\", \"Canada\"");
		sb.append("\n");
		sb.append("- **Partial matches**: \"Main\", \"Ave\", \"St\"");
		sb.append("\n\n");
		sb.append("**Examples:**");
		sb.append("\n");
		sb.append(
			"- \"Search orders shipped to New York using " +
				"your.email@example.com\"");
		sb.append("\n");
		sb.append(
			"- \"Find orders with address containing 'Main Street' for " +
				"customer@company.com\"");

		return sb.toString();
	}

	private String _getNullQueryMessage(
		Map<String, Map<String, String>> faqData) {

		StringBuilder sb = new StringBuilder();

		sb.append("**📚 Frequently Asked Questions**\n\n");

		sb.append("Here are the main categories of questions we can help ");

		sb.append("with:\n\n");

		for (Map.Entry<String, Map<String, String>> entry :
				faqData.entrySet()) {

			String categoryName = _formatTitle(
				Strings.CS.replace(entry.getKey(), "_", " "));

			sb.append(
				"**"
			).append(
				categoryName
			).append(
				":**\n"
			);

			for (String questionText :
					entry.getValue(
					).keySet()) {

				sb.append(
					"- "
				).append(
					questionText
				).append(
					"\n"
				);
			}

			sb.append("\n");
		}

		sb.append("**💡 How to use:** Ask me about any of these topics, ");
		sb.append("and I will provide the official answer!\n");
		sb.append("**Example:** 'What is your return policy?' or 'How do I ");
		sb.append("track my order?'");

		return sb.toString();
	}

	private OffsetDateTime _getOffsetDateTime(String date) {
		try {
			List<DateTimeFormatter> dateTimeFormatters = new ArrayList<>();

			dateTimeFormatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			dateTimeFormatters.add(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
			dateTimeFormatters.add(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
			dateTimeFormatters.add(
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			dateTimeFormatters.add(
				DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"));

			String sanitizedDate = StringUtils.trim(
				StringUtils.lowerCase(date));

			if (Strings.CS.equals(sanitizedDate, "today") ||
				Strings.CS.equals(sanitizedDate, "now")) {

				LocalDate localDate = LocalDate.now();

				ZonedDateTime zonedDateTime = localDate.atStartOfDay(
					ZoneId.systemDefault());

				return zonedDateTime.toOffsetDateTime();
			}

			if (Strings.CS.equals(sanitizedDate, "yesterday")) {
				LocalDate localDate = LocalDate.now();

				localDate = localDate.minusDays(1);

				ZonedDateTime zonedDateTime = localDate.atStartOfDay(
					ZoneId.systemDefault());

				return zonedDateTime.toOffsetDateTime();
			}

			Matcher matcher = _pattern.matcher(sanitizedDate);

			if (matcher.find()) {
				int days = Integer.parseInt(matcher.group(1));

				LocalDate localDate = LocalDate.now();

				localDate = localDate.minusDays(days);

				ZonedDateTime zonedDateTime = localDate.atStartOfDay(
					ZoneId.systemDefault());

				return zonedDateTime.toOffsetDateTime();
			}

			for (DateTimeFormatter dateTimeFormatter : dateTimeFormatters) {
				try {
					ZonedDateTime zonedDateTime = null;

					String dateTimeFormatterString =
						dateTimeFormatter.toString();

					if (dateTimeFormatterString.contains("H")) {
						LocalDateTime localDateTime = LocalDateTime.parse(
							StringUtils.trim(date), dateTimeFormatter);

						zonedDateTime = localDateTime.atZone(
							ZoneId.systemDefault());
					}
					else {
						LocalDate localDate = LocalDate.parse(
							StringUtils.trim(date), dateTimeFormatter);

						zonedDateTime = localDate.atStartOfDay(
							ZoneId.systemDefault());
					}

					return zonedDateTime.toOffsetDateTime();
				}
				catch (Exception exception) {
					if (_log.isInfoEnabled()) {
						_log.info(exception);
					}
				}
			}
		}
		catch (Exception exception) {
			_log.error(exception);

			throw new RuntimeException("Unable to parse date: " + date);
		}

		return null;
	}

	private Order _getOrder(String identifier) {
		Order order = _commerceService.getOrderByExternalReferenceCode(
			identifier);

		if (order == null) {
			try {
				order = _commerceService.getOrder(Long.parseLong(identifier));
			}
			catch (NumberFormatException numberFormatException) {
				throw new IllegalArgumentException(numberFormatException);
			}
		}

		if (order != null) {
			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				SecurityUtils.getEmail());

			if (!userAccount.containsAccount(order.getAccountId())) {
				throw new IllegalArgumentException(
					"User does not belong to account");
			}
		}

		return order;
	}

	private StringBuilder _getOrderItemsErrorMessage(
		Exception exception, String orderIdentifier) {

		StringBuilder sb = new StringBuilder();

		sb.append("**Unexpected Error**");
		sb.append("\n\n");
		sb.append("**Order ID**: ");
		sb.append(orderIdentifier);
		sb.append("\n");
		sb.append("**Error**: ");
		sb.append(exception.getMessage());
		sb.append("\n");
		sb.append("**Error Type**: ");
		sb.append(
			exception.getClass(
			).getSimpleName());
		sb.append("\n\n");
		sb.append("**💡 Try These Alternatives:**");
		sb.append("\n");
		sb.append("- \"Find order ");
		sb.append(orderIdentifier);
		sb.append("\" - Get complete order details");
		sb.append("\n");
		sb.append("- \"Search for orders\" - Browse available orders");
		sb.append("\n");
		sb.append("- Check system status with \"Show me system status\"");

		return sb;
	}

	private List<Order> _getOrders(
		long accountId, long channelId, String search, String sort,
		String filter) {

		int page = 1;
		int pageSize = 50;

		List<Order> orders = new ArrayList<>();

		while (orders.size() < 100) {
			PageResult<Order> pageResult =
				_commerceService.getPlacedOrdersByAccount(
					channelId, accountId, page, pageSize, search, sort, filter,
					null);

			if (pageResult == null) {
				break;
			}

			List<Order> pageResultOrders = pageResult.getItems();

			if ((pageResult.getItems() == null) || pageResultOrders.isEmpty()) {
				break;
			}

			int remaining = 100 - orders.size();

			if (pageResultOrders.size() > remaining) {
				orders.addAll(pageResultOrders.subList(0, remaining));
			}
			else {
				orders.addAll(pageResultOrders);
			}

			if (page >= pageResult.getLastPage()) {
				break;
			}

			page++;
		}

		return orders;
	}

	private List<Order> _getOrdersByDateRangeMap(
		Long accountId, long channelId, OffsetDateTime endOffsetDateTime,
		String search, OffsetDateTime startOffsetDateTime) {

		List<Order> orders = new ArrayList<>();

		int currentPage = 1;
		int maxPages = 3;
		int pageSize = 50;

		while (currentPage <= maxPages) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");

			dateTimeFormatter = dateTimeFormatter.withZone(ZoneOffset.UTC);

			String filter = new StringBuilder(
			).append(
				"createDate ge "
			).append(
				dateTimeFormatter.format(startOffsetDateTime)
			).append(
				" and createDate le "
			).append(
				dateTimeFormatter.format(endOffsetDateTime)
			).toString();

			PageResult<Order> pageResult = null;

			if (accountId != null) {
				pageResult = _commerceService.getPlacedOrdersByAccount(
					channelId, accountId, currentPage, pageSize, search,
					"orderDate:desc", filter, null);
			}

			if (pageResult == null) {
				break;
			}

			List<Order> pageResultOrders = pageResult.getItems();

			if ((pageResult.getItems() == null) || pageResultOrders.isEmpty()) {
				break;
			}

			orders.addAll(pageResult.getItems());

			if (currentPage >= pageResult.getLastPage()) {
				break;
			}

			currentPage++;
		}

		return orders;
	}

	private String _getOrderShippingErrorMessage(
		Exception exception, String identifier) {

		StringBuilder sb = new StringBuilder();

		sb.append("**Error Retrieving Shipping Information**");
		sb.append("\n\n");
		sb.append("**Identifier**: ");
		sb.append(identifier);
		sb.append("\n");
		sb.append("**Error**: ");
		sb.append(exception.getMessage());
		sb.append("\n");
		sb.append("**Error Type**: ");
		sb.append(
			exception.getClass(
			).getSimpleName());
		sb.append("\n\n");
		sb.append("**Try These Alternatives:**");
		sb.append("\n");
		sb.append("- \"Find order ");
		sb.append(identifier);
		sb.append("\" - Get complete order details");
		sb.append("\n");
		sb.append("- \"Get order items for order ");
		sb.append(identifier);
		sb.append("\" - View order items");
		sb.append("\n");
		sb.append("- \"Search for orders\" - Browse available orders");

		return sb.toString();
	}

	private String _getUserAccountNotFoundMessage(String email) {
		StringBuilder sb = new StringBuilder();

		sb.append("**Account not found**");
		sb.append("\n\n");
		sb.append("The email '");
		sb.append(email);
		sb.append("' was not found in our system.");
		sb.append("\n\n");
		sb.append("**Please check:**");
		sb.append("\n");
		sb.append("- Email spelling and format");
		sb.append("\n");
		sb.append("- If you have an account with us");
		sb.append("\n");
		sb.append("- Contact customer support if you believe this is an error");
		sb.append("\n\n");
		sb.append("**💡 Tip:** Make sure you are using the same ");
		sb.append("email address you used when placing your ");
		sb.append("orders.");

		return sb.toString();
	}

	private boolean _isMatch(Map<String, String> addressMap, String query) {
		if ((query == null) || query.isEmpty()) {
			return false;
		}

		String normalizedQuery = StringUtils.lowerCase(query);

		normalizedQuery = StringUtils.trim(normalizedQuery);

		for (String value : addressMap.values()) {
			if (value == null) {
				continue;
			}

			value = StringUtils.lowerCase(value);

			if (value.contains(normalizedQuery)) {
				return true;
			}
		}

		String fullAddress = String.join(
			" ", String.valueOf(addressMap.getOrDefault("street1", "")),
			String.valueOf(addressMap.getOrDefault("city", "")),
			String.valueOf(addressMap.getOrDefault("region", "")),
			String.valueOf(addressMap.getOrDefault("regionISOCode", "")),
			String.valueOf(addressMap.getOrDefault("zip", ""))
		).toLowerCase();

		return fullAddress.contains(normalizedQuery);
	}

	private static final Log _log = LogFactory.getLog(CommerceTools.class);

	private static final Pattern _orderIdPattern = Pattern.compile(
		"\\b\\d{4,6}\\b");
	private static final Pattern _pattern = Pattern.compile(
		"last (\\d+) days?");

	private final CommerceService _commerceService;
	private final ObjectMapper _objectMapper;
	private final SettingsService _settingsService;

}