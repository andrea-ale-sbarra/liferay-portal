/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

/**
 * @author Ivica Cardic
 */
public class Account {

	public Account() {
	}

	public Account(long id, String name, String type, String status) {
		_id = id;
		_name = name;
		_type = type;
		_status = status;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}

		Account account = (Account)object;

		if (_id != account.getId()) {
			return false;
		}

		return true;
	}

	public long getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public String getStatus() {
		return _status;
	}

	public String getType() {
		return _type;
	}

	@Override
	public int hashCode() {
		return (int)(_id ^ (_id >>> 32));
	}

	public void setId(long id) {
		_id = id;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setStatus(String status) {
		_status = status;
	}

	public void setType(String type) {
		_type = type;
	}

	@Override
	public String toString() {
		return new StringBuilder(
		).append(
			"Account{id="
		).append(
			_id
		).append(
			", name='"
		).append(
			_name
		).append(
			"', type='"
		).append(
			_type
		).append(
			"', status='"
		).append(
			_status
		).append(
			"'}"
		).toString();
	}

	private long _id;
	private String _name;
	private String _status;
	private String _type;

}