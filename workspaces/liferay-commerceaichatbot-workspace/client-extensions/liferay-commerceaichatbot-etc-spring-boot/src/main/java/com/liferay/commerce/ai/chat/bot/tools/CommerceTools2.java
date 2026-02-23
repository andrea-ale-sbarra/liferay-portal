/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.adk.tools.Annotations;

import com.liferay.commerce.ai.chat.bot.model.Account;
import com.liferay.commerce.ai.chat.bot.model.Order;
import com.liferay.commerce.ai.chat.bot.model.OrderItem;
import com.liferay.commerce.ai.chat.bot.model.PageResult;
import com.liferay.commerce.ai.chat.bot.model.Settings;
import com.liferay.commerce.ai.chat.bot.model.Shipment;
import com.liferay.commerce.ai.chat.bot.model.UserAccount;
import com.liferay.commerce.ai.chat.bot.service.CommerceService;
import com.liferay.commerce.ai.chat.bot.service.SettingsService;
import com.liferay.commerce.ai.chat.bot.util.SecurityUtils;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class CommerceTools2 {

	public CommerceTools2(
		CommerceService commerceService, ObjectMapper objectMapper,
		SettingsService settingsService) {

		_commerceService = commerceService;
		_objectMapper = objectMapper;
		_settingsService = settingsService;
	}

	@Annotations.Schema(
		description = "Finds an order by its external reference code",
		name = "findOrderByExternalReferenceCodeTool"
	)
	public Map<String, Object> findOrderByExternalReferenceCodeTool(
		@Annotations.Schema(
			description = "the external reference code",
			name = "externalReferenceCode"
		)
		String externalReferenceCode) {

		try {
			Order order =
				_commerceService.getOrderByExternalReferenceCode(
					externalReferenceCode);

			if (order == null) {
				return Map.of(
					"error",
					"Order with external reference code '" +
						externalReferenceCode + "' not found.");
			}

			return Map.of("order", order);
		}
		catch (Exception exception) {
			return Map.of(
				"error", "Error finding order: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Finds an order by its ID",
		name = "findOrderByIdTool"
	)
	public Map<String, Object> findOrderByIdTool(
		@Annotations.Schema(
			description = "the order ID", name = "orderId"
		)
		long orderId) {

		try {
			Order order = _commerceService.getOrder(orderId);

			if (order == null) {
				return Map.of(
					"error", "Order with ID " + orderId + " not found.");
			}

			return Map.of("order", order);
		}
		catch (Exception exception) {
			return Map.of(
				"error", "Error finding order: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves information from the Frequently Asked Questions (FAQ) based on a query",
		name = "getFaqInformationTool"
	)
	public Map<String, Object> getFaqInformationTool(
		@Annotations.Schema(
			description = "the search query for the FAQ (e.g., 'return policy'). Use 'all' or empty string to see all available topics.",
			name = "query"
		)
		String query) {

		try {
			Map<String, Map<String, String>> faqData = _getFaqData();

			if (StringUtils.isBlank(query) ||
				Strings.CI.equals(query, "all") ||
				Strings.CI.equals(query, "faq") ||
				Strings.CI.equals(query, "general")) {

				List<String> categories = new ArrayList<>();

				for (String categoryKey : faqData.keySet()) {
					categories.add(
						Strings.CS.replace(categoryKey, "_", " "));
				}

				return Map.of("categories", categories);
			}

			String lowerCasedQuery = StringUtils.lowerCase(query);

			List<Map<String, String>> matchingAnswers = new ArrayList<>();

			for (Map.Entry<String, Map<String, String>> entry :
					faqData.entrySet()) {

				String categoryKey = entry.getKey();

				String categoryTitle = Strings.CS.replace(
					categoryKey, "_", " ");

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
								"answer", answerText, "category",
								categoryTitle, "question", questionText));
					}
				}
			}

			if (matchingAnswers.isEmpty()) {
				return Map.of(
					"error",
					"No FAQ results found for query '" + query + "'");
			}

			return Map.of("results", matchingAnswers);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error retrieving FAQ information: " +
					exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves the items contained in a specific order",
		name = "getOrderItemsTool"
	)
	public Map<String, Object> getOrderItemsTool(
		@Annotations.Schema(
			description = "the placed order ID", name = "orderId"
		)
		long orderId) {

		try {
			List<OrderItem> orderItems =
				_commerceService.getPlacedOrderItems(orderId);

			return Map.of("orderItems", orderItems);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error retrieving order items: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves shipping and tracking information for a specific order",
		name = "getOrderShipmentsTool"
	)
	public Map<String, Object> getOrderShipmentsTool(
		@Annotations.Schema(
			description = "the placed order ID", name = "orderId"
		)
		long orderId) {

		try {
			List<Shipment> shipments =
				_commerceService.getOrderShipmentsDto(orderId);

			return Map.of("shipments", shipments);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error retrieving order shipments: " +
					exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Retrieves paginated orders for a specific account",
		name = "searchOrdersByAccountTool"
	)
	public Map<String, Object> searchOrdersByAccountTool(
		@Annotations.Schema(
			description = "the account ID", name = "accountId"
		)
		long accountId,
		@Annotations.Schema(
			description = "the page number starting from 1", name = "page"
		)
		int page,
		@Annotations.Schema(
			description = "the number of items per page", name = "pageSize"
		)
		int pageSize,
		@Annotations.Schema(
			description = "the sort expression (e.g., 'createDate:desc')",
			name = "sort"
		)
		String sort,
		@Annotations.Schema(
			description = "the OData filter expression", name = "filter"
		)
		String filter) {

		try {
			if (page <= 0) {
				page = 1;
			}

			if (pageSize <= 0) {
				pageSize = 20;
			}

			long channelId = _getChannelId();

			PageResult<Order> pageResult =
				_commerceService.getPlacedOrdersByAccount(
					channelId, accountId, page, pageSize, null, sort, filter,
					null);

			return Map.of(
				"lastPage", pageResult.getLastPage(), "orders",
				pageResult.getItems(), "page", page, "totalCount",
				pageResult.getTotalCount());
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error retrieving orders: " + exception.getMessage());
		}
	}

	@Annotations.Schema(
		description = "Lists user associated accounts",
		name = "listAccountsTool"
	)
	public Map<String, Object> listAccountsTool() {
		try {
			String email = SecurityUtils.getEmail();

			UserAccount userAccount = _commerceService.getUserAccountByEmail(
				email);

			if (userAccount == null) {
				return Map.of(
					"error", "No user account found for current user.");
			}

			List<Account> accounts = userAccount.getAccounts();

			if (accounts.isEmpty()) {
				return Map.of("error", "No accounts available.");
			}

			return Map.of("accounts", accounts);
		}
		catch (Exception exception) {
			return Map.of(
				"error",
				"Error listing accounts: " + exception.getMessage());
		}
	}

	private long _getChannelId() {
		SecurityContext securityContext = SecurityContextHolder.getContext();

		Authentication authentication = securityContext.getAuthentication();

		Jwt jwt = (Jwt)authentication.getPrincipal();

		Settings settings = _settingsService.getActiveSettings(jwt);

		return settings.channelId;
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

	private static final Log _log = LogFactory.getLog(CommerceTools2.class);

	private final CommerceService _commerceService;
	private final ObjectMapper _objectMapper;
	private final SettingsService _settingsService;

}
