package com.liferay.commerce.ai.chat.bot.model;

public class OrderShipmentDetails {

	public OrderShipmentDetails() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getOneLineAddress() {
		return oneLineAddress;
	}

	public void setOneLineAddress(String oneLineAddress) {
		this.oneLineAddress = oneLineAddress;
	}

	public String getShipmentStatus() {
		return shipmentStatus;
	}

	public void setShipmentStatus(String shipmentStatus) {
		this.shipmentStatus = shipmentStatus;
	}

	public String getShippingDate() {
		return shippingDate;
	}

	public void setShippingDate(String shippingDate) {
		this.shippingDate = shippingDate;
	}

	public String getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(String expectedDate) {
		this.expectedDate = expectedDate;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getTotalFormatted() {
		return totalFormatted;
	}

	public void setTotalFormatted(String totalFormatted) {
		this.totalFormatted = totalFormatted;
	}

	private String id;
	private String createDate;
	private String oneLineAddress;
	private String shipmentStatus;
	private String shippingDate;
	private String expectedDate;
	private String trackingNumber;
	private String carrier;
	private String totalFormatted;

}
