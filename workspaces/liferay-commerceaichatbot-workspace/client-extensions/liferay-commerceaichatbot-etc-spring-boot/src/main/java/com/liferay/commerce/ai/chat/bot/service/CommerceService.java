package com.liferay.commerce.ai.chat.bot.service;

import com.liferay.commerce.ai.chat.bot.model.UserAccount;
import com.liferay.commerce.ai.chat.bot.model.Order;
import com.liferay.commerce.ai.chat.bot.model.OrderItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URI;

import java.nio.charset.StandardCharsets;

import java.util.*;
import java.util.Base64.Encoder;

import javax.annotation.PostConstruct;

import javax.net.ssl.SSLContext;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommerceService {

	public void authenticate() {
		try {
			URI uri = buildUri(
				"/o/headless-admin-user/v1.0/my-user-account", null);

			JSONObject json = executeGet(uri);

			this.authenticated = json != null;

			if (authenticated) {
				log.info("Authentication successful for user {}", username);
			}
			else {
				log.error("Authentication failed for user {}", username);
			}
		}
		catch (Exception e) {
			this.authenticated = false;
			log.error("Authentication error: {}", e.getMessage(), e);
		}
	}

	public java.util.List<Order> getAllPlacedOrdersByAccountDto(
		String channelId, String accountId) {

		java.util.List<Order> all = new java.util.ArrayList<>();
		int page = 1;
		int pageSize = 100;

		while (true) {
			com.liferay.commerce.ai.chat.bot.model.PageResult<Order> pr =
				getPlacedOrdersByAccount(
					channelId, accountId, page, pageSize, null, null);
			java.util.List<Order> items = pr.getItems();

			if ((items == null) || items.isEmpty())

				break;
			all.addAll(items);

			if (page >= pr.getLastPage())

				break;
			page++;
		}

		return all;
	}

 private JSONArray _getAccounts(String channelId) {
		ensureAuthenticated();

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

			URI uri = buildUri(path, null);

			JSONObject json = executeGet(uri);

			if (json != null) {
				return json.optJSONArray("items");
			}

			return new JSONArray();
		}
		catch (Exception e) {
			log.error("Error retrieving accounts: {}", e.getMessage(), e);

			return new JSONArray();
		}
	}

	public java.util.List<com.liferay.commerce.ai.chat.bot.model.Account>
	getAccounts(String channelId) {

		JSONArray arr = _getAccounts(channelId);
		java.util.List<com.liferay.commerce.ai.chat.bot.model.Account> out =
			new java.util.ArrayList<>();

		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				com.liferay.commerce.ai.chat.bot.model.Account a = toAccount(
					arr.optJSONObject(i));

				if (a != null)
					out.add(a);
			}
		}

		return out;
	}

 private JSONArray _getAvailableChannels() {
		ensureAuthenticated();

		try {
			URI uri = buildUri(
				"/o/headless-commerce-delivery-catalog/v1.0/channels", null);

			JSONObject json = executeGet(uri);

			if (json != null) {
				return json.optJSONArray("items");
			}

			return new JSONArray();
		}
		catch (Exception e) {
			log.error("Error retrieving channels: {}", e.getMessage(), e);

			return new JSONArray();
		}
	}

	public java.util.List<com.liferay.commerce.ai.chat.bot.model.Channel>
	getChannels() {

		JSONArray arr = _getAvailableChannels();
		java.util.List<com.liferay.commerce.ai.chat.bot.model.Channel> out =
			new java.util.ArrayList<>();

		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				com.liferay.commerce.ai.chat.bot.model.Channel c = toChannel(
					arr.optJSONObject(i));

				if (c != null)
					out.add(c);
			}
		}

		return out;
	}

 private JSONObject _getUserAccountByEmail(String email) {
		ensureAuthenticated();

		try {
			Map<String, String> params = new HashMap<>();

			params.put("filter", "emailAddress eq '" + email + "'");
			URI uri = buildUri(
				"/o/headless-admin-user/v1.0/user-accounts", params);

			JSONObject json = executeGet(uri);

			List<JSONObject> items = jsonItems(json);

			if (items.isEmpty()) {
				return null;
			}

			return items.get(0);
		}
		catch (Exception e) {
			log.error(
				"Error retrieving customer by email {}: {}", email,
				e.getMessage(), e);

			return null;
		}
	}

	// ---------- DTO-returning public methods ----------
		
		public UserAccount getUserAccountByEmail(String email) {
			JSONObject obj = _getUserAccountByEmail(email);
		
			return toUserAccount(obj);
		}

	// ----------------- Public API (modeled after liferay_client.py) -----------------

	public Order getOrder(String orderId) {
		JSONObject jsonObject = _getOrder(orderId);

		return toOrder(jsonObject);
	}


	private JSONObject _getOrder(String orderId) {
		ensureAuthenticated();

		try {
			URI uri = buildUri(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId,
				null);

			return executeGet(uri);
		}
		catch (Exception e) {
			log.error(
				"Error retrieving order details {}: {}", orderId,
				e.getMessage(), e);
		}

		return null;
	}

 private JSONArray getOrderShipments(String orderId) {
		ensureAuthenticated();

		try {
			URI uri = buildUri(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId + "/shipments",
				null);

			JSONObject json = executeGet(uri);

			if (json != null) {
				return json.optJSONArray("items");
			}

			return new JSONArray();
		}
		catch (Exception e) {
			log.error(
				"Error retrieving order shipments {}: {}", orderId,
				e.getMessage(), e);

			return new JSONArray();
		}
	}

	public java.util.List<com.liferay.commerce.ai.chat.bot.model.Shipment>
		getOrderShipmentsDto(String orderId) {

		JSONArray arr = getOrderShipments(orderId);
		java.util.List<com.liferay.commerce.ai.chat.bot.model.Shipment> list =
			new java.util.ArrayList<>();

		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				com.liferay.commerce.ai.chat.bot.model.Shipment s = toShipment(
					arr.optJSONObject(i));

				if (s != null)
					list.add(s);
			}
		}

		return list;
	}

	private JSONArray _getPlacedOrderItems(String orderId) {
		ensureAuthenticated();

		try {
			URI uri = buildUri(
				"/o/headless-commerce-delivery-order/v1.0/placed-orders/" +
					orderId + "/placed-order-items",
				null);

			JSONObject json = executeGet(uri);

			if (json != null) {
				return json.optJSONArray("items");
			}

			return new JSONArray();
		}
		catch (Exception e) {
			log.error(
				"Error retrieving order items {}: {}", orderId, e.getMessage(),
				e);

			return new JSONArray();
		}
	}

	public List<OrderItem> getPlacedOrderItems(String orderId) {
		JSONArray itemsArr = _getPlacedOrderItems(orderId);
		List<OrderItem> list = new ArrayList<>();

		if (itemsArr != null) {
			for (int i = 0; i < itemsArr.length(); i++) {
				OrderItem it = toOrderItem(itemsArr.optJSONObject(i));

				if (it != null)
					list.add(it);
			}
		}

		return list;
	}

 private JSONObject _getPlacedOrdersByAccount(
		String channelId, String accountId, Integer page, Integer pageSize,
		String sort, String filter) {

		ensureAuthenticated();

		try {
			Map<String, String> params = new HashMap<>();

			if (page != null)
				params.put("page", String.valueOf(page));

			if (pageSize != null)
				params.put("pageSize", String.valueOf(pageSize));

			if ((sort != null) && !sort.isEmpty())
				params.put("sort", sort);

			if ((filter != null) && !filter.isEmpty())
				params.put("filter", filter);
			String path = String.format(
				"/o/headless-commerce-delivery-order/v1.0/channels/%s/accounts/%s/placed-orders",
				channelId, accountId);

			URI uri = buildUri(path, params);

			return executeGet(uri);
		}
		catch (Exception e) {
			log.error("Error retrieving placed orders: {}", e.getMessage(), e);

			return null;
		}
	}

	public com.liferay.commerce.ai.chat.bot.model.PageResult<Order>
	getPlacedOrdersByAccount(
			String channelId, String accountId, Integer page, Integer pageSize,
			String sort, String filter) {

		JSONObject json = _getPlacedOrdersByAccount(
			channelId, accountId, page, pageSize, sort, filter);
		com.liferay.commerce.ai.chat.bot.model.PageResult<Order> pr =
			new com.liferay.commerce.ai.chat.bot.model.PageResult<>();

		if (json == null)

			return pr;
		pr.setTotalCount(json.optInt("totalCount", 0));
		pr.setLastPage(json.optInt("lastPage", 1));
		java.util.List<Order> items = new java.util.ArrayList<>();

		for (JSONObject obj : jsonItems(json)) {
			Order o = toOrder(obj);

			if (o != null)
				items.add(o);
		}

		pr.setItems(items);

		return pr;
	}

 private JSONObject getProductsByChannel(
		String channelId, String accountId, Integer page, Integer pageSize) {

		ensureAuthenticated();

		try {
			Map<String, String> params = new HashMap<>();

			if (page != null)
				params.put("page", String.valueOf(page));

			if (pageSize != null)
				params.put("pageSize", String.valueOf(pageSize));

			if ((accountId != null) && !accountId.isEmpty())
				params.put("accountId", accountId);
			URI uri = buildUri(
				"/o/headless-commerce-delivery-catalog/v1.0/channels/" +
					channelId + "/products",
				params);

			return executeGet(uri);
		}
		catch (Exception e) {
			log.error("Error retrieving products: {}", e.getMessage(), e);

			return null;
		}
	}

	public com.liferay.commerce.ai.chat.bot.model.PageResult
		<com.liferay.commerce.ai.chat.bot.model.Product>
			getProductsByChannelDto(
				String channelId, String accountId, Integer page,
				Integer pageSize) {

		JSONObject json = getProductsByChannel(
			channelId, accountId, page, pageSize);
		com.liferay.commerce.ai.chat.bot.model.PageResult
			<com.liferay.commerce.ai.chat.bot.model.Product> pr =
				new com.liferay.commerce.ai.chat.bot.model.PageResult<>();

		if (json == null)

			return pr;
		pr.setTotalCount(json.optInt("totalCount", 0));
		pr.setLastPage(json.optInt("lastPage", 1));
		java.util.List<com.liferay.commerce.ai.chat.bot.model.Product> items =
			new java.util.ArrayList<>();

		for (JSONObject obj : jsonItems(json)) {
			com.liferay.commerce.ai.chat.bot.model.Product p = toProduct(obj);

			if (p != null)
				items.add(p);
		}

		pr.setItems(items);

		return pr;
	}

	@PostConstruct
	public void init() {
		this.basicAuthHeader = buildBasicAuthHeader(username, password);
		this.httpClient = buildHttpClient(sslVerify, timeoutMs);
		log.info(
			"Liferay CommerceService initialized. baseUrl={} sslVerify={} timeoutMs={}",
			baseUrl, sslVerify, timeoutMs);
	}

	// ----------------- Low-level HTTP helpers -----------------

	private JSONObject executeGet(URI uri) throws Exception {
		HttpGet get = new HttpGet(uri);

		get.addHeader(
			HttpHeaders.USER_AGENT, "Liferay-Customer-Service-Agent/1.0");
		get.addHeader(HttpHeaders.ACCEPT, "application/json");
		get.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		get.addHeader(HttpHeaders.AUTHORIZATION, basicAuthHeader);

		try (CloseableHttpResponse response = httpClient.execute(get)) {
			int status = response.getStatusLine(
			).getStatusCode();

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(
						response.getEntity(
						).getContent(),
						StandardCharsets.UTF_8))) {

				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null)
					sb.append(line);
				String body = sb.toString();

				if ((status >= 200) && (status < 300)) {
					if ((body == null) || body.isEmpty())

						return new JSONObject();

					return new JSONObject(body);
				}

				log.warn("GET {} -> {} body: {}", uri, status, truncate(body));

				return null;
			}
		}
	}

	private String buildBasicAuthHeader(String user, String pass) {
		Encoder encoder = Base64.getEncoder();

		String token = encoder.encodeToString(
			(user + ":" + pass).getBytes(StandardCharsets.UTF_8));

		return "Basic " + token;
	}

	private CloseableHttpClient buildHttpClient(
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

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslContext, NoopHostnameVerifier.INSTANCE);

			return HttpClients.custom(
			).setSSLSocketFactory(
				sslsf
			).setDefaultRequestConfig(
				requestConfig
			).build();
		}
		catch (Exception e) {
			log.warn(
				"Failed to create insecure SSL context, falling back to default client",
				e);

			return HttpClients.custom(
			).setDefaultRequestConfig(
				requestConfig
			).build();
		}
	}

	private List<JSONObject> jsonItems(JSONObject json) {
		if (json == null)

			return Collections.emptyList();
		JSONArray arr = json.optJSONArray("items");

		if (arr == null)

			return Collections.emptyList();
		List<JSONObject> list = new ArrayList<>(arr.length());

		for (int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.optJSONObject(i);

			if (obj != null)
				list.add(obj);
		}

		return list;
	}

	// ----------------- DTO Mappers and DTO-returning API -----------------

	/** Convert a JSONObject user into a Customer DTO. */
	private UserAccount toUserAccount(JSONObject user) {
		if (user == null)

			return null;
		UserAccount c = new UserAccount();

		c.setId(String.valueOf(user.opt("id")));
		c.setEmail(user.optString("emailAddress", null));
		c.setFirstName(user.optString("givenName", ""));
		c.setLastName(user.optString("familyName", ""));

		// phoneNumbers is an array; take first if present

		String phone = null;

		try {
			JSONArray phones = user.optJSONArray("phoneNumbers");

			if ((phones != null) && (phones.length() > 0)) {
				JSONObject p0 = phones.optJSONObject(0);

				if (p0 != null)
					phone = p0.optString("phoneNumber", null);
			}
		}
		catch (Exception ignored) {
		}

		c.setPhone(phone);

		// address mapping not available in current payloads; keep empty map

		return c;
	}

	private URI buildUri(String path, Map<String, String> params)
		throws Exception {

		String trimmedBase =
			baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) :
				baseUrl;
		String trimmedPath = path.startsWith("/") ? path : "/" + path;

		URIBuilder builder = new URIBuilder(trimmedBase + trimmedPath);

		if (params != null) {
			for (Map.Entry<String, String> e : params.entrySet()) {
				if (e.getValue() != null)
					builder.addParameter(e.getKey(), e.getValue());
			}
		}

		return builder.build();
	}

	private void ensureAuthenticated() {
		if (!authenticated) {
			authenticate();
		}
	}

	private Map<String, String> objToMap(JSONObject obj) {
		Map<String, String> map = new HashMap<>();

		if (obj == null)

			return map;

		for (String key : obj.keySet()) {
			Object v = obj.opt(key);

			map.put(key, v == null ? null : String.valueOf(v));
		}

		return map;
	}

	private java.time.OffsetDateTime parseIsoDate(String s) {
		if ((s == null) || s.isEmpty())

			return null;

		try {

			// Accept both with and without trailing Z

			return java.time.OffsetDateTime.parse(s.replace("Z", "+00:00"));
		}
		catch (Exception e) {
			try {
				return java.time.OffsetDateTime.parse(s);
			}
			catch (Exception ignored) {
				return null;
			}
		}
	}

	// ---------- Helper mappers & utils for DTOs ----------
		
		private com.liferay.commerce.ai.chat.bot.model.Account toAccount(JSONObject acc) {
			if (acc == null) return null;
			com.liferay.commerce.ai.chat.bot.model.Account a = new com.liferay.commerce.ai.chat.bot.model.Account();
			a.setId(String.valueOf(acc.opt("id")));
			a.setName(acc.optString("name", ""));
			a.setType(acc.optString("type", ""));
			a.setStatus(acc.optString("status", ""));
			return a;
		}
		
		private com.liferay.commerce.ai.chat.bot.model.Product toProduct(JSONObject p) {
			if (p == null) return null;
			com.liferay.commerce.ai.chat.bot.model.Product pr = new com.liferay.commerce.ai.chat.bot.model.Product();
			pr.setId(String.valueOf(p.opt("id")));
			pr.setName(p.optString("name", ""));
			pr.setDescription(p.optString("description", ""));
			pr.setExternalReferenceCode(p.optString("externalReferenceCode", ""));
			return pr;
		}
		
		private OrderItem toOrderItem(JSONObject it) {
			if (it == null) return null;
			OrderItem oi = new OrderItem();
			oi.setId(String.valueOf(it.opt("id")));
			oi.setName(it.optString("name", ""));
			oi.setSku(it.optString("sku", ""));
			oi.setQuantity(it.has("quantity") ? it.optInt("quantity", 0) : it.optInt("quantityOrdered", 0));
			oi.setUnitPrice(safeDouble(it, "unitPrice"));
			oi.setTotalPrice(safeDouble(it, "totalPrice"));
			oi.setStatus(it.optString("orderItemStatus", it.optString("status", "")));
			return oi;
		}
		
		private double safeDouble(JSONObject obj, String key) {
			if (obj == null) return 0.0d;
			Object v = obj.opt(key);
			if (v == null) return 0.0d;
			if (v instanceof Number) return ((Number) v).doubleValue();
			try { return Double.parseDouble(String.valueOf(v)); } catch (Exception ignored) { return 0.0d; }
		}
		
		private String truncate(String s) {
		if (s == null)

			return null;

		return s.length() > 500 ? s.substring(0, 500) + "..." : s;
	}

	/** Convert a JSONObject order into an Order DTO. */
	private Order toOrder(JSONObject order) {
		if (order == null)

			return null;
		Order o = new Order();

		o.setId(String.valueOf(order.opt("id")));
		o.setOrderNumber(order.optString("orderNumber", ""));
		o.setAccountId(String.valueOf(order.opt("accountId")));
		o.setAccountName(order.optString("account", ""));
		o.setStatus(
			order.optString("orderStatus", order.optString("status", "")));

			// Merge extra details formerly in OrderDetails
			o.setExternalReferenceCode(order.optString("externalReferenceCode", null));

			JSONObject summaryJSONObject = order.optJSONObject("summary");

			if (summaryJSONObject != null) {
				o.setItemsQuantity(summaryJSONObject.optInt("itemsQuantity", 0));
				o.setSubtotalFormatted(summaryJSONObject.optString("subtotalFormatted", null));
				o.setShippingValueFormatted(summaryJSONObject.optString("shippingValueFormatted", null));
				o.setShippingDiscountValueFormatted(summaryJSONObject.optString("shippingDiscountValueFormatted", null));
				o.setTaxValueFormatted(summaryJSONObject.optString("taxValueFormatted", null));
				o.setTotal(safeDouble(order, "total"));
				o.setTotalFormatted(summaryJSONObject.optString("totalFormatted", null));
			}

			JSONObject statusInfo = order.optJSONObject("orderStatusInfo");
			if (statusInfo != null) {
				o.setStatusLabel(statusInfo.optString("label", ""));
				Object code = statusInfo.opt("code");
				o.setStatusCode(code == null ? null : String.valueOf(code));
			}

		// addresses if available

		JSONObject ship = order.optJSONObject("shippingAddress");

		if (ship != null)
			o.getShippingAddress(
			).putAll(
				objToMap(ship)
			);
		JSONObject bill = order.optJSONObject("billingAddress");

		if (bill != null)
			o.getBillingAddress(
			).putAll(
				objToMap(bill)
			);

		// date

		String createDate = order.optString("createDate", null);
		o.setCreateDate(createDate);
		o.setOrderDate(parseIsoDate(createDate));

		return o;
	}

	// ----- Additional DTOs: Channel, Account, Product, Shipment, PageResult wrappers -----

	private com.liferay.commerce.ai.chat.bot.model.Channel toChannel(
		JSONObject ch) {

		if (ch == null)

			return null;
		com.liferay.commerce.ai.chat.bot.model.Channel c =
			new com.liferay.commerce.ai.chat.bot.model.Channel();
		c.setId(String.valueOf(ch.opt("id")));
		c.setName(ch.optString("name", ""));
		c.setType(ch.optString("type", ""));
		c.setActive(ch.has("active") ? ch.optBoolean("active") : null);

		return c;
	}

	private com.liferay.commerce.ai.chat.bot.model.Shipment toShipment(
		JSONObject s) {

		if (s == null)

			return null;
		com.liferay.commerce.ai.chat.bot.model.Shipment sh =
			new com.liferay.commerce.ai.chat.bot.model.Shipment();
		sh.setId(String.valueOf(s.opt("id")));
		sh.setCarrier(s.optString("carrier", ""));
		sh.setTrackingNumber(s.optString("trackingNumber", ""));
		sh.setShipmentStatus(
			s.optString("shipmentStatus", s.optString("status", "")));
		sh.setOneLineAddress(s.optString("oneLineAddress", ""));
		sh.setShippingDate(s.optString("shippingDate", ""));
		sh.setExpectedDate(s.optString("expectedDate", ""));

		JSONObject statusObj = s.optJSONObject("status");
		if (statusObj != null) {
			com.liferay.commerce.ai.chat.bot.model.Shipment.Status status =
				new com.liferay.commerce.ai.chat.bot.model.Shipment.Status();
			status.setLabel(statusObj.optString("label", ""));
			sh.setStatus(status);
		}

		return sh;
	}

	private static final Logger log = LoggerFactory.getLogger(
		CommerceService.class);

	private volatile boolean authenticated;

	@Value(
		"${liferay.base.url:${LIFERAY_BASE_URL:https://webserver-lct66degrees-uat.lfr.cloud/}}"
	)
	private String baseUrl;

	private String basicAuthHeader;
	private CloseableHttpClient httpClient;

	@Value("${liferay.password:${LIFERAY_PASSWORD:test}}")
	private String password;

	@Value("${liferay.ssl.verify:${LIFERAY_SSL_VERIFY:true}}")
	private boolean sslVerify;

	@Value("${liferay.timeout.ms:${LIFERAY_TIMEOUT:30000}}")
	private int timeoutMs;

	@Value("${liferay.username:${LIFERAY_USERNAME:test@liferay.com}}")
	private String username;

}