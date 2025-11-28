package com.liferay.commerce.ai.chat.bot.model;

import java.util.Objects;

public class Product {

	public Product() {
	}

	public Product(
		String id, String name, String description,
		String externalReferenceCode) {

		this.id = id;
		this.name = name;
		this.description = description;
		this.externalReferenceCode = externalReferenceCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)

			return true;

		if ((o == null) || (getClass() != o.getClass()))

			return false;
		Product product = (Product)o;

		return Objects.equals(id, product.id);
	}

	public String getDescription() {
		return description;
	}

	public String getExternalReferenceCode() {
		return externalReferenceCode;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		this.externalReferenceCode = externalReferenceCode;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Product{" + "id='" + id + '\'' + ", name='" + name + '\'' +
			", description='" + description + '\'' +
				", externalReferenceCode='" + externalReferenceCode + '\'' +
					'}';
	}

	private String description;
	private String externalReferenceCode;
	private String id;
	private String name;

}