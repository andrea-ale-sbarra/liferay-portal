/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.service;

import com.liferay.commerce.ai.chat.bot.model.Account;
import com.liferay.commerce.ai.chat.bot.model.Channel;
import com.liferay.commerce.ai.chat.bot.model.Order;
import com.liferay.commerce.ai.chat.bot.model.OrderItem;
import com.liferay.commerce.ai.chat.bot.model.PageResult;
import com.liferay.commerce.ai.chat.bot.model.Product;
import com.liferay.commerce.ai.chat.bot.model.Shipment;
import com.liferay.commerce.ai.chat.bot.model.UserAccount;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URI;

import java.nio.charset.StandardCharsets;

import java.time.Instant;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import javax.net.ssl.SSLContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Ivica Cardic
 */
@Service
public class CommerceService {

	public void authenticate() {
		try {
			URI uri = _buildUri(
				"/o/headless-admin-user/v1.0/my-user-account", null);

			JSONObject jsonObject = _executeGetJSONObject(uri);

			if (jsonObject != null) {
				_authenticated = true;
			}

			if (_log.isInfoEnabled()) {
				if (_authenticated) {
					_log.info(
						"Authentication successful for user " + _userName);
				}
				else {
					_log.error("Authentication failed for user " + _userName);
				}
			}
		}
		catch (Exception exception) {
			_authenticated = false;
			_log.error(exception);
		}
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

			if (ListUtil.isEmpty(pageResultOrders)) {
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

	@PostConstruct
	public void init() {
		_basicAuthHeader = _buildBasicAuthHeader(_userName, _password);
		_closeableHttpClient = _buildHttpClient(_sslVerify, _timeoutMs);

		if (_log.isInfoEnabled()) {
			_log.info(
				new StringBuilder(
				).append(
					"Liferay CommerceService initialized. baseUrl="
				).append(
					_baseUrl
				).append(
					"sslVerify="
				).append(
					_sslVerify
				).append(
					" timeoutMs="
				).append(
					_timeoutMs
				).toString());
		}
	}

	private String _buildBasicAuthHeader(String user, String pass) {
		Base64.Encoder encoder = Base64.getEncoder();

		String credentials = user + ":" + pass;

		String token = encoder.encodeToString(
			credentials.getBytes(StandardCharsets.UTF_8));

		return "Basic " + token;
	}

	private CloseableHttpClient _buildHttpClient(
		boolean verifySsl, int timeoutMs) {

		RequestConfig requestConfig = RequestConfig.custom(
		).setConnectTimeout(
			timeoutMs
		).setConnectionRequestTimeout(
			timeoutMs
		).setSocketTimeout(
			timeoutMs
		).build();

		if (verifySsl) {
			return HttpClients.custom(
			).setDefaultRequestConfig(
				requestConfig
			).build();
		}

		try {
			SSLContext sslContext = SSLContexts.custom(
			).loadTrustMaterial(
				null, (chain, authType) -> true
			).build();

			SSLConnectionSocketFactory sslConnectionSocketFactory =
				new SSLConnectionSocketFactory(
					sslContext, NoopHostnameVerifier.INSTANCE);

			return HttpClients.custom(
			).setSSLSocketFactory(
				sslConnectionSocketFactory
			).setDefaultRequestConfig(
				requestConfig
			).build();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Failed to create insecure SSL context, falling back to " +
						"default client",
					exception);
			}

			return HttpClients.custom(
			).setDefaultRequestConfig(
				requestConfig
			).build();
		}
	}

	private URI _buildUri(String path, Map<String, String> params)
		throws Exception {

		String trimmedBase = _baseUrl;

		if (_baseUrl.endsWith("/")) {
			trimmedBase = _baseUrl.substring(0, _baseUrl.length() - 1);
		}

		String trimmedPath = path;

		if (!path.startsWith("/")) {
			trimmedPath = "/" + path;
		}

		URIBuilder builder = new URIBuilder(trimmedBase + trimmedPath);

		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (entry.getValue() != null) {
					builder.addParameter(entry.getKey(), entry.getValue());
				}
			}
		}

		return builder.build();
	}

	private void _ensureAuthenticated() {
		if (!_authenticated) {
			authenticate();
		}
	}

	private JSONObject _executeGetJSONObject(URI uri) throws Exception {
		HttpGet get = new HttpGet(uri);

		get.addHeader(
			HttpHeaders.USER_AGENT, "Liferay-Customer-Service-Agent/1.0");
		get.addHeader(HttpHeaders.ACCEPT, "application/json");
		get.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		get.addHeader(HttpHeaders.AUTHORIZATION, _basicAuthHeader);

		try (CloseableHttpResponse response = _closeableHttpClient.execute(
				get)) {

			int status = response.getStatusLine(
			).getStatusCode();

			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(
						response.getEntity(
						).getContent(),
						StandardCharsets.UTF_8))) {

				StringBuilder sb = new StringBuilder();

				String line;

				while ((line = bufferedReader.readLine()) != null) {
					sb.append(line);
				}

				String body = sb.toString();

				if ((status >= 200) && (status < 300)) {
					if ((body == null) || body.isEmpty()) {
						return new JSONObject();
					}

					return new JSONObject(body);
				}

				if (_log.isWarnEnabled()) {
					_log.warn(
						new StringBuilder(
						).append(
							"GET "
						).append(
							uri
						).append(
							" -> "
						).append(
							status
						).append(
							"body: "
						).append(
							_truncate(body)
						).toString());
				}

				return null;
			}
		}
	}

	private JSONArray _getAccountsJSONArray(String channelId, String search) {
		_ensureAuthenticated();

		try {
			String path;

			if ((channelId != null) && !channelId.isEmpty()) {
				path =
					"/o/headless-commerce-delivery-catalog/v1.0/channels/" +
						channelId + "/accounts";
			}
			else {
				path = "/o/headless-commerce-delivery-catalog/v1.0/accounts";
			}

			URI uri = _buildUri(
				path,
				HashMapBuilder.put(
					"search", search
				).build());

			JSONObject jsonObject = _executeGetJSONObject(uri);

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
		_ensureAuthenticated();

		try {
			URI uri = _buildUri(
				"/o/headless-commerce-delivery-catalog/v1.0/channels", null);

			JSONObject jsonObject = _executeGetJSONObject(uri);

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

		_ensureAuthenticated();

		try {
			URI uri = _buildUri(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders" +
					"/by-externalReferenceCode/" + externalReferenceCode,
				null);

			return _executeGetJSONObject(uri);
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return null;
	}

	private JSONObject _getOrderJSONObject(String orderId) {
		_ensureAuthenticated();

		try {
			URI uri = _buildUri(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId,
				null);

			return _executeGetJSONObject(uri);
		}
		catch (Exception exception) {
			_log.error(exception);
		}

		return null;
	}

	private JSONArray _getOrderShipmentsJSONArray(String orderId) {
		_ensureAuthenticated();

		try {
			URI uri = _buildUri(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId + "/shipments",
				null);

			JSONObject jsonObject = _executeGetJSONObject(uri);

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
		_ensureAuthenticated();

		try {
			URI uri = _buildUri(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId + "/placed-order-items",
				null);

			JSONObject jsonObject = _executeGetJSONObject(uri);

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

		_ensureAuthenticated();

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

			if ((sort != null) && !sort.isEmpty()) {
				params.put("sort", sort);
			}

			if ((filter != null) && !filter.isEmpty()) {
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

			URI uri = _buildUri(path, params);

			return _executeGetJSONObject(uri);
		}
		catch (Exception exception) {
			_log.error(exception);

			return null;
		}
	}

	private JSONObject _getProductsByChannelJSONObject(
		String channelId, String accountId, Integer page, Integer pageSize) {

		_ensureAuthenticated();

		try {
			Map<String, String> params = new HashMap<>();

			if (page != null) {
				params.put("page", String.valueOf(page));
			}

			if (pageSize != null) {
				params.put("pageSize", String.valueOf(pageSize));
			}

			if ((accountId != null) && !accountId.isEmpty()) {
				params.put("accountId", accountId);
			}

			URI uri = _buildUri(
				"/o/headless-commerce-delivery-catalog/v1.0/channels/" +
					channelId + "/products",
				params);

			return _executeGetJSONObject(uri);
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
		_ensureAuthenticated();

		try {
			URI uri = _buildUri(
				"/o/headless-admin-user/v1.0/user-accounts",
				HashMapBuilder.put(
					"filter", "emailAddress eq '" + email + "'"
				).build());

			JSONObject jsonObject = _executeGetJSONObject(uri);

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

	private Instant _parseIsoDate(String string) {
		if ((string == null) || string.isEmpty()) {
			return null;
		}

		try {
			return Instant.parse(StringUtil.replace(string, 'Z', "+00:00"));
		}
		catch (Exception exception1) {
			try {
				if (_log.isWarnEnabled()) {
					_log.warn(exception1);
				}

				return Instant.parse(string);
			}
			catch (Exception exception2) {
				if (_log.isWarnEnabled()) {
					_log.warn(exception2);
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

			if ((phonesJSONArray != null) && (phonesJSONArray.length() > 0)) {
				JSONObject phoneJSONObject = phonesJSONArray.optJSONObject(0);

				if (phoneJSONObject != null) {
					phone = phoneJSONObject.optString("phoneNumber", null);
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

	private String _truncate(String s) {
		if (s == null) {
			return null;
		}

		if (s.length() > 500) {
			return s.substring(0, 500) + "...";
		}

		return s;
	}

	private static final Log _log = LogFactory.getLog(CommerceService.class);

	private volatile boolean _authenticated;

	@Value(
		"${liferay.base.url:${LIFERAY_BASE_URL:https://webserver-lct66degrees-uat.lfr.cloud/}}"
	)
	private String _baseUrl;

	private String _basicAuthHeader;
	private CloseableHttpClient _closeableHttpClient;

	@Value("${liferay.password:${LIFERAY_PASSWORD:test}}")
	private String _password;

	@Value("${liferay.ssl.verify:${LIFERAY_SSL_VERIFY:true}}")
	private boolean _sslVerify;

	@Value("${liferay.timeout.ms:${LIFERAY_TIMEOUT:30000}}")
	private int _timeoutMs;

	@Value("${liferay.username:${LIFERAY_USERNAME:test@liferay.com}}")
	private String _userName;

}