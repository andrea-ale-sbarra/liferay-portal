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

package com.liferay.headless.commerce.delivery.order.client.serdes.v1_0;

import com.liferay.headless.commerce.delivery.order.client.dto.v1_0.ShipmentInfo;
import com.liferay.headless.commerce.delivery.order.client.json.BaseJSONParser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
public class ShipmentInfoSerDes {

	public static ShipmentInfo toDTO(String json) {
		ShipmentInfoJSONParser shipmentInfoJSONParser =
			new ShipmentInfoJSONParser();

		return shipmentInfoJSONParser.parseToDTO(json);
	}

	public static ShipmentInfo[] toDTOs(String json) {
		ShipmentInfoJSONParser shipmentInfoJSONParser =
			new ShipmentInfoJSONParser();

		return shipmentInfoJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ShipmentInfo shipmentInfo) {
		if (shipmentInfo == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (shipmentInfo.getAccountId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"accountId\": ");

			sb.append(shipmentInfo.getAccountId());
		}

		if (shipmentInfo.getCarrier() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"carrier\": ");

			sb.append("\"");

			sb.append(_escape(shipmentInfo.getCarrier()));

			sb.append("\"");
		}

		if (shipmentInfo.getCreateDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"createDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(shipmentInfo.getCreateDate()));

			sb.append("\"");
		}

		if (shipmentInfo.getExpectedDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"expectedDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(shipmentInfo.getExpectedDate()));

			sb.append("\"");
		}

		if (shipmentInfo.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(shipmentInfo.getId());
		}

		if (shipmentInfo.getModifiedDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"modifiedDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(shipmentInfo.getModifiedDate()));

			sb.append("\"");
		}

		if (shipmentInfo.getOrderId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"orderId\": ");

			sb.append(shipmentInfo.getOrderId());
		}

		if (shipmentInfo.getShippingAddressId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"shippingAddressId\": ");

			sb.append(shipmentInfo.getShippingAddressId());
		}

		if (shipmentInfo.getShippingDate() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"shippingDate\": ");

			sb.append("\"");

			sb.append(
				liferayToJSONDateFormat.format(shipmentInfo.getShippingDate()));

			sb.append("\"");
		}

		if (shipmentInfo.getShippingMethodId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"shippingMethodId\": ");

			sb.append(shipmentInfo.getShippingMethodId());
		}

		if (shipmentInfo.getShippingOptionName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"shippingOptionName\": ");

			sb.append("\"");

			sb.append(_escape(shipmentInfo.getShippingOptionName()));

			sb.append("\"");
		}

		if (shipmentInfo.getStatus() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"status\": ");

			sb.append(String.valueOf(shipmentInfo.getStatus()));
		}

		if (shipmentInfo.getTrackingNumber() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"trackingNumber\": ");

			sb.append("\"");

			sb.append(_escape(shipmentInfo.getTrackingNumber()));

			sb.append("\"");
		}

		if (shipmentInfo.getUserName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"userName\": ");

			sb.append("\"");

			sb.append(_escape(shipmentInfo.getUserName()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ShipmentInfoJSONParser shipmentInfoJSONParser =
			new ShipmentInfoJSONParser();

		return shipmentInfoJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(ShipmentInfo shipmentInfo) {
		if (shipmentInfo == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		DateFormat liferayToJSONDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssXX");

		if (shipmentInfo.getAccountId() == null) {
			map.put("accountId", null);
		}
		else {
			map.put("accountId", String.valueOf(shipmentInfo.getAccountId()));
		}

		if (shipmentInfo.getCarrier() == null) {
			map.put("carrier", null);
		}
		else {
			map.put("carrier", String.valueOf(shipmentInfo.getCarrier()));
		}

		if (shipmentInfo.getCreateDate() == null) {
			map.put("createDate", null);
		}
		else {
			map.put(
				"createDate",
				liferayToJSONDateFormat.format(shipmentInfo.getCreateDate()));
		}

		if (shipmentInfo.getExpectedDate() == null) {
			map.put("expectedDate", null);
		}
		else {
			map.put(
				"expectedDate",
				liferayToJSONDateFormat.format(shipmentInfo.getExpectedDate()));
		}

		if (shipmentInfo.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(shipmentInfo.getId()));
		}

		if (shipmentInfo.getModifiedDate() == null) {
			map.put("modifiedDate", null);
		}
		else {
			map.put(
				"modifiedDate",
				liferayToJSONDateFormat.format(shipmentInfo.getModifiedDate()));
		}

		if (shipmentInfo.getOrderId() == null) {
			map.put("orderId", null);
		}
		else {
			map.put("orderId", String.valueOf(shipmentInfo.getOrderId()));
		}

		if (shipmentInfo.getShippingAddressId() == null) {
			map.put("shippingAddressId", null);
		}
		else {
			map.put(
				"shippingAddressId",
				String.valueOf(shipmentInfo.getShippingAddressId()));
		}

		if (shipmentInfo.getShippingDate() == null) {
			map.put("shippingDate", null);
		}
		else {
			map.put(
				"shippingDate",
				liferayToJSONDateFormat.format(shipmentInfo.getShippingDate()));
		}

		if (shipmentInfo.getShippingMethodId() == null) {
			map.put("shippingMethodId", null);
		}
		else {
			map.put(
				"shippingMethodId",
				String.valueOf(shipmentInfo.getShippingMethodId()));
		}

		if (shipmentInfo.getShippingOptionName() == null) {
			map.put("shippingOptionName", null);
		}
		else {
			map.put(
				"shippingOptionName",
				String.valueOf(shipmentInfo.getShippingOptionName()));
		}

		if (shipmentInfo.getStatus() == null) {
			map.put("status", null);
		}
		else {
			map.put("status", String.valueOf(shipmentInfo.getStatus()));
		}

		if (shipmentInfo.getTrackingNumber() == null) {
			map.put("trackingNumber", null);
		}
		else {
			map.put(
				"trackingNumber",
				String.valueOf(shipmentInfo.getTrackingNumber()));
		}

		if (shipmentInfo.getUserName() == null) {
			map.put("userName", null);
		}
		else {
			map.put("userName", String.valueOf(shipmentInfo.getUserName()));
		}

		return map;
	}

	public static class ShipmentInfoJSONParser
		extends BaseJSONParser<ShipmentInfo> {

		@Override
		protected ShipmentInfo createDTO() {
			return new ShipmentInfo();
		}

		@Override
		protected ShipmentInfo[] createDTOArray(int size) {
			return new ShipmentInfo[size];
		}

		@Override
		protected void setField(
			ShipmentInfo shipmentInfo, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "accountId")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setAccountId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "carrier")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setCarrier((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "createDate")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setCreateDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "expectedDate")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setExpectedDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "modifiedDate")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setModifiedDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "orderId")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setOrderId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "shippingAddressId")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setShippingAddressId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "shippingDate")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setShippingDate(
						toDate((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "shippingMethodId")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setShippingMethodId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "shippingOptionName")) {

				if (jsonParserFieldValue != null) {
					shipmentInfo.setShippingOptionName(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "status")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setStatus(
						StatusSerDes.toDTO((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "trackingNumber")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setTrackingNumber(
						(String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "userName")) {
				if (jsonParserFieldValue != null) {
					shipmentInfo.setUserName((String)jsonParserFieldValue);
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