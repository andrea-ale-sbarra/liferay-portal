/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.client.serdes.v1_0;

import com.liferay.site.dsr.analytics.rest.client.dto.v1_0.EventEntry;
import com.liferay.site.dsr.analytics.rest.client.dto.v1_0.EventsPage;
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
public class EventsPageSerDes {

	public static EventsPage toDTO(String json) {
		EventsPageJSONParser eventsPageJSONParser = new EventsPageJSONParser();

		return eventsPageJSONParser.parseToDTO(json);
	}

	public static EventsPage[] toDTOs(String json) {
		EventsPageJSONParser eventsPageJSONParser = new EventsPageJSONParser();

		return eventsPageJSONParser.parseToDTOs(json);
	}

	public static String toJSON(EventsPage eventsPage) {
		if (eventsPage == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (eventsPage.getEventEntries() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"eventEntries\": ");

			sb.append("[");

			for (int i = 0; i < eventsPage.getEventEntries().length; i++) {
				sb.append(String.valueOf(eventsPage.getEventEntries()[i]));

				if ((i + 1) < eventsPage.getEventEntries().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		EventsPageJSONParser eventsPageJSONParser = new EventsPageJSONParser();

		return eventsPageJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(EventsPage eventsPage) {
		if (eventsPage == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (eventsPage.getEventEntries() == null) {
			map.put("eventEntries", null);
		}
		else {
			map.put(
				"eventEntries", String.valueOf(eventsPage.getEventEntries()));
		}

		return map;
	}

	public static class EventsPageJSONParser
		extends BaseJSONParser<EventsPage> {

		@Override
		protected EventsPage createDTO() {
			return new EventsPage();
		}

		@Override
		protected EventsPage[] createDTOArray(int size) {
			return new EventsPage[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "eventEntries")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			EventsPage eventsPage, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "eventEntries")) {
				if (jsonParserFieldValue != null) {
					Object[] jsonParserFieldValues =
						(Object[])jsonParserFieldValue;

					EventEntry[] eventEntriesArray =
						new EventEntry[jsonParserFieldValues.length];

					for (int i = 0; i < eventEntriesArray.length; i++) {
						eventEntriesArray[i] = EventEntrySerDes.toDTO(
							(String)jsonParserFieldValues[i]);
					}

					eventsPage.setEventEntries(eventEntriesArray);
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
// LIFERAY-REST-BUILDER-HASH:-390991617