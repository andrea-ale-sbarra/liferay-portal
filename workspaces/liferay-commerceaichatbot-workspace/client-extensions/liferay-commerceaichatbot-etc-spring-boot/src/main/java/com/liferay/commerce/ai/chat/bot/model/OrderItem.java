package com.liferay.commerce.ai.chat.bot.model;

import java.util.Objects;

/**
 * Represents an item within an order
 */
public class OrderItem {

	public OrderItem() {
	}

	public OrderItem(
		String id, String name, String sku, int quantity, double unitPrice,
		double totalPrice, String status) {

		this.id = id;
		this.name = name;
		this.sku = sku;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalPrice = totalPrice;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)

			return true;

		if ((o == null) || (getClass() != o.getClass()))

			return false;
		OrderItem orderItem = (OrderItem)o;

		return Objects.equals(id, orderItem.id);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getQuantity() {
		return quantity;
	}

	public String getSku() {
		return sku;
	}

	public String getStatus() {
		return status;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	@Override
	public String toString() {
		return "OrderItem{" + "id='" + id + '\'' + ", name='" + name + '\'' +
			", sku='" + sku + '\'' + ", quantity=" + quantity + ", unitPrice=" +
				unitPrice + ", totalPrice=" + totalPrice + ", status='" +
					status + '\'' + '}';
	}

	private String id;
	private String name;
	private int quantity;
	private String sku;
	private String status;
	private double totalPrice;
	private double unitPrice;

}