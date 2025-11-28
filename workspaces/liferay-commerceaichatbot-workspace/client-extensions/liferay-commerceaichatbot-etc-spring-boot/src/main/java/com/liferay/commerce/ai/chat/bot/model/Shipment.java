package com.liferay.commerce.ai.chat.bot.model;

import java.util.Objects;

public class Shipment {

	public Shipment() {
	}

	public Shipment(
		String id, String carrier, String trackingNumber,
		String shipmentStatus) {

		this.id = id;
		this.carrier = carrier;
		this.trackingNumber = trackingNumber;
		this.shipmentStatus = shipmentStatus;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)

			return true;

		if ((o == null) || (getClass() != o.getClass()))

			return false;
		Shipment shipment = (Shipment)o;

		return Objects.equals(id, shipment.id);
	}

	public String getCarrier() {
		return carrier;
	}

	public String getId() {
		return id;
	}

	public String getShipmentStatus() {
		return shipmentStatus;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public String getOneLineAddress() {
		return oneLineAddress;
	}

	public String getShippingDate() {
		return shippingDate;
	}

	public String getExpectedDate() {
		return expectedDate;
	}

	public Status getStatus() {
		return status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setShipmentStatus(String shipmentStatus) {
		this.shipmentStatus = shipmentStatus;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public void setOneLineAddress(String oneLineAddress) {
		this.oneLineAddress = oneLineAddress;
	}

	public void setShippingDate(String shippingDate) {
		this.shippingDate = shippingDate;
	}

	public void setExpectedDate(String expectedDate) {
		this.expectedDate = expectedDate;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Shipment{" + "id='" + id + '\'' + ", carrier='" + carrier +
			'\'' + ", trackingNumber='" + trackingNumber + '\'' +
				", shipmentStatus='" + shipmentStatus + '\'' + '}';
	}

	private String carrier;
	private String id;
	private String shipmentStatus;
	private String trackingNumber;
	private String oneLineAddress;
	private String shippingDate;
	private String expectedDate;
	private Status status;

	public static class Status {
		private String label;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
	}

}