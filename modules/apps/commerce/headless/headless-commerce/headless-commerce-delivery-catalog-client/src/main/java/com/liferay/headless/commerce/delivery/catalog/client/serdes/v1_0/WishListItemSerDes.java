/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.commerce.delivery.catalog.client.serdes.v1_0;

import com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0.WishListItem;
import com.liferay.headless.commerce.delivery.catalog.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Andrea Sbarra
 * @generated
 */
@Generated("")
public class WishListItemSerDes {

	public static WishListItem toDTO(String json) {
		WishListItemJSONParser wishListItemJSONParser =
			new WishListItemJSONParser();

		return wishListItemJSONParser.parseToDTO(json);
	}

	public static WishListItem[] toDTOs(String json) {
		WishListItemJSONParser wishListItemJSONParser =
			new WishListItemJSONParser();

		return wishListItemJSONParser.parseToDTOs(json);
	}

	public static String toJSON(WishListItem wishListItem) {
		if (wishListItem == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (wishListItem.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(wishListItem.getId());
		}

		if (wishListItem.getProduct() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"product\": ");

			sb.append(String.valueOf(wishListItem.getProduct()));
		}

		if (wishListItem.getWishListId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"wishListId\": ");

			sb.append(wishListItem.getWishListId());
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		WishListItemJSONParser wishListItemJSONParser =
			new WishListItemJSONParser();

		return wishListItemJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(WishListItem wishListItem) {
		if (wishListItem == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (wishListItem.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(wishListItem.getId()));
		}

		if (wishListItem.getProduct() == null) {
			map.put("product", null);
		}
		else {
			map.put("product", String.valueOf(wishListItem.getProduct()));
		}

		if (wishListItem.getWishListId() == null) {
			map.put("wishListId", null);
		}
		else {
			map.put("wishListId", String.valueOf(wishListItem.getWishListId()));
		}

		return map;
	}

	public static class WishListItemJSONParser
		extends BaseJSONParser<WishListItem> {

		@Override
		protected WishListItem createDTO() {
			return new WishListItem();
		}

		@Override
		protected WishListItem[] createDTOArray(int size) {
			return new WishListItem[size];
		}

		@Override
		protected void setField(
			WishListItem wishListItem, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					wishListItem.setId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "product")) {
				if (jsonParserFieldValue != null) {
					wishListItem.setProduct(
						ProductSerDes.toDTO((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "wishListId")) {
				if (jsonParserFieldValue != null) {
					wishListItem.setWishListId(
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

			Class<?> valueClass = value.getClass();

			if (value instanceof Map) {
				sb.append(_toJSON((Map)value));
			}
			else if (valueClass.isArray()) {
				Object[] values = (Object[])value;

				sb.append("[");

				for (int i = 0; i < values.length; i++) {
					sb.append("\"");
					sb.append(_escape(values[i]));
					sb.append("\"");

					if ((i + 1) < values.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(entry.getValue()));
				sb.append("\"");
			}
			else {
				sb.append(String.valueOf(entry.getValue()));
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

}