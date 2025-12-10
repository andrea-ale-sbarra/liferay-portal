/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

/**
 * @author Ivica Cardic
 */
public class Summary {

	public Summary() {
	}

	public Summary(
		String error, String id, String name, Order order, int orderCount,
		String type) {

		_error = error;
		_id = id;
		_name = name;
		_order = order;
		_orderCount = orderCount;
		_type = type;
	}

	public String getError() {
		return _error;
	}

	public String getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public Order getOrder() {
		return _order;
	}

	public int getOrderCount() {
		return _orderCount;
	}

	public String getType() {
		return _type;
	}

	public void setError(String error) {
		_error = error;
	}

	public void setId(String id) {
		_id = id;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setOrder(Order order) {
		_order = order;
	}

	public void setOrderCount(int orderCount) {
		_orderCount = orderCount;
	}

	public void setType(String type) {
		_type = type;
	}

	private String _error;
	private String _id;
	private String _name;
	private Order _order;
	private int _orderCount;
	private String _type;

}