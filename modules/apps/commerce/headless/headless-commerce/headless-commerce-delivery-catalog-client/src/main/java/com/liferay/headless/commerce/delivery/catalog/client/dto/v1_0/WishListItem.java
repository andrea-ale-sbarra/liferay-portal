/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.commerce.delivery.catalog.client.dto.v1_0;

import com.liferay.headless.commerce.delivery.catalog.client.function.UnsafeSupplier;
import com.liferay.headless.commerce.delivery.catalog.client.serdes.v1_0.WishListItemSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author Andrea Sbarra
 * @generated
 */
@Generated("")
public class WishListItem implements Cloneable, Serializable {

	public static WishListItem toDTO(String json) {
		return WishListItemSerDes.toDTO(json);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
		try {
			id = idUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long id;

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public void setProduct(
		UnsafeSupplier<Product, Exception> productUnsafeSupplier) {

		try {
			product = productUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Product product;

	public Long getWishListId() {
		return wishListId;
	}

	public void setWishListId(Long wishListId) {
		this.wishListId = wishListId;
	}

	public void setWishListId(
		UnsafeSupplier<Long, Exception> wishListIdUnsafeSupplier) {

		try {
			wishListId = wishListIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long wishListId;

	@Override
	public WishListItem clone() throws CloneNotSupportedException {
		return (WishListItem)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof WishListItem)) {
			return false;
		}

		WishListItem wishListItem = (WishListItem)object;

		return Objects.equals(toString(), wishListItem.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return WishListItemSerDes.toJSON(this);
	}

}