/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.tools;

import com.google.adk.tools.Annotations;

import com.liferay.commerce.ai.chat.bot.model.Account;
import com.liferay.commerce.ai.chat.bot.model.Channel;
import com.liferay.commerce.ai.chat.bot.model.Order;
import com.liferay.commerce.ai.chat.bot.model.OrderItem;
import com.liferay.commerce.ai.chat.bot.model.OrderShipmentDetails;
import com.liferay.commerce.ai.chat.bot.model.PageResult;
import com.liferay.commerce.ai.chat.bot.model.Shipment;
import com.liferay.commerce.ai.chat.bot.model.Summary;
import com.liferay.commerce.ai.chat.bot.model.UserAccount;
import com.liferay.commerce.ai.chat.bot.service.CommerceService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Service;

/**
 * @author Ivica Cardic
 */
@Service
public class CommerceTools {

	public CommerceTools(CommerceService commerceService) {
		_commerceService = commerceService;
	}

	@Annotations.Schema(
		description = "Finds an order by identifier", name = "findOrderTool"
	)
	public Map<String, String> findOrderTool(
		@Annotations.Schema(
			description = "the order id or external reference code",
			name = "identifier"
		)
		String identifier) {

		try {
			Order order = _getOrder(identifier);

			if (order != null) {
				return Map.of("order", _formatOrderSummary(order));
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
	public Map<String, String> getAllAccountsOrderSummaryTool() {
		try {
			List<Channel> channels = _commerceService.getChannels();

			if (channels.isEmpty()) {
				return Map.of("error", "No channels available in the system.");
			}

			Channel channel = channels.get(0);

			String channelId = channel.getId();

			List<Account> accounts = _commerceService.getAccounts(
				channelId, "");

			if (accounts.isEmpty()) {
				return Map.of(
					"error",
					new StringBuilder(
					).append(
						"No accounts available for channel "
					).append(
						channelId
					).append(
						"."
					).toString());
			}

			List<Summary> accountSummaries = new ArrayList<>();

			int totalOrders = 0;

			for (Account account : accounts) {
				try {
					Order order = null;

					List<Order> orders =
						_commerceService.getAllPlacedOrdersByAccountDto(
							channelId, account.getId(), null, "");

					totalOrders += orders.size();

					if (!orders.isEmpty()) {
						order = orders.get(0);
					}

					Summary summary = new Summary();

					summary.setId(account.getId());
					summary.setName(
						(account.getName() != null) ? account.getName() :
							"Unknown Account");
					summary.setType(
						(account.getType() != null) ? account.getType() :
							"N/A");
					summary.setOrderCount(orders.size());
					summary.setOrder(order);

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
					summary.setOrder(null);
					summary.setError(exception.getMessage());

					accountSummaries.add(summary);
				}
			}

			accountSummaries.sort(
				(a, b) -> Integer.compare(
					b.getOrderCount(), a.getOrderCount()));

			return Map.of(
				"response",
				_getAllAccountsOrderSummaryMessage(
					accountSummaries, accounts, channel, channelId,
					totalOrders));
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
			"current_year", localDate.getYear(), "full_date",
			localDate.toString());
	}

	@Annotations.Schema(
		description = "Retrieves orders for a specific customer account",
		name = "getCustomerOrdersByAccountTool"
	)
	public Map<String, String> getCustomerOrdersByAccountTool(
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
			List<Channel> channels = _commerceService.getChannels();

			if (channels.isEmpty()) {
				return Map.of("error", "No channels available in the system.");
			}

			Channel channel = channels.get(0);

			Account account = _fetchAccount(channel, accountName);

			if (account == null) {
				return Map.of("error", _getAccountNotFoundMessage(accountName));
			}

			String channelId = channel.getId();

			List<Order> orders;

			try {
				String orderBy = "desc";

				if (asc) {
					orderBy = "asc";
				}

				orders = _commerceService.getAllPlacedOrdersByAccountDto(
					channelId, account.getId(), null, "createDate:" + orderBy);
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
						accountName
					).append(
						"' but no orders are available."
					).toString());
			}

			if (orders.size() == 1) {
				return Map.of(
					"orders",
					_getOrderMessage(account, accountName, orders.get(0)));
			}

			return Map.of(
				"orders", _getOrdersMessage(account, accountName, orders));
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error getting customer orders: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves orders for a customer by their email address",
		name = "getCustomerOrdersTool"
	)
	public Map<String, String> getCustomerOrdersTool(
		@Annotations.Schema(
			description = "the email address of the customer", name = "email"
		)
		String email,
		@Annotations.Schema(
			description = "whether to sort the orders in ascending order",
			name = "asc"
		)
		boolean asc) {

		try {
			List<Channel> channels = _commerceService.getChannels();

			if (channels.isEmpty()) {
				return Map.of("error", "No channels available in the system.");
			}

			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				email);

			if (userAccount == null) {
				return Map.of("error", _getUserAccountNotFoundMessage(email));
			}

			Channel channel = channels.get(0);

			String channelId = channel.getId();

			List<Order> orders;

			try {
				String orderBy = "desc";

				if (asc) {
					orderBy = "asc";
				}

				orders = _commerceService.getAllPlacedOrdersByAccountDto(
					channelId, null, email, "createDate:" + orderBy);
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

			if (orders.size() == 1) {
				return Map.of(
					"orders",
					_getOrderMessage(userAccount, email, orders.get(0)));
			}

			return Map.of(
				"orders", _getOrdersMessage(userAccount, email, orders));
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error getting customer orders: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves information from the Frequently Asked Questions (FAQ) based on a query",
		name = "getFaqInformationTool"
	)
	public Map<String, String> getFaqInformationTool(
		@Annotations.Schema(
			description = "the search query for the FAQ", name = "query"
		)
		String query) {

		try {
			Map<String, Map<String, String>> faqData = _getFaqData();

			if ((query == null) || query.isEmpty()) {
				return Map.of("response", _getNullQueryMessage(faqData));
			}

			List<Map<String, String>> matchingAnswers = new ArrayList<>();

			for (Map.Entry<String, Map<String, String>> entry :
					faqData.entrySet()) {

				String categoryTitle = _formatTitle(
					Strings.CS.replace(entry.getKey(), "_", " "));

				Map<String, String> categoriesMap = entry.getValue();

				for (Map.Entry<String, String> question :
						categoriesMap.entrySet()) {

					String questionText = question.getKey();
					String answerText = question.getValue();

					String lowerCasedQuestionText = StringUtils.lowerCase(
						questionText);

					String lowerCasedQuery = StringUtils.lowerCase(query);

					boolean questionMatches = lowerCasedQuestionText.contains(
						lowerCasedQuery);

					boolean answerMatches = false;

					String[] words = lowerCasedQuery.split("\\s+");

					for (String word : words) {
						String lowerCasedAnswerText = StringUtils.lowerCase(
							answerText);

						if ((word.length() > 3) &&
							lowerCasedAnswerText.contains(word)) {

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
				return Map.of("error", _getFaqErrorMessage());
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
	public Map<String, String> getOrderItemsTool(
		@Annotations.Schema(
			description = "the order id or external reference code",
			name = "orderIdentifier"
		)
		String orderIdentifier) {

		try {
			List<OrderItem> orderItems;

			Order order = _getOrder(orderIdentifier);

			if (order == null) {
				return Map.of(
					"error",
					"Order " + orderIdentifier +
						" not found or not accessible.");
			}

			try {
				orderItems = _commerceService.getPlacedOrderItems(
					order.getId());
			}
			catch (Exception exception) {
				StringBuilder sb = new StringBuilder();

				sb.append("**🔍 Order Items Lookup Failed**");
				sb.append("\n\n");
				sb.append("**Order ID**: ");
				sb.append(orderIdentifier);
				sb.append("\n");
				sb.append("**Error**: ");
				sb.append(exception.getMessage());
				sb.append("\n\n");
				sb.append("**Possible Causes:**");
				sb.append("\n");
				sb.append("1. The order may not have items in the system");
				sb.append("\n");
				sb.append("2. The Liferay API endpoint may be unavailable");
				sb.append("\n");
				sb.append("3. There may be an authentication issue");
				sb.append("\n\n");
				sb.append("**💡 Try These Alternatives:**");
				sb.append("\n");
				sb.append("- \"Find order ");
				sb.append(orderIdentifier);
				sb.append("\" - Get complete order details");
				sb.append("\n");
				sb.append("- \"Search for orders\" - Browse available orders");
				sb.append("\n");
				sb.append("- Check if the order exists in the system");

				return Map.of("error", sb.toString());
			}

			if (orderItems.isEmpty()) {
				StringBuilder sb = new StringBuilder();

				sb.append("**Order Items for Order ");
				sb.append(orderIdentifier);
				sb.append("**");
				sb.append("\n\n");
				sb.append("**Status**: Order exists but has no items");
				sb.append("\n");
				sb.append("**Items Count**: 0");
				sb.append("\n\n");
				sb.append("**Try**: \"Find order ");
				sb.append(orderIdentifier);
				sb.append("\" for complete order information");

				return Map.of("error", sb.toString());
			}

			StringBuilder sb = new StringBuilder();

			sb.append(
				"**Order Items for Order "
			).append(
				order.getId()
			).append(
				"**\n\n"
			);

			if (order != null) {
				sb.append(
					"**Order Details:** "
				).append(
					(order.getExternalReferenceCode() != null) ?
						order.getExternalReferenceCode() : "N/A"
				).append(
					" | Date: "
				).append(
					order.getCreateDate()
				).append(
					" | Total: "
				).append(
					(order.getTotalFormatted() != null) ?
						order.getTotalFormatted() : "N/A"
				).append(
					"\n\n"
				);

				try {
					List<Shipment> shipments =
						_commerceService.getOrderShipmentsDto(order.getId());

					if (!shipments.isEmpty()) {
						Shipment latestShipment = shipments.get(0);

						sb.append("**Shipping Address:**\n");

						sb.append(
							"- **Address**: "
						).append(
							(latestShipment.getOneLineAddress() != null) ?
								latestShipment.getOneLineAddress() : "N/A"
						).append(
							"\n"
						);

						sb.append(
							"- **Shipping Date**: "
						).append(
							latestShipment.getShippingDate()
						).append(
							"\n"
						);

						sb.append(
							"- **Tracking Number**: "
						).append(
							(latestShipment.getTrackingNumber() != null) ?
								latestShipment.getTrackingNumber() : "N/A"
						).append(
							"\n"
						);

						sb.append(
							"- **Carrier**: "
						).append(
							(latestShipment.getCarrier() != null) ?
								latestShipment.getCarrier() : "N/A"
						).append(
							"\n"
						);

						Shipment.Status statusInfo = latestShipment.getStatus();

						if (statusInfo != null) {
							sb.append(
								"- **Status**: "
							).append(
								(statusInfo.getLabel() != null) ?
									statusInfo.getLabel() : "N/A"
							).append(
								"\n\n"
							);
						}
						else {
							sb.append(
								"- **Status**: "
							).append(
								(latestShipment.getShipmentStatus() != null) ?
									latestShipment.getShipmentStatus() : "N/A"
							).append(
								"\n\n"
							);
						}
					}
					else {
						sb.append("**Shipping Address**: Not available\n\n");
					}
				}
				catch (Exception exception) {
					if (_log.isInfoEnabled()) {
						_log.info(exception);
					}

					sb.append("**Shipping Address**: Not available\n\n");
				}
			}

			sb.append(
				"**Found "
			).append(
				orderItems.size()
			).append(
				" items:**\n\n"
			);

			for (int i = 0; i < orderItems.size(); i++) {
				OrderItem orderItem = orderItems.get(i);

				sb.append(
					i + 1
				).append(
					". **"
				).append(
					(orderItem.getName() != null) ? orderItem.getName() : "N/A"
				).append(
					"**\n"
				);

				sb.append(
					"   SKU: "
				).append(
					(orderItem.getSku() != null) ? orderItem.getSku() : "N/A"
				).append(
					" | Qty: "
				).append(
					orderItem.getQuantity()
				).append(
					" | Price: "
				).append(
					(orderItem.getUnitPrice() != null) ?
						orderItem.getUnitPrice() : "N/A"
				).append(
					"\n\n"
				);
			}

			return Map.of("response", sb.toString());
		}
		catch (Exception exception) {
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

			return Map.of("error", sb.toString());
		}
	}

	@Annotations.Schema(
		description = "Retrieves shipping and tracking information for a specific order",
		name = "getOrderShippingTool"
	)
	public Map<String, String> getOrderShippingTool(
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

			List<Shipment> shipments = null;

			try {
				shipments = _commerceService.getOrderShipmentsDto(
					order.getId());
			}
			catch (Exception exception) {
				if (_log.isInfoEnabled()) {
					_log.info(exception);
				}

				shipments = new ArrayList<>();
			}

			return Map.of(
				"response", _getOrderShippingMessage(order, shipments));
		}
		catch (Exception exception) {
			return Map.of(
				"error", _getOrderShippingErrorMessage(exception, identifier));
		}
	}

	@Annotations.Schema(
		description = "Retrieves information about how to look up customer emails for testing",
		name = "getTestEmailsTool"
	)
	public Map<String, String> getTestEmailsTool() {
		StringBuilder sb = new StringBuilder();

		sb.append("**Customer Email Lookup Information:**");
		sb.append("\n\n");
		sb.append("**How Customer Lookup Works:**");
		sb.append("\n- ");
		sb.append("Enter any email address that exists in our system");
		sb.append("\n- ");
		sb.append("The system will automatically find the customer's account");
		sb.append(
			"\n- Orders will be retrieved for that specific customer only");
		sb.append("\n\n");
		sb.append("**How to Use:**");
		sb.append("\n1. **Customer Order Lookup**: \"Get my ");
		sb.append("orders using your.email@example.com");
		sb.append("\n2. **Order Search**: ");
		sb.append("\"Search for orders customer@company.com");
		sb.append("\n3. **Date Range ");
		sb.append("Search**: \"Search orders from 2024-01-01 to 2024-01-31 ");
		sb.append("using myemail@domain.com\"");
		sb.append("\n\n");
		sb.append("**What You Need:**");
		sb.append("\n- ");
		sb.append("A valid email address that exists in our customer database");
		sb.append("\n- ");
		sb.append("The same email address used when placing orders");
		sb.append("\n- ");
		sb.append("Proper email format (e.g., user@domain.com)");
		sb.append("\n\n");
		sb.append("**If Email Not Found:**");
		sb.append("\n- Check spelling and format");
		sb.append("\n- Verify the email exists in our system");
		sb.append("\n- ");
		sb.append("Contact customer support if needed");

		return Map.of("result", sb.toString());
	}

	@Annotations.Schema(
		description = "Lists all available commerce channels and their associated accounts",
		name = "listAvailableChannelsAndAccountsTool"
	)
	public Map<String, String> listAvailableChannelsAndAccountsTool() {
		try {
			List<Channel> channels = _commerceService.getChannels();

			StringBuilder sb = new StringBuilder(
				"**Available Channels and Accounts**\n\n");

			if ((channels != null) && !channels.isEmpty()) {
				sb.append("**Channels:**\n");

				for (int i = 0; i < channels.size(); i++) {
					Channel channel = channels.get(i);

					sb.append(i + 1);
					sb.append(". **");
					sb.append(
						(channel.getName() != null) ? channel.getName() :
							"N/A");
					sb.append("** (ID: ");
					sb.append(
						(channel.getId() != null) ? channel.getId() : "N/A");
					sb.append(")\n");
					sb.append("   - Type: ");
					sb.append(
						(channel.getType() != null) ? channel.getType() :
							"N/A");
					sb.append("\n");
					sb.append("   - Active: ");
					sb.append(channel.getActive());
					sb.append("\n\n");
				}

				for (Channel channel : channels) {
					String channelId = channel.getId();

					List<Account> accounts = _commerceService.getAccounts(
						channelId, null);

					if ((accounts != null) && !accounts.isEmpty()) {
						sb.append("**Accounts for Channel ");

						sb.append(
							channelId
						).append(
							":**\n"
						);

						for (int i = 0; i < accounts.size(); i++) {
							Account account = accounts.get(i);

							sb.append(i + 1);
							sb.append(". **");
							sb.append(
								(account.getName() != null) ?
									account.getName() : "N/A");
							sb.append("** (ID: ");
							sb.append(
								(account.getId() != null) ? account.getId() :
									"N/A");
							sb.append(")\n");
							sb.append("   - Type: ");
							sb.append(
								(account.getType() != null) ?
									account.getType() : "N/A");
							sb.append("\n");
							sb.append("   - Status: ");
							sb.append(
								(account.getStatus() != null) ?
									account.getStatus() : "N/A");
							sb.append("\n\n");
						}
					}
					else {
						sb.append("**No accounts found for this channel**\n\n");
					}
				}
			}
			else {
				sb.append("**No channels available**\n\n");
			}

			sb.append("**How to use:**\n- ");
			sb.append("Use these IDs in your API calls\n- ");
			sb.append("Ask me to 'Find orders for account [ID]'\n- ");
			sb.append("Ask me to 'Show products for channel [ID]'\n");

			return Map.of("response", sb.toString());
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
	public Map<String, String> searchAccountOrdersByDateRangeTool(
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
			List<Channel> channels = _commerceService.getChannels();

			if (channels.isEmpty()) {
				return Map.of("error", "No channels available in the system.");
			}

			Channel channel = channels.get(0);

			Account account = _fetchAccount(channel, accountName);

			if (account == null) {
				return Map.of("error", _getAccountNotFoundMessage(accountName));
			}

			return _getOrdersByDateRangeMap(
				account.getId(), channel.getId(), endDate, null, startDate);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"**Error searching orders by date**: " +
					exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Searches for orders within a specific date range for a customer by email",
		name = "searchOrdersByDateRangeTool"
	)
	public Map<String, String> searchOrdersByDateRangeTool(
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
			description = "the email address of the customer", name = "email"
		)
		String email) {

		try {
			if (StringUtils.isEmpty(email)) {
				return Map.of("response", _getUserEmailRequiredMessage());
			}

			List<Channel> channels = _commerceService.getChannels();

			if (channels.isEmpty()) {
				return Map.of("error", "No channels available in the system.");
			}

			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				email);

			if (userAccount == null) {
				return Map.of("error", _getUserAccountNotFoundMessage(email));
			}

			Channel channel = channels.get(0);

			return _getOrdersByDateRangeMap(
				null, channel.getId(), endDate, email, startDate);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"**Error searching orders by date**: " +
					exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Searches for orders containing a specific product for a customer",
		name = "searchOrdersByProductTool"
	)
	public Map<String, String> searchOrdersByProductTool(
		@Annotations.Schema(
			description = "a description or name of the product",
			name = "productDescription"
		)
		String productDescription,
		@Annotations.Schema(
			description = "the email address of the customer",
			name = "userEmail"
		)
		String userEmail) {

		try {
			if (StringUtils.isEmpty(userEmail)) {
				return Map.of(
					"response", _getProductEmailRequiredEmailMessage());
			}

			List<Channel> channels = _commerceService.getChannels();

			if (channels.isEmpty()) {
				return Map.of("error", "No channels available in the system.");
			}

			Channel channel = channels.get(0);

			Account account = _fetchAccount(userEmail);

			if (account == null) {
				account = _fetchAccount(channel, userEmail);
			}

			if (account == null) {
				return Map.of(
					"error", _getUserAccountNotFoundMessage(userEmail));
			}

			String channelId = channel.getId();

			List<Order> orders = _getOrders(
				account, channelId, "orderDate:desc");

			if (orders.isEmpty()) {
				return Map.of("error", "**No orders found** for " + userEmail);
			}

			List<AbstractMap.SimpleEntry<Order, List<OrderItem>>> matches =
				new ArrayList<>();

			for (Order order : orders) {
				try {
					String orderId =
						(order.getId() != null) ? order.getId() : "N/A";

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

						if ((term == null) || term.isEmpty()) {
							term = "";
						}

						if (name.contains(term) || sku.contains(term) ||
							term.contains(name)) {

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
					"response",
					_getNoOrdersByProductMessage(account, productDescription));
			}

			return Map.of(
				"response",
				_getOrdersByProductMessage(
					account, matches, productDescription));
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
	public Map<String, String> searchOrdersByShippingAddressTool(
		@Annotations.Schema(
			description = "the address or part of the address to search for",
			name = "addressQuery"
		)
		String addressQuery,
		@Annotations.Schema(
			description = "the email address of the customer",
			name = "userEmail"
		)
		String userEmail) {

		try {
			if (StringUtils.isEmpty(userEmail)) {
				return Map.of("error", _getShippingEmailRequiredEmailMessage());
			}

			List<Channel> channels = _commerceService.getChannels();

			if (channels.isEmpty()) {
				return Map.of("error", "No channels available in the system.");
			}

			Channel channel = channels.get(0);

			Account account = _fetchAccount(userEmail);

			if (account == null) {
				account = _fetchAccount(channel, userEmail);
			}

			if (account == null) {
				return Map.of(
					"error", _getUserAccountNotFoundMessage(userEmail));
			}

			String channelId = channel.getId();

			List<Order> orders = new ArrayList<>();

			int page = 1;
			int pageSize = 50;

			while (true) {
				PageResult<Order> pageResult =
					_commerceService.getPlacedOrdersByAccount(
						channelId, account.getId(), page, pageSize, null,
						"createDate:desc", null);

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

			if (orders.isEmpty()) {
				return Map.of("error", "**No orders found** for " + userEmail);
			}

			List<OrderShipmentDetails> orderShipmentDetailsList =
				new ArrayList<>();

			String addressQueryLower = "";

			if (addressQuery != null) {
				addressQueryLower = StringUtils.lowerCase(addressQuery);

				addressQueryLower = addressQueryLower.trim();
			}

			for (Order order : orders) {
				String orderId = order.getId();

				if (StringUtils.isEmpty(orderId)) {
					continue;
				}

				for (Shipment shipment :
						_commerceService.getOrderShipmentsDto(orderId)) {

					String oneLineAddress = shipment.getOneLineAddress();

					if (StringUtils.isEmpty(oneLineAddress)) {
						continue;
					}

					String lowerCasedOneLineAddress = StringUtils.lowerCase(
						oneLineAddress);

					if (lowerCasedOneLineAddress.contains(addressQueryLower)) {
						OrderShipmentDetails orderShipmentDetails =
							new OrderShipmentDetails();

						orderShipmentDetails.setId(orderId);
						orderShipmentDetails.setCreateDate(
							order.getCreateDate());
						orderShipmentDetails.setOneLineAddress(oneLineAddress);

						String shipmentStatus = "N/A";

						if (shipment.getStatus() != null) {
							Shipment.Status status = shipment.getStatus();

							shipmentStatus = status.getLabel();
						}

						orderShipmentDetails.setShipmentStatus(shipmentStatus);

						orderShipmentDetails.setShippingDate(
							shipment.getShippingDate());
						orderShipmentDetails.setExpectedDate(
							shipment.getExpectedDate());
						orderShipmentDetails.setTrackingNumber(
							(shipment.getTrackingNumber() != null) ?
								shipment.getTrackingNumber() : "N/A");
						orderShipmentDetails.setCarrier(
							(shipment.getCarrier() != null) ?
								shipment.getCarrier() : "N/A");
						orderShipmentDetails.setTotalFormatted(
							(order.getTotalFormatted() != null) ?
								order.getTotalFormatted() : "N/A");

						orderShipmentDetailsList.add(orderShipmentDetails);

						break;
					}
				}
			}

			if (orderShipmentDetailsList.isEmpty()) {
				return Map.of(
					"error",
					_getNoOrdersByShippingAddressMessage(
						addressQuery, account));
			}

			return Map.of(
				"response",
				_getOrdersByAshippingAddressMessage(
					account, addressQuery, orderShipmentDetailsList));
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
	public Map<String, String> searchOrdersByStatusTool(
		@Annotations.Schema(
			description = "the status of the order (e.g., Pending, Shipped, Delivered)",
			name = "orderStatus"
		)
		String orderStatus,
		@Annotations.Schema(
			description = "the email address of the customer",
			name = "userEmail"
		)
		String userEmail) {

		try {
			if (StringUtils.isEmpty(userEmail)) {
				return Map.of(
					"error", _getOrdersByStatusRequiredEmailMessage());
			}

			List<Channel> channels = _commerceService.getChannels();

			if (channels.isEmpty()) {
				return Map.of("error", "No channels available in the system.");
			}

			Channel channel = channels.get(0);

			Account account = _fetchAccount(userEmail);

			if (account == null) {
				account = _fetchAccount(channel, userEmail);
			}

			if (account == null) {
				return Map.of(
					"error", _getUserAccountNotFoundMessage(userEmail));
			}

			String channelId = channel.getId();

			List<Order> orders = _getOrders(
				account, channelId, "createDate:desc");

			if (orders.isEmpty()) {
				return Map.of("error", "**No orders found** for " + userEmail);
			}

			String statusLabel = _getStatusLabel(orderStatus);

			List<Order> filteredOrders = new ArrayList<>();

			for (Order order : orders) {
				String orderId = order.getId();

				if ((orderId == null) || orderId.isEmpty() ||
					!Strings.CI.equals(
						StringUtils.trim(order.getStatusLabel()),
						statusLabel)) {

					continue;
				}

				if (order.getOrderDate() != null) {
					order.setCreateDate(order.getOrderDate());
				}

				filteredOrders.add(order);
			}

			if (filteredOrders.isEmpty()) {
				return Map.of(
					"response",
					_getNoOrdersFoundByStatusMessage(account, orderStatus));
			}

			return Map.of(
				"response",
				_getOrdersByStatusMessage(
					account, filteredOrders, orderStatus));
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
	public Map<String, String> searchOrdersTool(
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

				StringBuilder sb = new StringBuilder();

				sb.append("**Fast Route: Direct Order Lookup**");
				sb.append("\n\n");
				sb.append("I found what looks like an order ID (");
				sb.append(orderId);
				sb.append(") in your query.");
				sb.append("\nLet me get that order directly for ");
				sb.append("you - this will be much faster than searching.");
				sb.append("\n\n");

				Map<String, String> orderToolResult = findOrderTool(orderId);

				if (orderToolResult.containsKey("order")) {
					sb.append(orderToolResult.get("order"));
				}
				else {
					sb.append(orderToolResult.get("error"));
				}

				return Map.of("order", sb.toString());
			}

			if (queryLower.contains("from") || queryLower.contains("between") ||
				queryLower.contains("since") || queryLower.contains("after") ||
				queryLower.contains("before") ||
				queryLower.contains("date range") ||
				queryLower.contains("last ")) {

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

			if (queryLower.contains("product") || queryLower.contains("item") ||
				queryLower.contains("contains") ||
				queryLower.contains("battery") || queryLower.contains("tire") ||
				queryLower.contains("brake") || queryLower.contains("oil")) {

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

			if (queryLower.contains("pending") ||
				queryLower.contains("shipped") ||
				queryLower.contains("completed") ||
				queryLower.contains("canceled") ||
				queryLower.contains("processing") ||
				queryLower.contains("hold") || queryLower.contains("status")) {

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

			if (queryLower.contains("address") ||
				queryLower.contains("shipped to") ||
				queryLower.contains("delivery") ||
				queryLower.contains("city") || queryLower.contains("state") ||
				queryLower.contains("zip") || queryLower.contains("postal")) {

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

	private Account _fetchAccount(Channel channel, String search) {
		List<Account> accounts = _commerceService.getAccounts(
			channel.getId(), search);

		if (!accounts.isEmpty()) {
			return accounts.get(0);
		}

		return null;
	}

	private Account _fetchAccount(String email) {
		UserAccount userAccount = _commerceService.getUserAccountByEmail(email);

		if (userAccount != null) {
			Account account = new Account();

			account.setId(userAccount.getId());

			String fullName = new StringBuilder(
			).append(
				userAccount.getFirstName()
			).append(
				" "
			).append(
				userAccount.getLastName()
			).toString();

			if (StringUtils.isEmpty(fullName)) {
				account.setName(userAccount.getEmail());
			}
			else {
				account.setName(fullName);
			}

			return account;
		}

		return null;
	}

	private String _formatOrderSummary(Order order) {
		String orderDate = "N/A";

		if (order.getOrderDate() != null) {
			orderDate = String.valueOf(order.getOrderDate());
		}

		StringBuilder result = new StringBuilder();

		result.append(
			"**Order Details for Order "
		).append(
			(order.getId() != null) ? order.getId() : "N/A"
		).append(
			"**\n"
		).append(
			"- **Reference**: "
		).append(
			order.getExternalReferenceCode()
		).append(
			"\n"
		).append(
			"- **Status**: "
		).append(
			(order.getStatus() != null) ? order.getStatus() : "N/A"
		).append(
			"\n"
		).append(
			"- **Order Date**: "
		).append(
			orderDate
		).append(
			"\n"
		).append(
			"- **Account**: "
		).append(
			order.getAccountName()
		).append(
			"\n"
		).append(
			"- **Total Amount**: "
		).append(
			order.getTotalFormatted()
		).append(
			"\n"
		).append(
			"- **Items**: "
		).append(
			order.getItemsQuantity()
		).append(
			" items\n"
		);

		if ((order.getShippingAddress() != null) &&
			!order.getShippingAddress(
			).isEmpty()) {

			Map<String, String> shippingAddress = order.getShippingAddress();

			result.append(
				"\n**Shipping Address:**\n"
			).append(
				"- **Name**: "
			).append(
				(shippingAddress.get("name") != null) ?
					shippingAddress.get("name") : "N/A"
			).append(
				"\n"
			).append(
				"- **City**: "
			).append(
				(shippingAddress.get("city") != null) ?
					shippingAddress.get("city") : "N/A"
			).append(
				", "
			).append(
				(shippingAddress.get("regionISOCode") != null) ?
					shippingAddress.get("regionISOCode") : "N/A"
			).append(
				"\n"
			).append(
				"- **Country**: "
			).append(
				(shippingAddress.get("countryISOCode") != null) ?
					shippingAddress.get("countryISOCode") : "N/A"
			).append(
				"\n"
			).append(
				"\n💡 **For complete shipping details, ask**: 'Get shipping "
			).append(
				"info for order "
			).append(
				(order.getId() != null) ? order.getId() : "N/A"
			).append(
				"'\n"
			);
		}

		return result.toString();
	}

	private String _formatTitle(String title) {
		if ((title == null) || title.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		String[] words = StringUtils.split(StringUtils.lowerCase(title), " ");

		for (int i = 0; i < words.length; i++) {
			String character = words[i];

			if (!character.isEmpty()) {
				sb.append(Character.toUpperCase(character.charAt(0)));

				if (character.length() > 1) {
					sb.append(character.substring(1));
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

	private String _getAllAccountsOrderSummaryMessage(
		List<Summary> accountSummaries, List<Account> accounts, Channel channel,
		String channelId, int totalOrders) {

		StringBuilder sb = new StringBuilder();

		sb.append("**Order Summary for All Accounts**\n\n");

		sb.append(
			"**Channel**: "
		).append(
			(channel.getName() != null) ? channel.getName() : "N/A"
		).append(
			" (ID: "
		).append(
			channelId
		).append(
			")\n"
		);

		sb.append(
			"**Total Accounts**: "
		).append(
			accounts.size()
		).append(
			"\n"
		);

		sb.append(
			"**Total Orders**: "
		).append(
			totalOrders
		).append(
			"\n\n"
		);

		sb.append("**Account Breakdown:**\n\n");

		for (int i = 0; i < accountSummaries.size(); i++) {
			Summary summary = accountSummaries.get(i);

			sb.append(
				i + 1
			).append(
				". **"
			).append(
				summary.getName()
			).append(
				"** (ID: "
			).append(
				summary.getId()
			).append(
				")\n"
			);

			sb.append(
				"   **Type**: "
			).append(
				summary.getType()
			).append(
				"\n"
			);

			sb.append(
				"   **Orders**: "
			).append(
				summary.getOrderCount()
			).append(
				"\n"
			);

			if (summary.getOrder() != null) {
				Order order = summary.getOrder();

				sb.append(
					"   **Sample Order**: "
				).append(
					(order.getId() == null) ? "N/A" : order.getId()
				).append(
					" - "
				).append(
					order.getCreateDate()
				).append(
					" - "
				).append(
					(order.getTotalFormatted() != null) ?
						order.getTotalFormatted() : "N/A"
				).append(
					"\n"
				);
			}

			if (summary.getError() != null) {
				sb.append(
					"   **⚠Error**: "
				).append(
					summary.getError()
				).append(
					"\n"
				);
			}

			sb.append("\n");
		}

		int activeAccounts = 0;
		int inactiveAccounts = 0;

		for (Summary summary : accountSummaries) {
			if (summary.getOrderCount() > 0) {
				activeAccounts++;
			}
			else {
				inactiveAccounts++;
			}
		}

		sb.append("**📈 Summary Statistics:**\n");

		sb.append(
			"- **Active Accounts** (with orders): "
		).append(
			activeAccounts
		).append(
			"\n"
		);

		sb.append(
			"- **Inactive Accounts** (no orders): "
		).append(
			inactiveAccounts
		).append(
			"\n"
		);

		double avg = 0.0;

		if (!accounts.isEmpty()) {
			avg = (double)totalOrders / (double)accounts.size();
		}

		sb.append(
			"- **Average Orders per Account**: "
		).append(
			String.format("%.1f", avg)
		).append(
			"\n"
		);

		return sb.toString();
	}

	private OffsetDateTime _getEndOffsetDateTime(String endDate) {
		OffsetDateTime endOffsetDateTime;

		if ((endDate != null) &&
			!endDate.trim(
			).isEmpty()) {

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

	private LinkedHashMap<String, Map<String, String>> _getFaqData() {

		// Official FAQ content from

		//https://webserver-lct66degrees-uat.lfr.cloud/web/minium-demo/faq

		return new LinkedHashMap<String, Map<String, String>>() {
			{
				put(
					"ordering",
					new HashMap<String, String>() {
						{
							put(
								"Do I need an account to place an order?",
								new StringBuilder(
								).append(
									"No, you can check out as a guest. However, creating an "
								).append(
									"account allows you to track your order history, save "
								).append(
									"multiple shipping addresses, and enjoy a faster checkout "
								).append(
									"process on future purchases."
								).toString());
							put(
								"How do I place an order?",
								new StringBuilder(
								).append(
									"To place an order, browse our selection by vehicle "
								).append(
									"make/model, part category, or using our search bar. Add "
								).append(
									"the desired items to your cart, then proceed to checkout "
								).append(
									"Follow the prompts to enter your shipping information "
								).append(
									"and payment details to complete your purchase."
								).toString());
						}
					});
				put(
					"payment",
					new HashMap<String, String>() {
						{
							put(
								"Is my payment information secure?",
								new StringBuilder(
								).append(
									"Absolutely. We use industry-standard SSL encryption and "
								).append(
									"PCI-compliant payment gateways to protect your personal "
								).append(
									"and payment information. Your data is never stored on "
								).append(
									"our servers."
								).toString());
							put(
								"What payment methods do you accept?",
								new StringBuilder(
								).append(
									"We accept all major credit cards (Visa, MasterCard, "
								).append(
									"American Express, Discover), PayPal, and Google Pay. All "
								).append(
									"transactions are securely processed."
								).toString());
						}
					});
				put(
					"shipping",
					new HashMap<String, String>() {
						{
							put(
								"Do you ship internationally?",
								new StringBuilder(
								).append(
									"Yes, we ship to select international destinations. "
								).append(
									"International shipping costs and delivery times vary "
								).append(
									"significantly. Please enter your address at checkout to "
								).append(
									"see available options and costs for your country. "
								).append(
									"Customers are responsible for all customs duties, taxes, "
								).append(
									"and fees."
								).toString());
							put(
								"How can I track my order?",
								new StringBuilder(
								).append(
									"Once your order ships, you will receive a shipping "
								).append(
									"confirmation email with a tracking number. You can click "
								).append(
									"on the link in the email or enter your tracking number "
								).append(
									"on our 'Track Your Order' page."
								).toString());
							put(
								"How long will it take for my order to arrive?",
								new StringBuilder(
								).append(
									"Standard Shipping: Typically 3-7 business days. "
								).append(
									"Expedited Shipping: Typically 2-3 business days. "
								).append(
									"Overnight Shipping: 1 business day (orders must be "
								).append(
									"placed by 2 PM PST for same-day dispatch). please note "
								).append(
									"that these are estimates and may vary based on product "
								).append(
									"availability and carrier delays."
								).toString());
							put(
								"What are your shipping options and costs?",
								new StringBuilder(
								).append(
									"We offer several shipping options, including Standard, "
								).append(
									"Expedited, and Overnight delivery. Shipping costs are "
								).append(
									"calculated at checkout based on your location, the "
								).append(
									"weight/size of your order, and the chosen shipping speed."
								).toString());
						}
					});
				put(
					"order_management",
					new HashMap<String, String>() {
						{
							put(
								"Can I change or cancel my order after it is placed?",
								new StringBuilder(
								).append(
									"We process orders quickly to ensure fast delivery. If "
								).append(
									"you need to change or cancel, please contact us "
								).append(
									"immediately by phone or email. We will do our best to "
								).append(
									"accommodate your request if the order has not yet been "
								).append(
									"shipped."
								).toString());
							put(
								"What if my package is lost or damaged?",
								new StringBuilder(
								).append(
									"Please contact our customer support within 48 hours of "
								).append(
									"the expected delivery date for lost packages, or "
								).append(
									"immediately upon receipt for damaged items. We will "
								).append(
									"initiate a claim with the carrier and arrange for a "
								).append(
									"replacement or refund as quickly as possible."
								).toString());
						}
					});
				put(
					"returns",
					new HashMap<String, String>() {
						{
							put(
								"Are there any non-returnable items?",
								new StringBuilder(
								).append(
									"Yes, certain items are non-returnable for safety or "
								).append(
									"hygiene reasons, or if they are custom-made or marked as "
								).append(
									"'final sale.' This often includes used parts, opened "
								).append(
									"electrical components, or parts that have been "
								).append(
									"installed. Please refer to our full Return Policy for "
								).append(
									"the complete list."
								).toString());
							put(
								"How do I return a part?",
								new StringBuilder(
								).append(
									"To initiate a return, please visit our Returns Portal or "
								).append(
									"contact customer support to receive an RMA (Return "
								).append(
									"Merchandise Authorization) number and detailed "
								).append(
									"instructions. Do not send items back without an RMA."
								).toString());
							put(
								"How long does it take to process a refund?",
								new StringBuilder(
								).append(
									"Once we receive your returned item and inspect it, "
								).append(
									"refunds are typically processed within 5-7 business "
								).append(
									"days. The refund will be issued to your original payment "
								).append(
									"method. Please note that it may take additional time for "
								).append(
									"the refund to appear on your bank statement."
								).toString());
							put(
								"What is your return policy?",
								new StringBuilder(
								).append(
									"We offer a 30-day return policy for most unused parts in "
								).append(
									"their original, unopened packaging. Some exceptions "
								).append(
									"apply (e.g., electrical components, custom orders, final "
								).append(
									"sale items). Please see our full Return Policy for "
								).append(
									"complete details."
								).toString());
						}
					});
				put(
					"parts",
					new HashMap<String, String>() {
						{
							put(
								"Are your parts new or used?",
								new StringBuilder(
								).append(
									"Unless explicitly stated otherwise (e.g., in a 'Used "
								).append(
									"Parts' or 'Salvage' section), all products sold on our "
								).append(
									"website are brand new from the manufacturer."
								).toString());
							put(
								"Do you offer technical support for installation?",
								new StringBuilder(
								).append(
									"While we sell parts, we are not certified mechanics and "
								).append(
									"cannot provide specific installation advice or "
								).append(
									"instructions. We recommend consulting a qualified "
								).append(
									"mechanic or referring to your vehicle's service manual "
								).append(
									"for proper installation procedures."
								).toString());
							put(
								"Do you provide installation instructions?",
								new StringBuilder(
								).append(
									"Some manufacturers include basic installation guides "
								).append(
									"with their parts. However, for detailed instructions, we "
								).append(
									"strongly advise referring to your vehicle's factory "
								).append(
									"service manual or seeking professional automotive "
								).append(
									"assistance."
								).toString());
							put(
								"Do your parts come with a warranty?",
								new StringBuilder(
								).append(
									"Many of our parts come with a manufacturer's warranty. "
								).append(
									"Warranty terms vary by manufacturer and part. Please "
								).append(
									"check the individual product page for specific warranty "
								).append(
									"information. For warranty claims, please contact our "
								).append(
									"support team."
								).toString());
							put(
								"How do I find the right part for my vehicle?",
								new StringBuilder(
								).append(
									"You can use our 'Vehicle Selector' tool on the homepage "
								).append(
									"by entering your Year, Make, and Model. Our search "
								).append(
									"results will then filter for compatible parts. You can "
								).append(
									"also search by VIN number, OEM part number, or part name."
								).toString());
							put(
								"What if I can not find the part I need?",
								new StringBuilder(
								).append(
									"If you are having trouble locating a specific part, "
								).append(
									"please contact our parts specialists. Provide your "
								).append(
									"vehicle's VIN and as much detail about the part as "
								).append(
									"possible, and we will do our best to help you find it or "
								).append(
									"suggest alternatives."
								).toString());
						}
					});
				put(
					"account",
					new HashMap<String, String>() {
						{
							put(
								"How do I reset my password?",
								new StringBuilder(
								).append(
									"Click on the 'Login' button at the top of the page, then "
								).append(
									"click 'Forgot Password?'. Enter your registered email "
								).append(
									"address, and we will send you a link to reset your "
								).append(
									"password."
								).toString());
							put(
								"How do I update my account information?",
								new StringBuilder(
								).append(
									"Log in to your account, and navigate to the 'My Account' "
								).append(
									"or 'Account Settings' section. From there, you can "
								).append(
									"update your personal details, shipping addresses, and "
								).append(
									"payment methods."
								).toString());
						}
					});
				put(
					"support",
					new HashMap<String, String>() {
						{
							put(
								"Do you offer a trade discount for mechanics/shops?",
								new StringBuilder(
								).append(
									"Yes, we offer special pricing and programs for "
								).append(
									"registered automotive businesses and mechanics. Please "
								).append(
									"visit our 'Trade Program' page or contact our B2B sales "
								).append(
									"team for more information."
								).toString());
							put(
								"How can I contact customer service?",
								new StringBuilder(
								).append(
									"You can reach us by: **Phone:** [Your Phone Number] "
								).append(
									"(Mon-Fri, [Hours of Operation]) **Email:** [Your Support "
								).append(
									"Email] (We aim to respond within 24 business hours) "
								).append(
									"**Live Chat:** Available on our website during business "
								).append(
									"hours."
								).toString());
						}
					});
				put(
					"general",
					new HashMap<String, String>() {
						{
							put(
								"Can I pick up my order in person?",
								new StringBuilder(
								).append(
									"No, currently all orders are processed and shipped from "
								).append(
									"our distribution centers. We do not offer local pickup "
								).append(
									"services."
								).toString());
						}
					});
			}
		};
	}

	private String _getFaqErrorMessage() {
		StringBuilder sb = new StringBuilder();

		sb.append("**FAQ Search Results**");
		sb.append("\n\n");
		sb.append("I could not find specific information about \"");
		sb.append("when will my widget arrive");
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

		sb.append("**Source:** [Official FAQ](https://webserver-lct66degrees-");
		sb.append("uat.lfr.cloud/web/minium-demo/faq)");

		return sb.toString();
	}

	private String _getNoOrdersByProductMessage(
		Account account, String productDescription) {

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
			"**Customer**: "
		).append(
			(account.getName() != null) ? account.getName() : "N/A"
		).append(
			"\n"
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

	private String _getNoOrdersByShippingAddressMessage(
		String addressQuery, Account account) {

		StringBuilder sb = new StringBuilder();

		sb.append("**No Orders Found**");
		sb.append("\n\n");
		sb.append("No orders found with shipping address containing: **\"");
		sb.append(addressQuery);
		sb.append("\"**");
		sb.append("\n\n");
		sb.append("**Customer**: ");
		sb.append(account.getName());
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

	private String _getNoOrdersFoundByStatusMessage(
		Account account, String orderStatus) {

		StringBuilder sb = new StringBuilder();

		sb.append("**No Orders Found**\n\n");

		sb.append(
			"No orders found with status: **\""
		).append(
			orderStatus
		).append(
			"\"**\n\n"
		);

		sb.append(
			"**Customer**: "
		).append(
			account.getName()
		).append(
			"\n"
		);

		sb.append(
			"**Search Status**: "
		).append(
			orderStatus
		).append(
			"\n\n"
		);

		sb.append("**💡 Available Status Options:**\n");
		sb.append("- **Canceled** - Orders that have been cancelled\n");
		sb.append("- **Completed** - Orders that are fully completed\n");
		sb.append("- **On Hold** - Orders temporarily paused\n");
		sb.append("- **Partially Shipped** - Orders with some items shipped\n");
		sb.append("- **Pending** - Orders awaiting processing\n");
		sb.append("- **Processing** - Orders currently being processed\n");
		sb.append("- **Shipped** - Orders that have been shipped\n\n");
		sb.append("**Try:**\n");
		sb.append("- Use any of the status names above\n");
		sb.append("- Partial names work too (e.g., \"cancel\"");
		sb.append("for \"Canceled\")\n");
		sb.append(
			"- Check available statuses with \"Show me system status\"\n");

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
				entry.getKey(
				).replace(
					'_', ' '
				));

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

			if (sanitizedDate.equals("today") || sanitizedDate.equals("now")) {
				LocalDate localDate = LocalDate.now();

				ZonedDateTime zonedDateTime = localDate.atStartOfDay(
					ZoneId.systemDefault());

				return zonedDateTime.toOffsetDateTime();
			}

			if (sanitizedDate.equals("yesterday")) {
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
		Order order = _commerceService.getOrder(identifier);

		if (order == null) {
			order = _commerceService.getOrderByExternelReferenceCode(
				identifier);
		}

		return order;
	}

	private String _getOrderMessage(
		Account account, String email, Order order) {

		StringBuilder sb = new StringBuilder();

		sb.append("**Customer Orders for ");
		sb.append(email);
		sb.append("**");
		sb.append("\n");
		sb.append("**Account**: ");
		sb.append(account.getName());
		sb.append(" (ID: ");
		sb.append(account.getId());
		sb.append(")");
		sb.append("\n\n");
		sb.append("**Found 1 order:**");
		sb.append("\n");
		sb.append("- **Order ID**: ");
		sb.append((order.getId() != null) ? order.getId() : "N/A");
		sb.append("\n");
		sb.append("- **Reference**: ");
		sb.append(
			(order.getExternalReferenceCode() != null) ?
				order.getExternalReferenceCode() : "N/A");
		sb.append("\n");
		sb.append("- **Date**: ");
		sb.append(
			(order.getOrderDate() != null) ? order.getOrderDate() : "N/A");
		sb.append("\n");
		sb.append("- **Status**: ");
		sb.append((order.getStatus() != null) ? order.getStatus() : "N/A");
		sb.append("\n");
		sb.append("- **Total**: ");
		sb.append(
			(order.getTotalFormatted() != null) ? order.getTotalFormatted() :
				"N/A");

		return sb.toString();
	}

	private String _getOrderMessage(
		UserAccount userAccount, String email, Order order) {

		StringBuilder sb = new StringBuilder();

		sb.append("**Customer Orders for ");
		sb.append(email);
		sb.append("**");
		sb.append("\n");
		sb.append("**User Email**: ");
		sb.append(userAccount.getEmail());
		sb.append(" (ID: ");
		sb.append(userAccount.getId());
		sb.append(")");
		sb.append("\n\n");
		sb.append("**Found 1 order:**");
		sb.append("\n");
		sb.append("- **Order ID**: ");
		sb.append((order.getId() != null) ? order.getId() : "N/A");
		sb.append("\n");
		sb.append("- **Reference**: ");
		sb.append(
			(order.getExternalReferenceCode() != null) ?
				order.getExternalReferenceCode() : "N/A");
		sb.append("\n");
		sb.append("- **Date**: ");
		sb.append(
			(order.getOrderDate() != null) ? order.getOrderDate() : "N/A");
		sb.append("\n");
		sb.append("- **Status**: ");
		sb.append((order.getStatus() != null) ? order.getStatus() : "N/A");
		sb.append("\n");
		sb.append("- **Total**: ");
		sb.append(
			(order.getTotalFormatted() != null) ? order.getTotalFormatted() :
				"N/A");

		return sb.toString();
	}

	private List<Order> _getOrders(
		Account account, String channelId, String sort) {

		int page = 1;
		int pageSize = 50;

		List<Order> orders = new ArrayList<>();

		while (orders.size() < 100) {
			PageResult<Order> pageResult =
				_commerceService.getPlacedOrdersByAccount(
					channelId, account.getId(), page, pageSize, null, sort,
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

	private String _getOrdersByAshippingAddressMessage(
		Account account, String addressQuery,
		List<OrderShipmentDetails> orderShipmentDetailsList) {

		StringBuilder sb = new StringBuilder();

		sb.append(
			"**🔍 Enhanced Address Search Results: "
		).append(
			orderShipmentDetailsList.size()
		).append(
			" orders with address containing '"
		).append(
			addressQuery
		).append(
			"'**\n"
		);

		sb.append(
			"**Customer**: "
		).append(
			account.getName()
		).append(
			"\n"
		);

		sb.append(
			"**Address Filter**: "
		).append(
			addressQuery
		).append(
			"\n\n"
		);

		for (int i = 0; i < orderShipmentDetailsList.size(); i++) {
			OrderShipmentDetails orderShipmentDetails =
				orderShipmentDetailsList.get(i);

			sb.append(
				"**"
			).append(
				i + 1
			).append(
				". Order #"
			).append(
				(orderShipmentDetails.getId() != null) ?
					orderShipmentDetails.getId() : "N/A"
			).append(
				"**\n"
			);

			sb.append(
				"**Date**: "
			).append(
				orderShipmentDetails.getCreateDate()
			).append(
				"\n"
			);

			sb.append(
				"**Shipment Status**: "
			).append(
				_formatTitle(orderShipmentDetails.getShipmentStatus())
			).append(
				"\n"
			);

			sb.append(
				"**Total**: "
			).append(
				orderShipmentDetails.getTotalFormatted()
			).append(
				"\n"
			);

			sb.append(
				"**Address**: "
			).append(
				(orderShipmentDetails.getOneLineAddress() != null) ?
					orderShipmentDetails.getOneLineAddress() : "N/A"
			).append(
				"\n"
			);

			String shippingDate =
				(orderShipmentDetails.getShippingDate() != null) ?
					String.valueOf(orderShipmentDetails.getShippingDate()) :
						"N/A";

			if (!shippingDate.equals("N/A")) {
				sb.append(
					"**Shipped**: "
				).append(
					shippingDate
				).append(
					"\n"
				);
			}

			String trackingNumber =
				(orderShipmentDetails.getTrackingNumber() != null) ?
					orderShipmentDetails.getTrackingNumber() : "N/A";

			if ((trackingNumber != null) && !trackingNumber.equals("N/A")) {
				sb.append(
					"**Tracking**: "
				).append(
					trackingNumber
				).append(
					"\n"
				);
			}

			sb.append("\n");
		}

		sb.append(
			"**For detailed shipping info, ask**: 'Get shipping info for " +
				"order [ORDER_ID]'");

		return sb.toString();
	}

	private Map<String, String> _getOrdersByDateRangeMap(
		String accountId, String channelId, String endDate, String search,
		String startDate) {

		OffsetDateTime startOffsetDateTime = null;
		OffsetDateTime endOffsetDateTime = null;

		try {
			startOffsetDateTime = _getOffsetDateTime(startDate);
			endOffsetDateTime = _getEndOffsetDateTime(endDate);
		}
		catch (RuntimeException runtimeException) {
			StringBuilder sb = new StringBuilder();

			sb.append("**Date Parsing Error**: ");
			sb.append(runtimeException.getMessage());
			sb.append("\n\n");
			sb.append("**Supported formats:**");
			sb.append("\n");
			sb.append("- YYYY-MM-DD (2024-01-15)");
			sb.append("\n");
			sb.append("- MM/DD/YYYY (01/15/2024)");
			sb.append("\n");
			sb.append("- MM-DD-YYYY (01-15-2024)");
			sb.append("\n");
			sb.append("- today, yesterday");
			sb.append("\n");
			sb.append("- last X days (last 7 days)");
			sb.append("\n\n");
			sb.append(
				"**Example:** 'Search orders from 2024-01-01 to 2024-01-31'");

			return Map.of("error", sb.toString());
		}

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

			PageResult<Order> pageResult =
				_commerceService.getPlacedOrdersByAccount(
					channelId, accountId, currentPage, pageSize, search,
					"orderDate:desc", filter);

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

		if (orders.isEmpty()) {
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
				"yyyy-MM-dd");

			return Map.of(
				"error",
				new StringBuilder(
				).append(
					"**No orders found** for "
				).append(
					search
				).append(
					" between "
				).append(
					dateTimeFormatter.format(startOffsetDateTime)
				).append(
					" and "
				).append(
					dateTimeFormatter.format(endOffsetDateTime)
				).toString());
		}

		return Map.of(
			"response",
			_getOrdersByDateRangeMessage(
				endOffsetDateTime, orders, startOffsetDateTime, search));
	}

	private String _getOrdersByDateRangeMessage(
		OffsetDateTime endOffsetDateTime, List<Order> orders,
		OffsetDateTime startOffsetDateTime, String userEmail) {

		StringBuilder sb = new StringBuilder();

		sb.append(
			"**Orders Found: "
		).append(
			orders.size()
		).append(
			" orders for "
		).append(
			userEmail
		).append(
			"**\n"
		);

		sb.append(
			"**Customer**: "
		).append(
			(userEmail != null) ? userEmail : "N/A"
		).append(
			"\n"
		);

		sb.append(
			"**Date Range**: "
		).append(
			DateTimeFormatter.ofPattern(
				"yyyy-MM-dd"
			).format(
				startOffsetDateTime
			)
		).append(
			" to "
		).append(
			DateTimeFormatter.ofPattern(
				"yyyy-MM-dd"
			).format(
				endOffsetDateTime
			)
		).append(
			"\n\n"
		);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
			"yyyy-MM-dd HH:mm");

		for (int i = 0; i < Math.min(20, orders.size()); i++) {
			Order order = orders.get(i);

			String orderId = (order.getId() != null) ? order.getId() : "N/A";

			String externalReferenceCode = "N/A";
			String dateString;

			try {
				dateString = (order.getOrderDate() != null) ?
					dateTimeFormatter.format(order.getOrderDate()) : "N/A";
			}
			catch (Exception exception) {
				if (_log.isInfoEnabled()) {
					_log.info(exception);
				}

				dateString = "N/A";
			}

			sb.append(
				i + 1
			).append(
				". **Order "
			).append(
				orderId
			).append(
				"** - "
			).append(
				externalReferenceCode
			).append(
				"\n"
			);

			sb.append(
				"   Date: "
			).append(
				dateString
			).append(
				", Status: "
			).append(
				(order.getStatus() != null) ? order.getStatus() : "N/A"
			).append(
				", Total: "
			).append(
				(order.getTotalFormatted() != null) ?
					order.getTotalFormatted() : "N/A"
			).append(
				"\n\n"
			);
		}

		if (orders.size() > 20) {
			sb.append(
				"... and "
			).append(
				orders.size() - 20
			).append(
				" more orders.\n"
			);
		}

		sb.append("\n**💡 Tips:**\n");
		sb.append("- Use 'Find order [ID]' for detailed order information\n");
		sb.append("- Use 'Get order items for order [ID]' for item details\n");
		sb.append(
			"- Use 'Get shipping info for order [ID]' for shipping details\n");

		return sb.toString();
	}

	private String _getOrdersByProductMessage(
		Account account,
		List<AbstractMap.SimpleEntry<Order, List<OrderItem>>> matches,
		String productDescription) {

		StringBuilder sb = new StringBuilder();

		sb.append(
			"**🔍 Product Search Results: "
		).append(
			matches.size()
		).append(
			" orders containing '"
		).append(
			productDescription
		).append(
			"'**\n"
		);

		sb.append(
			"**Customer**: "
		).append(
			(account.getName() != null) ? account.getName() : "N/A"
		).append(
			"\n"
		);

		sb.append(
			"**Search Term**: "
		).append(
			productDescription
		).append(
			"\n\n"
		);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
			"yyyy-MM-dd HH:mm");

		for (int i = 0; i < Math.min(10, matches.size()); i++) {
			AbstractMap.SimpleEntry<Order, List<OrderItem>> entry = matches.get(
				i);

			Order order = entry.getKey();

			List<OrderItem> orderItems = entry.getValue();

			String orderId = (order.getId() != null) ? order.getId() : "N/A";
			String externalReferenceCode = "N/A";
			String status =
				(order.getStatus() != null) ? order.getStatus() : "N/A";
			String total = "N/A";
			String dateString;

			try {
				if (order.getOrderDate() != null) {
					dateString = dateTimeFormatter.format(order.getOrderDate());
				}
				else {
					dateString = "N/A";
				}
			}
			catch (Exception exception) {
				if (_log.isInfoEnabled()) {
					_log.info(exception);
				}

				dateString = "N/A";
			}

			sb.append(
				i + 1
			).append(
				". **Order "
			).append(
				orderId
			).append(
				"** - "
			).append(
				externalReferenceCode
			).append(
				"\n"
			);

			sb.append(
				"   Date: "
			).append(
				dateString
			).append(
				", Status: "
			).append(
				status
			).append(
				", Total: "
			).append(
				total
			).append(
				"\n"
			);

			if ((orderItems != null) && !orderItems.isEmpty()) {
				sb.append("   **Matching Products:**\n");

				for (int j = 0; j < Math.min(3, orderItems.size()); j++) {
					OrderItem orderItem = orderItems.get(j);

					sb.append(
						"   - "
					).append(
						(orderItem.getName() != null) ? orderItem.getName() :
							"Unknown Product"
					).append(
						" (SKU: "
					).append(
						(orderItem.getSku() != null) ? orderItem.getSku() :
							"N/A"
					).append(
						") x"
					).append(
						orderItem.getQuantity()
					).append(
						"\n"
					);
				}

				if (orderItems.size() > 3) {
					sb.append(
						"   - ... and "
					).append(
						orderItems.size() - 3
					).append(
						" more items\n"
					);
				}
			}

			sb.append("\n");
		}

		if (matches.size() > 10) {
			sb.append(
				"... and "
			).append(
				matches.size() - 10
			).append(
				" more orders.\n\n"
			);
		}

		sb.append(
			"**Total Orders Found**: "
		).append(
			matches.size()
		).append(
			"\n"
		);

		sb.append(
			"**Customer**: "
		).append(
			(account.getName() != null) ? account.getName() : "N/A"
		).append(
			"\n\n"
		);

		sb.append("**💡 Tips:**\n");
		sb.append("- Use 'Find order [ID]' for detailed order information\n");
		sb.append(
			"- Use 'Get order items for order [ID]' for complete item " +
				"details\n");
		sb.append("- Try different search terms for better results\n");

		return sb.toString();
	}

	private String _getOrdersByStatusMessage(
		Account account, List<Order> filteredOrders, String orderStatus) {

		StringBuilder sb = new StringBuilder();

		sb.append(
			"**🔍 Enhanced Status Search Results: "
		).append(
			filteredOrders.size()
		).append(
			" orders with status '"
		).append(
			orderStatus
		).append(
			"'**\n"
		);

		sb.append(
			"**Customer**: "
		).append(
			account.getName()
		).append(
			"\n"
		);

		sb.append(
			"**Status Filter**: "
		).append(
			orderStatus
		).append(
			"\n\n"
		);

		for (int i = 0; i < filteredOrders.size(); i++) {
			Order order = filteredOrders.get(i);

			sb.append(
				"**"
			).append(
				i + 1
			).append(
				". Order #"
			).append(
				(order.getId() != null) ? order.getId() : "N/A"
			).append(
				"**\n"
			);

			sb.append(
				"   📅 **Date**: "
			).append(
				order.getCreateDate()
			).append(
				"\n"
			);

			sb.append(
				"   📊 **Status**: "
			).append(
				_formatTitle(
					(order.getStatusLabel() != null) ? order.getStatusLabel() :
						"N/A")
			).append(
				"\n"
			);

			String statusCode = order.getStatusCode();

			if (StringUtils.isEmpty(statusCode)) {
				sb.append(
					"   🔢 **Status Code**: "
				).append(
					statusCode
				).append(
					"\n"
				);
			}

			sb.append(
				"   💰 **Total**: "
			).append(
				(order.getTotalFormatted() != null) ?
					order.getTotalFormatted() : "N/A"
			).append(
				"\n\n"
			);
		}

		sb.append(
			"**💡 For detailed order info, ask**: 'Find order [ORDER_ID]'");

		return sb.toString();
	}

	private String _getOrdersByStatusRequiredEmailMessage() {
		StringBuilder sb = new StringBuilder();

		sb.append("**User Identification Required**");
		sb.append("\n\n");
		sb.append("To search for orders by status, I need to know which ");
		sb.append("customer you are.");
		sb.append("\n\n");
		sb.append("**Please provide your email address:**");
		sb.append(
			"\n- \"Search my pending orders using your.email@example.com\"");
		sb.append("\n");
		sb.append("- \"Find shipped orders for customer@company.com\"");
		sb.append("\n");
		sb.append("- \"Show me canceled orders using myemail@domain.com\"");
		sb.append("\n\n");
		sb.append("**Available Status Options:**");
		sb.append("\n");
		sb.append(
			"- Canceled, Completed, On Hold, Partially Shipped,  Pending, " +
				"Processing, Shipped");
		sb.append("\n\n");
		sb.append(
			"**💡 Use the same email address you used when placing your " +
				"orders.**");

		return sb.toString();
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

	private String _getOrderShippingMessage(
		Order order, List<Shipment> shipments) {

		Map<String, String> shippingAddress = order.getShippingAddress();
		Map<String, String> billingAddress = order.getBillingAddress();

		StringBuilder sb = new StringBuilder();

		sb.append(
			"**Shipping Information for Order "
		).append(
			order.getId()
		).append(
			"**\n\n"
		);

		sb.append("**Order Summary:**\n");

		sb.append(
			"- **Subtotal**: "
		).append(
			(order.getSubtotalFormatted() != null) ?
				order.getSubtotalFormatted() : "N/A"
		).append(
			"\n"
		);

		sb.append(
			"- **Shipping Cost**: "
		).append(
			(order.getShippingValueFormatted() != null) ?
				order.getShippingValueFormatted() : "N/A"
		).append(
			"\n"
		);

		String shippingDiscount =
			(order.getShippingDiscountValueFormatted() != null) ?
				order.getShippingDiscountValueFormatted() : "N/A";

		if (!shippingDiscount.equals("N/A") &&
			!shippingDiscount.equals("$ 0.00")) {

			sb.append(
				"- **Shipping Discount**: "
			).append(
				shippingDiscount
			).append(
				"\n"
			);
		}

		sb.append(
			"- **Tax**: "
		).append(
			(order.getTaxValueFormatted() != null) ?
				order.getTaxValueFormatted() : "N/A"
		).append(
			"\n"
		);

		sb.append(
			"- **Total**: "
		).append(
			(order.getTotalFormatted() != null) ? order.getTotalFormatted() :
				"N/A"
		).append(
			"\n\n"
		);

		Shipment shipment = null;

		if ((shipments != null) && !shipments.isEmpty()) {
			shipment = shipments.get(0);
		}

		if ((shipment != null) && (shipment.getOneLineAddress() != null) &&
			!StringUtils.equals(shipment.getOneLineAddress(), "N/A")) {

			sb.append("**Shipping Address (from Shipments):**\n");

			sb.append(
				"- **Address**: "
			).append(
				(shipment.getOneLineAddress() != null) ?
					shipment.getOneLineAddress() : "N/A"
			).append(
				"\n"
			);

			sb.append(
				"- **Shipping Date**: "
			).append(
				shipment.getShippingDate()
			).append(
				"\n"
			);

			sb.append(
				"- **Tracking Number**: "
			).append(
				(shipment.getTrackingNumber() != null) ?
					shipment.getTrackingNumber() : "N/A"
			).append(
				"\n"
			);

			sb.append(
				"- **Carrier**: "
			).append(
				(shipment.getCarrier() != null) ? shipment.getCarrier() : "N/A"
			).append(
				"\n"
			);

			String status = null;

			Shipment.Status shipmentStatus = shipment.getStatus();

			if ((shipmentStatus != null) &&
				(shipmentStatus.getLabel() != null)) {

				status = shipmentStatus.getLabel();
			}
			else {
				status = shipment.getShipmentStatus();
			}

			sb.append(
				"- **Status**: "
			).append(
				(status != null) ? status : "N/A"
			).append(
				"\n\n"
			);
		}
		else if ((shippingAddress != null) && !shippingAddress.isEmpty()) {
			sb.append("**Shipping Address (from Order):**\n");

			sb.append(
				"- **Name**: "
			).append(
				shippingAddress.getOrDefault("name", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **Street**: "
			).append(
				shippingAddress.getOrDefault("street1", "N/A")
			).append(
				"\n"
			);

			String street2 = shippingAddress.get("street2");

			if ((street2 != null) && !street2.isEmpty()) {
				sb.append(
					"- **Street 2**: "
				).append(
					street2
				).append(
					"\n"
				);
			}

			sb.append(
				"- **City**: "
			).append(
				shippingAddress.getOrDefault("city", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **State/Province**: "
			).append(
				shippingAddress.getOrDefault("regionISOCode", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **Postal Code**: "
			).append(
				shippingAddress.getOrDefault("zip", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **Country**: "
			).append(
				shippingAddress.getOrDefault("countryISOCode", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **Phone**: "
			).append(
				shippingAddress.getOrDefault("phoneNumber", "N/A")
			).append(
				"\n\n"
			);
		}
		else {
			sb.append("**Shipping Address**: Not available\n\n");
		}

		if ((billingAddress != null) && !billingAddress.isEmpty()) {
			sb.append("**Billing Address:**\n");

			sb.append(
				"- **Name**: "
			).append(
				billingAddress.getOrDefault("name", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **Street**: "
			).append(
				billingAddress.getOrDefault("street1", "N/A")
			).append(
				"\n"
			);

			String billStreet2 = billingAddress.get("street2");

			if ((billStreet2 != null) && !billStreet2.isEmpty()) {
				sb.append(
					"- **Street 2**: "
				).append(
					billStreet2
				).append(
					"\n"
				);
			}

			sb.append(
				"- **City**: "
			).append(
				billingAddress.getOrDefault("city", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **State/Province**: "
			).append(
				billingAddress.getOrDefault("regionISOCode", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **Postal Code**: "
			).append(
				billingAddress.getOrDefault("zip", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **Country**: "
			).append(
				billingAddress.getOrDefault("countryISOCode", "N/A")
			).append(
				"\n"
			);

			sb.append(
				"- **Phone**: "
			).append(
				billingAddress.getOrDefault("phoneNumber", "N/A")
			).append(
				"\n\n"
			);
		}
		else {
			sb.append("**💳 Billing Address**: Not available\n\n");
		}

		sb.append("**Additional Information:**\n");

		sb.append(
			"- **Order Date**: "
		).append(
			order.getCreateDate()
		).append(
			"\n"
		);

		sb.append(
			"- **Order Status**: "
		).append(
			(order.getStatus() != null) ? order.getStatus() : "N/A"
		).append(
			"\n"
		);

		sb.append(
			"- **Order Reference**: "
		).append(
			(order.getExternalReferenceCode() != null) ?
				order.getExternalReferenceCode() : "N/A"
		);

		return sb.toString();
	}

	private String _getOrdersMessage(
		Account account, String email, List<Order> orders) {

		StringBuilder sb = new StringBuilder();

		sb.append(
			"**Customer Orders for "
		).append(
			email
		).append(
			"**\n"
		);

		sb.append(
			"**Account**: "
		).append(
			account.getName()
		).append(
			" (ID: "
		).append(
			account.getId()
		).append(
			")\n\n"
		);

		sb.append(
			"**Found "
		).append(
			orders.size()
		).append(
			" orders:**"
		);

		for (int i = 0; i < Math.min(10, orders.size()); i++) {
			Order order = orders.get(i);

			String orderId = "N/A";

			if (order.getId() != null) {
				orderId = order.getId();
			}

			String createDate = "N/A";

			if (order.getCreateDate() != null) {
				createDate = String.valueOf(order.getCreateDate());
			}

			String status = "N/A";

			if (order.getStatus() != null) {
				status = order.getStatus();
			}

			String total = "N/A";

			if (order.getTotalFormatted() != null) {
				total = order.getTotalFormatted();
			}

			sb.append(
				"\n"
			).append(
				i + 1
			).append(
				". **Order "
			).append(
				orderId
			).append(
				"** - "
			).append(
				orderId
			);

			sb.append(
				"\n   Date: "
			).append(
				createDate
			).append(
				", Status: "
			).append(
				status
			).append(
				", Total: "
			).append(
				total
			);
		}

		if (orders.size() > 10) {
			sb.append(
				"\n\n... and "
			).append(
				orders.size() - 10
			).append(
				" more orders."
			);
		}

		sb.append(
			"\n\n**Total Orders**: "
		).append(
			orders.size()
		);

		sb.append(
			"\n**Account**: "
		).append(
			account.getName()
		);

		return sb.toString();
	}

	private String _getOrdersMessage(
		UserAccount userAccount, String email, List<Order> orders) {

		StringBuilder sb = new StringBuilder();

		sb.append(
			"**Customer Orders for "
		).append(
			email
		).append(
			"**\n"
		);

		sb.append(
			"**User Email**: "
		).append(
			userAccount.getEmail()
		).append(
			" (ID: "
		).append(
			userAccount.getId()
		).append(
			")\n\n"
		);

		sb.append(
			"**Found "
		).append(
			orders.size()
		).append(
			" orders:**"
		);

		for (int i = 0; i < Math.min(10, orders.size()); i++) {
			Order order = orders.get(i);

			String orderId = "N/A";

			if (order.getId() != null) {
				orderId = order.getId();
			}

			String createDate = "N/A";

			if (order.getCreateDate() != null) {
				createDate = String.valueOf(order.getCreateDate());
			}

			String status = "N/A";

			if (order.getStatus() != null) {
				status = order.getStatus();
			}

			String total = "N/A";

			if (order.getTotalFormatted() != null) {
				total = order.getTotalFormatted();
			}

			sb.append(
				"\n"
			).append(
				i + 1
			).append(
				". **Order "
			).append(
				orderId
			).append(
				"** - "
			).append(
				orderId
			);

			sb.append(
				"\n   Date: "
			).append(
				createDate
			).append(
				", Status: "
			).append(
				status
			).append(
				", Total: "
			).append(
				total
			);
		}

		if (orders.size() > 10) {
			sb.append(
				"\n\n... and "
			).append(
				orders.size() - 10
			).append(
				" more orders."
			);
		}

		sb.append(
			"\n\n**Total Orders**: "
		).append(
			orders.size()
		);

		sb.append(
			"\n**User Email**: "
		).append(
			userAccount.getEmail()
		);

		return sb.toString();
	}

	private String _getProductEmailRequiredEmailMessage() {
		StringBuilder sb = new StringBuilder();

		sb.append("**User Identification Required**");
		sb.append("\n\n");
		sb.append(
			"To search for orders by product, I need to know which customer " +
				"you are.");
		sb.append("\n\n");
		sb.append("**Please provide your email address:**");
		sb.append("\n");
		sb.append(
			"- \"Search my orders for brake pads using " +
				"your.email@example.com\"");
		sb.append("\n");
		sb.append(
			"- \"Find orders containing 'tires' for customer@company.com\"");
		sb.append("\n");
		sb.append("- \"Show me orders with 'oil' using myemail@domain.com\"");
		sb.append("\n\n");
		sb.append(
			"**💡 Use the same email address you used when placing your " +
				"orders.**");

		return sb.toString();
	}

	private String _getShippingEmailRequiredEmailMessage() {
		StringBuilder sb = new StringBuilder();

		sb.append("**User Identification Required**");
		sb.append("\n\n");
		sb.append(
			"To search for orders by shipping address, I need to know which " +
				"customer you are.");
		sb.append("\n\n");
		sb.append("**Please provide your email address:**");
		sb.append("\n");
		sb.append(
			"- \"Search orders shipped to New York using " +
				"your.email@example.com\"");
		sb.append("\n");
		sb.append(
			"- \"Find orders with address containing 'Main Street' for " +
				"customer@company.com\"");
		sb.append("\n");
		sb.append(
			"- \"Show me orders shipped to California using " +
				"myemail@domain.com\"");
		sb.append("\n\n");
		sb.append("**Available Address Search Options:**");
		sb.append("\n");
		sb.append("- Street name or number");
		sb.append("\n");
		sb.append("- City name");
		sb.append("\n");
		sb.append("- State/Province");
		sb.append("\n");
		sb.append("- Postal/ZIP code");
		sb.append("\n");
		sb.append("- Country");
		sb.append("\n");
		sb.append("- Partial address matches");
		sb.append("\n\n");
		sb.append(
			"**💡 Use the same email address you used when placing your " +
				"orders.**");

		return sb.toString();
	}

	private String _getStatusLabel(String status) {
		String statusLabel = null;

		Map<String, List<String>> statusMappings = Map.ofEntries(
			Map.entry(
				"canceled", Arrays.asList("canceled", "cancelled", "cancel")),
			Map.entry(
				"completed", Arrays.asList("completed", "complete", "done")),
			Map.entry(
				"on hold",
				Arrays.asList("on hold", "hold", "on-hold", "onhold")),
			Map.entry(
				"partially shipped",
				Arrays.asList(
					"partially shipped", "partial", "partially",
					"part shipped")),
			Map.entry("pending", Arrays.asList("pending", "pend", "waiting")),
			Map.entry(
				"processing",
				Arrays.asList(
					"processing", "process", "in process", "in progress")),
			Map.entry(
				"shipped",
				Arrays.asList(
					"shipped", "shipping", "delivered", "out for delivery")));

		status = StringUtils.trim(StringUtils.lowerCase(status));

		for (Map.Entry<String, List<String>> entry :
				statusMappings.entrySet()) {

			for (String value : entry.getValue()) {
				if (status.equals(value) || status.contains(value)) {
					statusLabel = entry.getKey();

					break;
				}
			}

			if (statusLabel != null) {				break;
			}
		}

		if (statusLabel == null) {
			statusLabel = status;
		}

		return statusLabel;
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

	private String _getUserEmailRequiredMessage() {
		StringBuilder sb = new StringBuilder();

		sb.append("**User Identification Required**");
		sb.append("\n\n");
		sb.append("To search for orders by date range, I need to know ");
		sb.append("which customer you are.");
		sb.append("\n\n");
		sb.append("**Please provide your email address:**");
		sb.append("\n- ");
		sb.append("Search my orders from 2024-01-01 to 2024-01-31 ");
		sb.append("using your.email@example.com");
		sb.append("\n- ");
		sb.append("Find orders since 2024-01-01 for ");
		sb.append("customer@company.com");
		sb.append("\n- ");
		sb.append("Show orders from last 7 days using ");
		sb.append("myemail@domain.com");
		sb.append("\n\n");
		sb.append("**Use the same email address you used when placing ");
		sb.append("your orders.**");

		return sb.toString();
	}

	private static final Log _log = LogFactory.getLog(CommerceTools.class);

	private static final Pattern _orderIdPattern = Pattern.compile(
		"\\b\\d{4,6}\\b");
	private static final Pattern _pattern = Pattern.compile(
		"last (\\d+) days?");

	private final CommerceService _commerceService;

}