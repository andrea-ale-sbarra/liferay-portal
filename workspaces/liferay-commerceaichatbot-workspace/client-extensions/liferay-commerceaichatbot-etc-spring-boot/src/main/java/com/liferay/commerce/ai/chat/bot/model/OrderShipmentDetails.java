/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import java.util.Date;

/**
 * @author Ivica Cardic
 */
public class OrderShipmentDetails {

	public String getCarrier() {
		return _carrier;
	}

	public Date getCreateDate() {
		return _createDate;
	}

	public Date getExpectedDate() {
		return _expectedDate;
	}

	public long getId() {
		return _id;
	}

	public String getOneLineAddress() {
		return _oneLineAddress;
	}

	public String getShipmentStatus() {
		return _shipmentStatus;
	}

	public Date getShippingDate() {
		return _shippingDate;
	}

	public String getTotalFormatted() {
		return _totalFormatted;
	}

	public String getTrackingNumber() {
		return _trackingNumber;
	}

	public void setCarrier(String carrier) {
		_carrier = carrier;
	}

	public void setCreateDate(Date createDate) {
		_createDate = createDate;
	}

	public void setExpectedDate(Date expectedDate) {
		_expectedDate = expectedDate;
	}

	public void setId(long id) {
		_id = id;
	}

	public void setOneLineAddress(String oneLineAddress) {
		_oneLineAddress = oneLineAddress;
	}

	public void setShipmentStatus(String shipmentStatus) {
		_shipmentStatus = shipmentStatus;
	}

	public void setShippingDate(Date shippingDate) {
		_shippingDate = shippingDate;
	}

	public void setTotalFormatted(String totalFormatted) {
		_totalFormatted = totalFormatted;
	}

	public void setTrackingNumber(String trackingNumber) {
		_trackingNumber = trackingNumber;
	}

	private String _carrier;
	private Date _createDate;
	private Date _expectedDate;
	private long _id;
	private String _oneLineAddress;
	private String _shipmentStatus;
	private Date _shippingDate;
	private String _totalFormatted;
	private String _trackingNumber;

}