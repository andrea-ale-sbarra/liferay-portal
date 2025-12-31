/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivica Cardic
 */
public class UserAccount {

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}

		UserAccount userAccount = (UserAccount)object;

		if (_id != userAccount.getId()) {
			return false;
		}

		return true;
	}

	public List<AccountBrief> getAccountBriefs() {
		return _accountBriefs;
	}

	public Map<String, String> getAddress() {
		return _address;
	}

	public String getEmail() {
		return _email;
	}

	public String getFirstName() {
		return _firstName;
	}

	public long getId() {
		return _id;
	}

	public String getLastName() {
		return _lastName;
	}

	public String getPhone() {
		return _phone;
	}

	@Override
	public int hashCode() {
		return (int)(_id ^ (_id >>> 32));
	}

	public void setAccountBriefs(List<AccountBrief> accountBriefs) {
		_accountBriefs = accountBriefs;
	}

	public void setAddress(Map<String, String> address) {
		_address = (address != null) ? new HashMap<>(address) : new HashMap<>();
	}

	public void setEmail(String email) {
		_email = email;
	}

	public void setFirstName(String firstName) {
		_firstName = firstName;
	}

	public void setId(long id) {
		_id = id;
	}

	public void setLastName(String lastName) {
		_lastName = lastName;
	}

	public void setPhone(String phone) {
		_phone = phone;
	}

	@Override
	public String toString() {
		return new StringBuilder(
		).append(
			"UserAccount{id="
		).append(
			_id
		).append(
			", email='"
		).append(
			_email
		).append(
			"', firstName='"
		).append(
			_firstName
		).append(
			"', lastName='"
		).append(
			_lastName
		).append(
			"', phone='"
		).append(
			_phone
		).append(
			"', address="
		).append(
			_address
		).append(
			'}'
		).toString();
	}

	private List<AccountBrief> _accountBriefs;
	private Map<String, String> _address = new HashMap<>();
	private String _email;
	private String _firstName;
	private long _id;
	private String _lastName;
	private String _phone;

}