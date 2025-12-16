/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import java.time.Instant;

/**
 * @author Ivica Cardic
 */
public class OrderShipmentDetails {

	public String getCarrier() {
		return _carrier;
	}

	public Instant getCreateDate() {
		return _createDate;
	}

	public Instant getExpectedDate() {
		return _expectedDate;
	}

	public String getId() {
		return _id;
	}

	public String getOneLineAddress() {
		return _oneLineAddress;
	}

	public String getShipmentStatus() {
		return _shipmentStatus;
	}

	public Instant getShippingDate() {
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

	public void setCreateDate(Instant createDate) {
		_createDate = createDate;
	}

	public void setExpectedDate(Instant expectedDate) {
		_expectedDate = expectedDate;
	}

	public void setId(String id) {
		_id = id;
	}

	public void setOneLineAddress(String oneLineAddress) {
		_oneLineAddress = oneLineAddress;
	}

	public void setShipmentStatus(String shipmentStatus) {
		_shipmentStatus = shipmentStatus;
	}

	public void setShippingDate(Instant shippingDate) {
		_shippingDate = shippingDate;
	}

	public void setTotalFormatted(String totalFormatted) {
		_totalFormatted = totalFormatted;
	}

	public void setTrackingNumber(String trackingNumber) {
		_trackingNumber = trackingNumber;
	}

	private String _carrier;
	private Instant _createDate;
	private Instant _expectedDate;
	private String _id;
	private String _oneLineAddress;
	private String _shipmentStatus;
	private Instant _shippingDate;
	private String _totalFormatted;
	private String _trackingNumber;

}