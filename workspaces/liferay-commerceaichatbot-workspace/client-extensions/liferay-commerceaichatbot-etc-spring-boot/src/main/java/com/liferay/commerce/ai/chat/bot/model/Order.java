package com.liferay.commerce.ai.chat.bot.model;

import java.time.OffsetDateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Order {

	@Override
	public boolean equals(Object o) {
		if (this == o)

			return true;

		if ((o == null) || (getClass() != o.getClass()))

			return false;
		Order order = (Order)o;

		return Objects.equals(id, order.id);
	}

	public Map<String, String> getBillingAddress() {
		return billingAddress;
	}

	public String getAccountId() {
		return accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getId() {
		return id;
	}

	public OffsetDateTime getOrderDate() {
		return orderDate;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public Map<String, String> getShippingAddress() {
		return shippingAddress;
	}

	public String getStatus() {
		return status;
	}

	public double getTotal() {
		return total;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public void setBillingAddress(Map<String, String> billingAddress) {
		this.billingAddress =
			billingAddress != null ? new HashMap<>(billingAddress) :
				new HashMap<>();
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOrderDate(OffsetDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public void setShippingAddress(Map<String, String> shippingAddress) {
		this.shippingAddress =
			shippingAddress != null ? new HashMap<>(shippingAddress) :
				new HashMap<>();
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	@Override
	public String toString() {
		return "Order{" + "id='" + id + '\'' + ", orderNumber='" + orderNumber +
			'\'' + ", customerId='" + accountId + '\'' + ", customerName='" +
			accountName + '\'' + ", orderDate=" + orderDate +
					", status='" + status + '\'' + ", total=" +
			total +
							", shippingAddress=" + shippingAddress +
								", billingAddress=" + billingAddress + '}';
	}

	// --- Extra fields merged from former OrderDetails DTO ---
	public String getCreateDate() { return createDate; }
	public void setCreateDate(String createDate) { this.createDate = createDate; }
	public String getStatusLabel() { return statusLabel; }
	public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
	public String getStatusCode() { return statusCode; }
	public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
	public String getTotalFormatted() { return totalFormatted; }
	public void setTotalFormatted(String totalFormatted) { this.totalFormatted = totalFormatted; }
	public String getExternalReferenceCode() { return externalReferenceCode; }
	public void setExternalReferenceCode(String externalReferenceCode) { this.externalReferenceCode = externalReferenceCode; }

	// --- New summary formatted fields to avoid JSONObject usage ---
	public String getSubtotalFormatted() { return subtotalFormatted; }
	public void setSubtotalFormatted(String subtotalFormatted) { this.subtotalFormatted = subtotalFormatted; }
	public String getShippingValueFormatted() { return shippingValueFormatted; }
	public void setShippingValueFormatted(String shippingValueFormatted) { this.shippingValueFormatted = shippingValueFormatted; }
	public String getShippingDiscountValueFormatted() { return shippingDiscountValueFormatted; }
	public void setShippingDiscountValueFormatted(String shippingDiscountValueFormatted) { this.shippingDiscountValueFormatted = shippingDiscountValueFormatted; }
	public String getTaxValueFormatted() { return taxValueFormatted; }
	public void setTaxValueFormatted(String taxValueFormatted) { this.taxValueFormatted = taxValueFormatted; }

	public int getItemsQuantity() {
		return itemsQuantity;
	}

	public void setItemsQuantity(int itemsQuantity) {
		this.itemsQuantity = itemsQuantity;
	}

	private Map<String, String> billingAddress = new HashMap<>();
	private String accountId;
	private String accountName;
	private String id;
	private OffsetDateTime orderDate;
	private String orderNumber;
	private Map<String, String> shippingAddress = new HashMap<>();
	private String status;
	private double total;
	private int itemsQuantity;

	// Former OrderDetails fields
	private String createDate; // ISO string for display/sorting
	private String statusLabel; // orderStatusInfo.label
	private String statusCode;  // orderStatusInfo.code
	private String totalFormatted; // summary.totalFormatted or totalFormatted
	private String externalReferenceCode; // externalReferenceCode
	private String subtotalFormatted; // summary.subtotalFormatted
	private String shippingValueFormatted; // summary.shippingValueFormatted
	private String shippingDiscountValueFormatted; // summary.shippingDiscountValueFormatted
	private String taxValueFormatted; // summary.taxValueFormatted
}