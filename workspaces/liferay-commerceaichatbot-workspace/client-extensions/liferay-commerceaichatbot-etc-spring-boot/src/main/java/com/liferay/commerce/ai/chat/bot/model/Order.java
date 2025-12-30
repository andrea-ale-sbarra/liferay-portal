/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ivica Cardic
 */
public class Order {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}

		Order order = (Order)object;

		return Objects.equals(_id, order.getId());
	}

	public String getAccountId() {
		return _accountId;
	}

	public String getAccountName() {
		return _accountName;
	}

	public Map<String, String> getBillingAddress() {
		return _billingAddress;
	}

	public Date getCreateDate() {
		return _createDate;
	}

	public String getExternalReferenceCode() {
		return _externalReferenceCode;
	}

	public String getId() {
		return _id;
	}

	public int getItemsQuantity() {
		return _itemsQuantity;
	}

	public Date getOrderDate() {
		return _orderDate;
	}

	public String getOrderNumber() {
		return _orderNumber;
	}

	public Map<String, String> getShippingAddress() {
		return _shippingAddress;
	}

	public String getShippingDiscountValueFormatted() {
		return _shippingDiscountValueFormatted;
	}

	public String getShippingValueFormatted() {
		return _shippingValueFormatted;
	}

	public String getStatus() {
		return _status;
	}

	public String getStatusCode() {
		return _statusCode;
	}

	public String getStatusLabel() {
		return _statusLabel;
	}

	public String getSubtotalFormatted() {
		return _subtotalFormatted;
	}

	public String getTaxValueFormatted() {
		return _taxValueFormatted;
	}

	public double getTotal() {
		return _total;
	}

	public String getTotalFormatted() {
		return _totalFormatted;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_id);
	}

	public void setAccountId(String accountId) {
		_accountId = accountId;
	}

	public void setAccountName(String accountName) {
		_accountName = accountName;
	}

	public void setBillingAddress(Map<String, String> billingAddress) {
		_billingAddress =
			(billingAddress != null) ? new HashMap<>(billingAddress) :
				new HashMap<>();
	}

	public void setCreateDate(Date createDate) {
		_createDate = createDate;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		_externalReferenceCode = externalReferenceCode;
	}

	public void setId(String id) {
		_id = id;
	}

	public void setItemsQuantity(int itemsQuantity) {
		_itemsQuantity = itemsQuantity;
	}

	public void setOrderDate(Date orderDate) {
		_orderDate = orderDate;
	}

	public void setOrderNumber(String orderNumber) {
		_orderNumber = orderNumber;
	}

	public void setShippingAddress(Map<String, String> shippingAddress) {
		_billingAddress =
			(shippingAddress != null) ? new HashMap<>(shippingAddress) :
				new HashMap<>();
	}

	public void setShippingDiscountValueFormatted(
		String shippingDiscountValueFormatted) {

		_shippingDiscountValueFormatted = shippingDiscountValueFormatted;
	}

	public void setShippingValueFormatted(String shippingValueFormatted) {
		_shippingValueFormatted = shippingValueFormatted;
	}

	public void setStatus(String status) {
		_status = status;
	}

	public void setStatusCode(String statusCode) {
		_statusCode = statusCode;
	}

	public void setStatusLabel(String statusLabel) {
		_statusLabel = statusLabel;
	}

	public void setSubtotalFormatted(String subtotalFormatted) {
		_subtotalFormatted = subtotalFormatted;
	}

	public void setTaxValueFormatted(String taxValueFormatted) {
		_taxValueFormatted = taxValueFormatted;
	}

	public void setTotal(double total) {
		_total = total;
	}

	public void setTotalFormatted(String totalFormatted) {
		_totalFormatted = totalFormatted;
	}

	@Override
	public String toString() {
		return new StringBuilder(
		).append(
			"Order{id='"
		).append(
			_id
		).append(
			"\', orderNumber='"
		).append(
			_orderNumber
		).append(
			"\', customerId='"
		).append(
			_accountId
		).append(
			"\', customerName='"
		).append(
			_accountName
		).append(
			"\', orderDate="
		).append(
			_orderDate
		).append(
			", status='"
		).append(
			_status
		).append(
			"\', total="
		).append(
			_total
		).append(
			", shippingAddress="
		).append(
			_shippingAddress
		).append(
			", billingAddress="
		).append(
			_billingAddress
		).append(
			'}'
		).toString();
	}

	private String _accountId;
	private String _accountName;
	private Map<String, String> _billingAddress = new HashMap<>();
	private Date _createDate;
	private String _externalReferenceCode;
	private String _id;
	private int _itemsQuantity;
	private Date _orderDate;
	private String _orderNumber;
	private final Map<String, String> _shippingAddress = new HashMap<>();
	private String _shippingDiscountValueFormatted;
	private String _shippingValueFormatted;
	private String _status;
	private String _statusCode;
	private String _statusLabel;
	private String _subtotalFormatted;
	private String _taxValueFormatted;
	private double _total;
	private String _totalFormatted;

}