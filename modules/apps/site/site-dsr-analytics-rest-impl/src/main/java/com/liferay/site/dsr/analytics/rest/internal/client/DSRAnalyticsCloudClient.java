/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.internal.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.liferay.analytics.settings.configuration.AnalyticsConfiguration;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.site.dsr.analytics.rest.dto.v1_0.DocumentMetricsPage;
import com.liferay.site.dsr.analytics.rest.dto.v1_0.EventsPage;
import com.liferay.site.dsr.analytics.rest.dto.v1_0.MostActiveVisitorsPage;
import com.liferay.site.dsr.analytics.rest.dto.v1_0.SiteHistogramMetric;
import com.liferay.site.dsr.analytics.rest.dto.v1_0.UserSessionsPage;
import com.liferay.site.dsr.analytics.rest.dto.v1_0.VisitFrequency;

import java.net.HttpURLConnection;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Gianmarco Brunialti
 */
public class DSRAnalyticsCloudClient {

	public DSRAnalyticsCloudClient(Http http) {
		_http = http;
	}

	public DocumentMetricsPage getDocumentMetricsPage(
			AnalyticsConfiguration analyticsConfiguration, String channelId,
			String keywords, String rangeEnd, Integer rangeKey,
			String rangeStart, Integer size, String sortColumn, String sortType,
			Integer start)
		throws Exception {

		ObjectNode variables = _objectMapper.createObjectNode();

		variables.put("channelId", channelId);
		variables.put("keywords", keywords);
		_putNullable(variables, "rangeEnd", rangeEnd);
		_putNullable(variables, "rangeKey", rangeKey);
		_putNullable(variables, "rangeStart", rangeStart);
		variables.put("size", size);
		variables.put("start", start);

		ObjectNode sort = variables.putObject("sort");

		sort.put("column", sortColumn);
		sort.put("type", sortType);

		JsonNode data = _executeQuery(
			analyticsConfiguration, _QUERY_DOCUMENTS, variables, "documents");

		_renameKey(data, "documentMetrics", "assetMetrics");

		return _objectMapper.treeToValue(data, DocumentMetricsPage.class);
	}

	public EventsPage getEventsPage(
			AnalyticsConfiguration analyticsConfiguration, String channelId,
			Boolean includeAnonymousUsers, String individualId, String keywords,
			Integer page, String rangeEnd, Integer rangeKey, String rangeStart,
			Integer size)
		throws Exception {

		ObjectNode variables = _objectMapper.createObjectNode();

		variables.put("channelId", channelId);
		_putNullable(variables, "includeAnonymousUsers", includeAnonymousUsers);
		_putNullable(variables, "individualId", individualId);
		_putNullable(variables, "keywords", keywords);
		variables.put("page", page);
		_putNullable(variables, "rangeEnd", rangeEnd);
		_putNullable(variables, "rangeKey", rangeKey);
		_putNullable(variables, "rangeStart", rangeStart);
		variables.put("size", size);

		JsonNode data = _executeQuery(
			analyticsConfiguration, _QUERY_EVENTS, variables, "events");

		_renameKey(data, "eventEntries", "events");

		return _objectMapper.treeToValue(data, EventsPage.class);
	}

	public MostActiveVisitorsPage getMostActiveVisitorsPage(
			AnalyticsConfiguration analyticsConfiguration, String channelId,
			Integer rangeKey, Integer size, Integer start)
		throws Exception {

		ObjectNode variables = _objectMapper.createObjectNode();

		variables.put("channelId", channelId);
		_putNullable(variables, "rangeKey", rangeKey);
		variables.put("size", size);
		variables.put("start", start);

		JsonNode data = _executeQuery(
			analyticsConfiguration, _QUERY_MOST_ACTIVE_VISITORS, variables,
			"mostActiveVisitors");

		return _objectMapper.treeToValue(data, MostActiveVisitorsPage.class);
	}

	public SiteHistogramMetric getSiteSessionsMetric(
			AnalyticsConfiguration analyticsConfiguration, String channelId,
			String[] emailAddresses, String interval, String rangeEnd,
			Integer rangeKey, String rangeStart)
		throws Exception {

		ObjectNode variables = _objectMapper.createObjectNode();

		_putNullable(variables, "channelId", channelId);

		ArrayNode emailAddressesNode = variables.putArray("emailAddresses");

		if (emailAddresses != null) {
			for (String emailAddress : emailAddresses) {
				emailAddressesNode.add(emailAddress);
			}
		}

		variables.put("interval", interval);
		_putNullable(variables, "rangeEnd", rangeEnd);
		_putNullable(variables, "rangeKey", rangeKey);
		_putNullable(variables, "rangeStart", rangeStart);

		JsonNode data = _executeQuery(
			analyticsConfiguration, _QUERY_SITE_SESSIONS_METRIC, variables,
			"site");

		return _toSiteHistogramMetric(data, "sessionsMetric");
	}

	public SiteHistogramMetric getSiteVisitorsMetric(
			AnalyticsConfiguration analyticsConfiguration, String channelId,
			String interval, String rangeEnd, Integer rangeKey,
			String rangeStart)
		throws Exception {

		ObjectNode variables = _objectMapper.createObjectNode();

		_putNullable(variables, "channelId", channelId);
		variables.put("interval", interval);
		_putNullable(variables, "rangeEnd", rangeEnd);
		_putNullable(variables, "rangeKey", rangeKey);
		_putNullable(variables, "rangeStart", rangeStart);

		JsonNode data = _executeQuery(
			analyticsConfiguration, _QUERY_SITE_VISITORS_METRIC, variables,
			"site");

		return _toSiteHistogramMetric(data, "visitorsMetric");
	}

	public UserSessionsPage getUserSessionsPage(
			AnalyticsConfiguration analyticsConfiguration, String channelId,
			String entityType, String keywords, Integer page, String rangeEnd,
			Integer rangeKey, String rangeStart, Integer size)
		throws Exception {

		ObjectNode variables = _objectMapper.createObjectNode();

		variables.put("channelId", channelId);
		variables.put("entityType", entityType);
		_putNullable(variables, "keywords", keywords);
		variables.put("page", page);
		_putNullable(variables, "rangeEnd", rangeEnd);
		_putNullable(variables, "rangeKey", rangeKey);
		_putNullable(variables, "rangeStart", rangeStart);
		variables.put("size", size);

		JsonNode data = _executeQuery(
			analyticsConfiguration, _QUERY_EVENTS_BY_USER_SESSIONS, variables,
			"eventsByUserSessions");

		_renameKey(data, "userSessionEvents", "events");

		return _objectMapper.treeToValue(data, UserSessionsPage.class);
	}

	public VisitFrequency getVisitFrequency(
			AnalyticsConfiguration analyticsConfiguration, String channelId,
			Integer rangeKey)
		throws Exception {

		ObjectNode variables = _objectMapper.createObjectNode();

		variables.put("channelId", channelId);
		_putNullable(variables, "rangeKey", rangeKey);

		JsonNode data = _executeQuery(
			analyticsConfiguration, _QUERY_VISIT_FREQUENCY, variables,
			"visitFrequency");

		_renameKey(data, "visitFrequencyItems", "visitFrequency");

		return _objectMapper.treeToValue(data, VisitFrequency.class);
	}

	private JsonNode _executeQuery(
			AnalyticsConfiguration analyticsConfiguration, String query,
			ObjectNode variables, String rootField)
		throws Exception {

		try {
			ObjectNode body = _objectMapper.createObjectNode();

			body.put("query", query);
			body.set("variables", variables);

			Http.Options options = new Http.Options();

			options.addHeader("Content-Type", "application/json");
			options.addHeader(
				"OSB-Asah-Data-Source-ID",
				analyticsConfiguration.liferayAnalyticsDataSourceId());
			options.addHeader(
				"OSB-Asah-Faro-Backend-Security-Signature",
				analyticsConfiguration.
					liferayAnalyticsFaroBackendSecuritySignature());
			options.addHeader(
				"OSB-Asah-Project-ID",
				analyticsConfiguration.liferayAnalyticsProjectId());

			options.setBody(
				_objectMapper.writeValueAsString(body), "application/json",
				"UTF-8");
			options.setLocation(
				analyticsConfiguration.liferayAnalyticsFaroBackendURL() +
					"/graphql");
			options.setPost(true);

			String content = _http.URLtoString(options);

			Http.Response response = options.getResponse();

			if (response.getResponseCode() != HttpURLConnection.HTTP_OK) {
				if (_log.isDebugEnabled()) {
					_log.debug("Response code " + response.getResponseCode());
				}

				throw new PortalException(
					"Unable to execute DSR analytics query " + rootField);
			}

			JsonNode root = _objectMapper.readTree(content);

			return root.path("data").path(rootField);
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}

			throw new PortalException(
				"Unable to execute DSR analytics query " + rootField,
				exception);
		}
	}

	private void _putNullable(ObjectNode node, String key, Integer value) {
		if (value == null) {
			node.putNull(key);
		}
		else {
			node.put(key, value);
		}
	}

	private void _putNullable(ObjectNode node, String key, Boolean value) {
		if (value == null) {
			node.putNull(key);
		}
		else {
			node.put(key, value);
		}
	}

	private void _putNullable(ObjectNode node, String key, String value) {
		if (value == null) {
			node.putNull(key);
		}
		else {
			node.put(key, value);
		}
	}

	private void _renameKey(JsonNode jsonNode, String newKey, String oldKey) {
		if (jsonNode == null) {
			return;
		}

		if (jsonNode.isObject()) {
			ObjectNode objectNode = (ObjectNode)jsonNode;

			if (objectNode.has(oldKey)) {
				JsonNode value = objectNode.remove(oldKey);

				objectNode.set(newKey, value);
			}

			Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

			fields.forEachRemaining(
				entry -> _renameKey(entry.getValue(), newKey, oldKey));
		}
		else if (jsonNode.isArray()) {
			for (JsonNode element : jsonNode) {
				_renameKey(element, newKey, oldKey);
			}
		}
	}

	private SiteHistogramMetric _toSiteHistogramMetric(
			JsonNode siteNode, String metricField)
		throws Exception {

		JsonNode metricNode = siteNode.path(metricField);

		if (metricNode.isMissingNode() || metricNode.isNull()) {
			return new SiteHistogramMetric();
		}

		_renameKey(metricNode, "histogramMetrics", "metrics");

		ObjectNode wrapper = _objectMapper.createObjectNode();

		wrapper.set("histogram", metricNode.path("histogram"));

		return _objectMapper.treeToValue(wrapper, SiteHistogramMetric.class);
	}

	private static final String _QUERY_DOCUMENTS =
		"query DocumentsAndMediaList($channelId: String, $keywords: String, " +
			"$rangeEnd: String, $rangeKey: Int, $rangeStart: String, " +
				"$size: Int!, $sort: Sort!, $start: Int!) { documents(" +
					"channelId: $channelId, keywords: $keywords, " +
						"rangeEnd: $rangeEnd, rangeKey: $rangeKey, " +
							"rangeStart: $rangeStart, size: $size, " +
								"sort: $sort, start: $start) { assetMetrics " +
									"{ ... on DocumentMetric { assetId " +
										"assetTitle commentsMetric { value } " +
											"downloadsMetric { value } " +
												"impressionMadeMetric { " +
													"value } lastViewedMetric" +
														" { value } " +
															"ratingsMetric { " +
																"value } " +
																	"usersInvolvedMetric " +
																		"{ value } urls } } total } }";

	private static final String _QUERY_EVENTS =
		"query EventQuery($channelId: String!, " +
			"$includeAnonymousUsers: Boolean, $individualId: String, " +
				"$keywords: String, $page: Int!, $rangeEnd: String, " +
					"$rangeKey: Int, $rangeStart: String, $size: Int!) { " +
						"events(channelId: $channelId, " +
							"includeAnonymousUsers: $includeAnonymousUsers, " +
								"individualId: $individualId, " +
									"keywords: $keywords, page: $page, " +
										"rangeEnd: $rangeEnd, " +
											"rangeKey: $rangeKey, " +
												"rangeStart: $rangeStart, " +
													"size: $size) { events { " +
														"emailAddressHashed " +
															"name createDate " +
																"} } }";

	private static final String _QUERY_EVENTS_BY_USER_SESSIONS =
		"query UserSession($channelId: String!, $entityType: EntityType!, " +
			"$keywords: String, $page: Int!, $rangeEnd: String, " +
				"$rangeKey: Int, $rangeStart: String, $size: Int!) { " +
					"eventsByUserSessions(channelId: $channelId, " +
						"entityType: $entityType, keywords: $keywords, " +
							"page: $page, rangeEnd: $rangeEnd, " +
								"rangeKey: $rangeKey, rangeStart: " +
									"$rangeStart, size: $size) { userSessions" +
										" { ... on UserSession { events { " +
											"createDate emailAddressHashed " +
												"name } } } totalEvents } }";

	private static final String _QUERY_MOST_ACTIVE_VISITORS =
		"query MostActiveVisitors($channelId: String!, $rangeKey: Int, " +
			"$size: Int!, $start: Int!) { mostActiveVisitors(" +
				"channelId: $channelId, rangeKey: $rangeKey, size: $size, " +
					"start: $start) { mostActiveVisitors { activitiesCount " +
						"emailAddress firstName id lastName } total } }";

	private static final String _QUERY_SITE_SESSIONS_METRIC =
		"query SitesMetricQuery($channelId: String, " +
			"$emailAddresses: [String], $interval: String!, " +
				"$rangeEnd: String, $rangeKey: Int, $rangeStart: String) { " +
					"site(channelId: $channelId, " +
						"emailAddresses: $emailAddresses, " +
							"interval: $interval, rangeEnd: $rangeEnd, " +
								"rangeKey: $rangeKey, " +
									"rangeStart: $rangeStart) { " +
										"sessionsMetric { histogram { " +
											"asymmetricComparison metrics { " +
												"key value valueKey } total } " +
													"} } }";

	private static final String _QUERY_SITE_VISITORS_METRIC =
		"query SitesMetricQuery($channelId: String, $interval: String!, " +
			"$rangeEnd: String, $rangeKey: Int, $rangeStart: String) { " +
				"site(channelId: $channelId, interval: $interval, " +
					"rangeEnd: $rangeEnd, rangeKey: $rangeKey, " +
						"rangeStart: $rangeStart) { visitorsMetric { " +
							"histogram { asymmetricComparison metrics { key " +
								"value valueKey } total } } } }";

	private static final String _QUERY_VISIT_FREQUENCY =
		"query VisitFrequency($channelId: String!, $rangeKey: Int) { " +
			"visitFrequency(channelId: $channelId, rangeKey: $rangeKey) { " +
				"visitFrequency { count name } totalCount } }";

	private static final Log _log = LogFactoryUtil.getLog(
		DSRAnalyticsCloudClient.class);

	private static final ObjectMapper _objectMapper = new ObjectMapper() {
		{
			configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		}
	};

	private final Http _http;

}
