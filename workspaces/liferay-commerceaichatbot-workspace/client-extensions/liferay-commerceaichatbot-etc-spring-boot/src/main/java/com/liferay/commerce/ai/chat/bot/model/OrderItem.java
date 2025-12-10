/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import com.liferay.petra.string.StringBundler;

import java.util.Objects;

/**
 * @author Ivica Cardic
 */
public class OrderItem {

	public OrderItem() {
	}

	public OrderItem(
		String id, String name, String sku, int quantity, double unitPrice,
		double totalPrice, String status) {

		_id = id;
		_name = name;
		_sku = sku;
		_quantity = quantity;
		_unitPrice = unitPrice;
		_totalPrice = totalPrice;
		_status = status;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}

		OrderItem orderItem = (OrderItem)object;

		return Objects.equals(_id, orderItem.getId());
	}

	public String getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public int getQuantity() {
		return _quantity;
	}

	public String getSku() {
		return _sku;
	}

	public String getStatus() {
		return _status;
	}

	public double getTotalPrice() {
		return _totalPrice;
	}

	public double getUnitPrice() {
		return _unitPrice;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_id);
	}

	public void setId(String id) {
		_id = id;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setQuantity(int quantity) {
		_quantity = quantity;
	}

	public void setSku(String sku) {
		_sku = sku;
	}

	public void setStatus(String status) {
		_status = status;
	}

	public void setTotalPrice(double totalPrice) {
		_totalPrice = totalPrice;
	}

	public void setUnitPrice(double unitPrice) {
		_unitPrice = unitPrice;
	}

	@Override
	public String toString() {
		return StringBundler.concat(
			"OrderItem{id='", _id, "\', name='", _name, "\', sku='", _sku,
			"\', quantity=", _quantity, ", unitPrice=", _unitPrice,
			", totalPrice=", _totalPrice, ", status='", _status, "\'}");
	}

	private String _id;
	private String _name;
	private int _quantity;
	private String _sku;
	private String _status;
	private double _totalPrice;
	private double _unitPrice;

}