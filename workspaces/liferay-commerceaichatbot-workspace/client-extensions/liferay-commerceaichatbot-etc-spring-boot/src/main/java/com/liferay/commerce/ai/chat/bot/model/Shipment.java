/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import java.util.Objects;

/**
 * @author Ivica Cardic
 */
public class Shipment {

	public Shipment() {
	}

	public Shipment(
		String id, String carrier, String trackingNumber,
		String shipmentStatus) {

		_id = id;
		_carrier = carrier;
		_trackingNumber = trackingNumber;
		_shipmentStatus = shipmentStatus;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}

		Shipment shipment = (Shipment)object;

		return Objects.equals(_id, shipment.getId());
	}

	public String getCarrier() {
		return _carrier;
	}

	public String getExpectedDate() {
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

	public String getShippingDate() {
		return _shippingDate;
	}

	public Status getStatus() {
		return _status;
	}

	public String getTrackingNumber() {
		return _trackingNumber;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_id);
	}

	public void setCarrier(String carrier) {
		_carrier = carrier;
	}

	public void setExpectedDate(String expectedDate) {
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

	public void setShippingDate(String shippingDate) {
		_shippingDate = shippingDate;
	}

	public void setStatus(Status status) {
		_status = status;
	}

	public void setTrackingNumber(String trackingNumber) {
		_trackingNumber = trackingNumber;
	}

	@Override
	public String toString() {
		return new StringBuilder(
		).append(
			"Shipment{id='"
		).append(
			_id
		).append(
			"\', carrier='"
		).append(
			_carrier
		).append(
			"\', trackingNumber='"
		).append(
			_trackingNumber
		).append(
			"\', shipmentStatus='"
		).append(
			_shipmentStatus
		).append(
			"\'}"
		).toString();
	}

	public static class Status {

		public String getLabel() {
			return _label;
		}

		public void setLabel(String label) {
			_label = label;
		}

		private String _label;

	}

	private String _carrier;
	private String _expectedDate;
	private String _id;
	private String _oneLineAddress;
	private String _shipmentStatus;
	private String _shippingDate;
	private Status _status;
	private String _trackingNumber;

}