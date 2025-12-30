/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.service;

import com.liferay.commerce.ai.chat.bot.client.HttpClient;
import com.liferay.commerce.ai.chat.bot.model.Account;
import com.liferay.commerce.ai.chat.bot.model.Channel;
import com.liferay.commerce.ai.chat.bot.model.Order;
import com.liferay.commerce.ai.chat.bot.model.OrderItem;
import com.liferay.commerce.ai.chat.bot.model.PageResult;
import com.liferay.commerce.ai.chat.bot.model.Product;
import com.liferay.commerce.ai.chat.bot.model.Shipment;
import com.liferay.commerce.ai.chat.bot.model.UserAccount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.stereotype.Service;

/**
 * @author Ivica Cardic
 */
@Service
public class CommerceService {

	public CommerceService(HttpClient httpClient) {
		_httpClient = httpClient;
	}

	public List<Account> getAccounts(String channelId, String search) {
		List<Account> accounts = new ArrayList<>();

		JSONArray jsonArray = _getAccountsJSONArray(channelId, search);

		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				Account account = _toAccount(jsonArray.optJSONObject(i));

				if (account != null) {
					accounts.add(account);
				}
			}
		}

		return accounts;
	}

	public List<Order> getAllPlacedOrdersByAccountDto(
		String channelId, String accountId, String search, String sort) {

		List<Order> orders = new ArrayList<>();

		int page = 1;
		int pageSize = 100;

		while (true) {
			PageResult<Order> pageResult = getPlacedOrdersByAccount(
				channelId, accountId, page, pageSize, search, sort, null);

			List<Order> pageResultOrders = pageResult.getItems();

			if (pageResultOrders.isEmpty()) {
				break;
			}

			orders.addAll(pageResultOrders);

			if (page >= pageResult.getLastPage()) {
				break;
			}

			page++;
		}

		return orders;
	}

	public List<Channel> getChannels() {
		List<Channel> channels = new ArrayList<>();

		JSONArray jsonArray = _getAvailableChannelsJSONArray();

		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				Channel channel = _toChannel(jsonArray.optJSONObject(i));

				if (channel != null) {
					channels.add(channel);
				}
			}
		}

		return channels;
	}

	public Order getOrder(String orderId) {
		JSONObject jsonObject = _getOrderJSONObject(orderId);

		return _toOrder(jsonObject);
	}

	public Order getOrderByExternelReferenceCode(String externalReferenceCode) {
		JSONObject jsonObject = _getOrderByExternalReferenceCodeJSONObject(
			externalReferenceCode);

		return _toOrder(jsonObject);
	}

	public List<Shipment> getOrderShipmentsDto(String orderId) {
		List<Shipment> shipments = new ArrayList<>();

		JSONArray jsonArray = _getOrderShipmentsJSONArray(orderId);

		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				Shipment shipment = _toShipment(jsonArray.optJSONObject(i));

				if (shipment != null) {
					shipments.add(shipment);
				}
			}
		}

		return shipments;
	}

	public List<OrderItem> getPlacedOrderItems(String orderId) {
		List<OrderItem> orderItems = new ArrayList<>();

		JSONArray jsonArray = _getPlacedOrderItemsJSONArray(orderId);

		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				OrderItem orderItem = _toOrderItem(jsonArray.optJSONObject(i));

				if (orderItem != null) {
					orderItems.add(orderItem);
				}
			}
		}

		return orderItems;
	}

	public PageResult<Order> getPlacedOrdersByAccount(
		String channelId, String accountId, Integer page, Integer pageSize,
		String search, String sort, String filter) {

		JSONObject ordersJSONObject = _getPlacedOrdersByAccountJSONObject(
			channelId, accountId, page, pageSize, search, sort, filter);

		PageResult<Order> pageResult = new PageResult<>();

		if (ordersJSONObject == null) {
			return pageResult;
		}

		pageResult.setTotalCount(ordersJSONObject.optInt("totalCount", 0));
		pageResult.setLastPage(ordersJSONObject.optInt("lastPage", 1));

		List<Order> items = new ArrayList<>();

		for (JSONObject jsonObject : _getJSONObjects(ordersJSONObject)) {
			Order order = _toOrder(jsonObject);

			if (order != null) {
				items.add(order);
			}
		}

		pageResult.setItems(items);

		return pageResult;
	}

	public PageResult<Product> getProductsByChannelDto(
		String channelId, String accountId, Integer page, Integer pageSize) {

		JSONObject productsJSONObject = _getProductsByChannelJSONObject(
			channelId, accountId, page, pageSize);
		PageResult<Product> pageResult = new PageResult<>();

		if (productsJSONObject == null) {
			return pageResult;
		}

		pageResult.setTotalCount(productsJSONObject.optInt("totalCount", 0));
		pageResult.setLastPage(productsJSONObject.optInt("lastPage", 1));

		List<Product> products = new ArrayList<>();

		for (JSONObject jsonObject : _getJSONObjects(productsJSONObject)) {
			Product product = _toProduct(jsonObject);

			if (product != null) {
				products.add(product);
			}
		}

		pageResult.setItems(products);

		return pageResult;
	}

	public UserAccount getUserAccountByEmail(String email) {
		JSONObject jsonObject = _getUserAccountByEmailJSONObject(email);

		return _toUserAccount(jsonObject);
	}

	private JSONArray _getAccountsJSONArray(String channelId, String search) {
		try {
			String path;

			if (!StringUtils.isEmpty(channelId)) {
				path =
					"/o/headless-commerce-delivery-catalog/v1.0/channels/" +
						channelId + "/accounts";
			}
			else {
				path = "/o/headless-commerce-delivery-catalog/v1.0/accounts";
			}

			JSONObject jsonObject = _httpClient.get(
				path, Map.of("search", search));

			if (jsonObject != null) {
				return jsonObject.optJSONArray("items");
			}

			return new JSONArray();
		}
		catch (Exception exception) {
			_log.error(exception);

			return new JSONArray();
		}
	}

	private JSONArray _getAvailableChannelsJSONArray() {
		try {
			JSONObject jsonObject = _httpClient.get(
				"/o/headless-commerce-delivery-catalog/v1.0/channels");

			if (jsonObject != null) {
				return jsonObject.optJSONArray("items");
			}

			return new JSONArray();
		}
		catch (Exception exception) {
			_log.error(exception);

			return new JSONArray();
		}
	}

	private List<JSONObject> _getJSONObjects(JSONObject jsonObject) {
		if (jsonObject == null) {
			return Collections.emptyList();
		}

		JSONArray jsonArray = jsonObject.optJSONArray("items");

		if (jsonArray == null) {
			return Collections.emptyList();
		}

		List<JSONObject> jsonObjects = new ArrayList<>(jsonArray.length());

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject arrayJSONObject = jsonArray.optJSONObject(i);

			if (arrayJSONObject != null) {
				jsonObjects.add(arrayJSONObject);
			}
		}

		return jsonObjects;
	}

	private JSONObject _getOrderByExternalReferenceCodeJSONObject(
		String externalReferenceCode) {

		try {
			return _httpClient.get(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders" +
					"/by-externalReferenceCode/" + externalReferenceCode);
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return null;
	}

	private JSONObject _getOrderJSONObject(String orderId) {
		try {
			return _httpClient.get(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId);
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return null;
	}

	private JSONArray _getOrderShipmentsJSONArray(String orderId) {
		try {
			JSONObject jsonObject = _httpClient.get(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId + "/shipments");

			if (jsonObject != null) {
				return jsonObject.optJSONArray("items");
			}

			return new JSONArray();
		}
		catch (Exception exception) {
			_log.error(exception);

			return new JSONArray();
		}
	}

	private JSONArray _getPlacedOrderItemsJSONArray(String orderId) {
		try {
			JSONObject jsonObject = _httpClient.get(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId + "/placed-order-items");

			if (jsonObject != null) {
				return jsonObject.optJSONArray("items");
			}

			return new JSONArray();
		}
		catch (Exception exception) {
			_log.error(exception);

			return new JSONArray();
		}
	}

	private JSONObject _getPlacedOrdersByAccountJSONObject(
		String channelId, String accountId, Integer page, Integer pageSize,
		String search, String sort, String filter) {

		try {
			Map<String, String> params = new HashMap<>();

			if (page != null) {
				params.put("page", String.valueOf(page));
			}

			if (pageSize != null) {
				params.put("pageSize", String.valueOf(pageSize));
			}

			if (search != null) {
				params.put("search", search);
			}

			if (!StringUtils.isEmpty(sort)) {
				params.put("sort", sort);
			}

			if (!StringUtils.isEmpty(filter)) {
				params.put("filter", filter);
			}

			String path = String.format(
				"/o/headless-commerce-delivery-order/v1.0/channels/%s" +
					"/accounts/%s/placed-orders",
				channelId, accountId);

			if (accountId == null) {
				path = String.format(
					"/o/headless-commerce-delivery-order/v1.0/channels/%s" +
						"/placed-orders",
					channelId);
			}

			return _httpClient.get(path, params);
		}
		catch (Exception exception) {
			_log.error(exception);

			return null;
		}
	}

	private JSONObject _getProductsByChannelJSONObject(
		String channelId, String accountId, Integer page, Integer pageSize) {

		try {
			Map<String, String> params = new HashMap<>();

			if (page != null) {
				params.put("page", String.valueOf(page));
			}

			if (pageSize != null) {
				params.put("pageSize", String.valueOf(pageSize));
			}

			if (!StringUtils.isEmpty(accountId)) {
				params.put("accountId", accountId);
			}

			return _httpClient.get(
				"/o/headless-commerce-delivery-catalog/v1.0/channels/" +
					channelId + "/products",
				params);
		}
		catch (Exception exception) {
			_log.error(exception);

			return null;
		}
	}

	private double _getSafeDouble(JSONObject jsonObject, String key) {
		if (jsonObject == null) {
			return 0.0;
		}

		Object value = jsonObject.opt(key);

		if (value == null) {
			return 0.0;
		}

		if (value instanceof Number) {
			Number number = (Number)value;

			return number.doubleValue();
		}

		try {
			return Double.parseDouble(String.valueOf(value));
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}

			return 0.0;
		}
	}

	private JSONObject _getUserAccountByEmailJSONObject(String email) {
		try {
			JSONObject jsonObject = _httpClient.get(
				"/o/headless-admin-user/v1.0/user-accounts",
				Map.of("filter", "emailAddress eq '" + email + "'"));

			List<JSONObject> jsonObjects = _getJSONObjects(jsonObject);

			if (jsonObjects.isEmpty()) {
				return null;
			}

			return jsonObjects.get(0);
		}
		catch (Exception exception) {
			_log.error(exception);

			return null;
		}
	}

	private Map<String, String> _jsonObjectToMap(JSONObject jsonObject) {
		Map<String, String> map = new HashMap<>();

		if (jsonObject == null) {
			return map;
		}

		for (String key : jsonObject.keySet()) {
			Object value = jsonObject.opt(key);

			if (value != null) {
				map.put(key, String.valueOf(value));
			}
			else {
				map.put(key, null);
			}
		}

		return map;
	}

	private Date _parseIsoDate(String string) {
		if (StringUtils.isEmpty(string)) {
			return null;
		}

		try {
			return Date.from(
				java.time.Instant.parse(
					StringUtils.replace(string, "Z", "+00:00")));
		}
		catch (Exception exception1) {
			try {
				if (_log.isWarnEnabled()) {
					_log.warn(exception1.getMessage(), exception1);
				}

				return Date.from(java.time.Instant.parse(string));
			}
			catch (Exception exception2) {
				if (_log.isWarnEnabled()) {
					_log.warn(exception2.getMessage(), exception2);
				}

				return null;
			}
		}
	}

	private Account _toAccount(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		Account account = new Account();

		account.setId(String.valueOf(jsonObject.opt("id")));
		account.setName(jsonObject.optString("name", ""));
		account.setType(jsonObject.optString("type", ""));
		account.setStatus(jsonObject.optString("status", ""));

		return account;
	}

	private Channel _toChannel(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		Channel channel = new Channel();

		channel.setId(String.valueOf(jsonObject.opt("id")));
		channel.setName(jsonObject.optString("name", ""));
		channel.setType(jsonObject.optString("type", ""));
		channel.setActive(
			jsonObject.has("active") ? jsonObject.optBoolean("active") : null);

		return channel;
	}

	private Order _toOrder(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		Order order = new Order();

		order.setId(String.valueOf(jsonObject.opt("id")));
		order.setOrderNumber(jsonObject.optString("orderNumber", ""));
		order.setAccountId(String.valueOf(jsonObject.opt("accountId")));
		order.setAccountName(jsonObject.optString("account", ""));
		order.setExternalReferenceCode(
			jsonObject.optString("externalReferenceCode", null));

		JSONObject summaryJSONObject = jsonObject.optJSONObject("summary");

		if (summaryJSONObject != null) {
			order.setItemsQuantity(
				summaryJSONObject.optInt("itemsQuantity", 0));
			order.setSubtotalFormatted(
				summaryJSONObject.optString("subtotalFormatted", null));
			order.setShippingValueFormatted(
				summaryJSONObject.optString("shippingValueFormatted", null));
			order.setShippingDiscountValueFormatted(
				summaryJSONObject.optString(
					"shippingDiscountValueFormatted", null));
			order.setTaxValueFormatted(
				summaryJSONObject.optString("taxValueFormatted", null));
			order.setTotal(_getSafeDouble(jsonObject, "total"));
			order.setTotalFormatted(
				summaryJSONObject.optString("totalFormatted", null));
		}

		JSONObject statusInfoJSONObject = jsonObject.optJSONObject(
			"orderStatusInfo");

		if (statusInfoJSONObject != null) {
			order.setStatus(
				jsonObject.optString(
					"orderStatus",
					statusInfoJSONObject.optString("label_i18n", "")));

			order.setStatusLabel(statusInfoJSONObject.optString("label", ""));

			Object code = statusInfoJSONObject.opt("code");

			if (code != null) {
				order.setStatusCode(String.valueOf(code));
			}
		}

		JSONObject shippingJSONObject = jsonObject.optJSONObject(
			"shippingAddress");

		if (shippingJSONObject != null) {
			order.getShippingAddress(
			).putAll(
				_jsonObjectToMap(shippingJSONObject)
			);
		}

		JSONObject billingJSONObject = jsonObject.optJSONObject(
			"billingAddress");

		if (billingJSONObject != null) {
			order.getBillingAddress(
			).putAll(
				_jsonObjectToMap(billingJSONObject)
			);
		}

		String createDate = jsonObject.optString("createDate", null);

		order.setCreateDate(_parseIsoDate(createDate));
		order.setOrderDate(_parseIsoDate(createDate));

		return order;
	}

	private OrderItem _toOrderItem(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		OrderItem orderItem = new OrderItem();

		orderItem.setId(String.valueOf(jsonObject.opt("id")));
		orderItem.setName(jsonObject.optString("name", ""));
		orderItem.setSku(jsonObject.optString("sku", ""));

		if (jsonObject.has("quantity")) {
			orderItem.setQuantity(jsonObject.optInt("quantity", 0));
		}
		else {
			orderItem.setQuantity(jsonObject.optInt("quantityOrdered", 0));
		}

		JSONObject priceJSONObject = jsonObject.optJSONObject("price");

		orderItem.setUnitPrice(priceJSONObject.optString("priceFormatted"));
		orderItem.setTotalPrice(
			priceJSONObject.optString("finalPriceFormatted"));

		orderItem.setStatus(
			jsonObject.optString(
				"orderItemStatus", jsonObject.optString("status", "")));

		return orderItem;
	}

	private Product _toProduct(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		Product product = new Product();

		product.setId(String.valueOf(jsonObject.opt("id")));
		product.setName(jsonObject.optString("name", ""));
		product.setDescription(jsonObject.optString("description", ""));
		product.setExternalReferenceCode(
			jsonObject.optString("externalReferenceCode", ""));

		return product;
	}

	private Shipment _toShipment(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		Shipment shipment = new Shipment();

		shipment.setId(String.valueOf(jsonObject.opt("id")));
		shipment.setCarrier(jsonObject.optString("carrier", ""));
		shipment.setTrackingNumber(jsonObject.optString("trackingNumber", ""));
		shipment.setShipmentStatus(
			jsonObject.optString(
				"shipmentStatus", jsonObject.optString("status", "")));
		shipment.setOneLineAddress(jsonObject.optString("oneLineAddress", ""));
		shipment.setShippingDate(
			_parseIsoDate(jsonObject.optString("shippingDate", "")));
		shipment.setExpectedDate(
			_parseIsoDate(jsonObject.optString("expectedDate", "")));

		JSONObject statusJSONObject = jsonObject.optJSONObject("status");

		if (statusJSONObject != null) {
			Shipment.Status status = new Shipment.Status();

			status.setLabel(statusJSONObject.optString("label", ""));

			shipment.setStatus(status);
		}

		return shipment;
	}

	private UserAccount _toUserAccount(JSONObject jsonObject) {
		if (jsonObject == null) {
			return null;
		}

		UserAccount userAccount = new UserAccount();

		userAccount.setId(String.valueOf(jsonObject.opt("id")));
		userAccount.setEmail(jsonObject.optString("emailAddress", null));
		userAccount.setFirstName(jsonObject.optString("givenName", ""));
		userAccount.setLastName(jsonObject.optString("familyName", ""));

		String phone = null;

		try {
			JSONArray phonesJSONArray = jsonObject.optJSONArray("phoneNumbers");

			if (phonesJSONArray != null) {
				for (int i = 0; i < phonesJSONArray.length(); i++) {
					JSONObject phoneJSONObject = phonesJSONArray.optJSONObject(
						i);

					if (phoneJSONObject != null) {
						phone = phoneJSONObject.optString("phoneNumber", null);

						break;
					}
				}
			}
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(exception);
			}
		}

		userAccount.setPhone(phone);

		return userAccount;
	}

	private static final Log _log = LogFactory.getLog(CommerceService.class);

	private final HttpClient _httpClient;

}