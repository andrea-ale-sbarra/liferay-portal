/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.client.serdes.v1_0;

import com.liferay.site.dsr.analytics.rest.client.dto.v1_0.DocumentMetric;
import com.liferay.site.dsr.analytics.rest.client.dto.v1_0.DocumentMetricsPage;
import com.liferay.site.dsr.analytics.rest.client.json.BaseJSONParser;

import jakarta.annotation.Generated;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Gianmarco Brunialti
 * @generated
 */
@Generated("")
public class DocumentMetricsPageSerDes {

	public static DocumentMetricsPage toDTO(String json) {
		DocumentMetricsPageJSONParser documentMetricsPageJSONParser =
			new DocumentMetricsPageJSONParser();

		return documentMetricsPageJSONParser.parseToDTO(json);
	}

	public static DocumentMetricsPage[] toDTOs(String json) {
		DocumentMetricsPageJSONParser documentMetricsPageJSONParser =
			new DocumentMetricsPageJSONParser();

		return documentMetricsPageJSONParser.parseToDTOs(json);
	}

	public static String toJSON(DocumentMetricsPage documentMetricsPage) {
		if (documentMetricsPage == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (documentMetricsPage.getDocumentMetrics() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"documentMetrics\": ");

			sb.append("[");

			for (int i = 0; i < documentMetricsPage.getDocumentMetrics().length;
				 i++) {

				sb.append(
					String.valueOf(
						documentMetricsPage.getDocumentMetrics()[i]));

				if ((i + 1) < documentMetricsPage.getDocumentMetrics().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		if (documentMetricsPage.getTotal() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"total\": ");

			sb.append(documentMetricsPage.getTotal());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		DocumentMetricsPageJSONParser documentMetricsPageJSONParser =
			new DocumentMetricsPageJSONParser();

		return documentMetricsPageJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(
		DocumentMetricsPage documentMetricsPage) {

		if (documentMetricsPage == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (documentMetricsPage.getDocumentMetrics() == null) {
			map.put("documentMetrics", null);
		}
		else {
			map.put(
				"documentMetrics",
				String.valueOf(documentMetricsPage.getDocumentMetrics()));
		}

		if (documentMetricsPage.getTotal() == null) {
			map.put("total", null);
		}
		else {
			map.put("total", String.valueOf(documentMetricsPage.getTotal()));
		}

		return map;
	}

	public static class DocumentMetricsPageJSONParser
		extends BaseJSONParser<DocumentMetricsPage> {

		@Override
		protected DocumentMetricsPage createDTO() {
			return new DocumentMetricsPage();
		}

		@Override
		protected DocumentMetricsPage[] createDTOArray(int size) {
			return new DocumentMetricsPage[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "documentMetrics")) {
				return false;
			}
			else if (Objects.equals(jsonParserFieldName, "total")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			DocumentMetricsPage documentMetricsPage, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "documentMetrics")) {
				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					DocumentMetric[] documentMetricsArray =
						new DocumentMetric[jsonParserFieldValues.length];

					for (int i = 0; i < documentMetricsArray.length; i++) {
						documentMetricsArray[i] = DocumentMetricSerDes.toDTO(
							(String)jsonParserFieldValues[i]);
					}

					documentMetricsPage.setDocumentMetrics(
						documentMetricsArray);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "total")) {
				if (jsonParserFieldValue != null) {
					documentMetricsPage.setTotal(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value == null) {
			return "null";
		}

		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}
// LIFERAY-REST-BUILDER-HASH:845544898