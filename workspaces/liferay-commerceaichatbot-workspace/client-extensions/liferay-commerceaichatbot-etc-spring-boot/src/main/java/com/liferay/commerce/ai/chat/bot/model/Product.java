/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

/**
 * @author Ivica Cardic
 */
public class Product {

	public Product() {
	}

	public Product(
		long id, String name, String description,
		String externalReferenceCode) {

		_id = id;
		_name = name;
		_description = description;
		_externalReferenceCode = externalReferenceCode;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}

		Product product = (Product)object;

		if (_id != product.getId()) {
			return false;
		}

		return true;
	}

	public String getDescription() {
		return _description;
	}

	public String getExternalReferenceCode() {
		return _externalReferenceCode;
	}

	public long getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	@Override
	public int hashCode() {
		return (int)(_id ^ (_id >>> 32));
	}

	public void setDescription(String description) {
		_description = description;
	}

	public void setExternalReferenceCode(String externalReferenceCode) {
		_externalReferenceCode = externalReferenceCode;
	}

	public void setId(long id) {
		_id = id;
	}

	public void setName(String name) {
		_name = name;
	}

	@Override
	public String toString() {
		return new StringBuilder(
		).append(
			"Product{id="
		).append(
			_id
		).append(
			", name='"
		).append(
			_name
		).append(
			"', description='"
		).append(
			_description
		).append(
			"', externalReferenceCode='"
		).append(
			_externalReferenceCode
		).append(
			"'}"
		).toString();
	}

	private String _description;
	private String _externalReferenceCode;
	private long _id;
	private String _name;

}