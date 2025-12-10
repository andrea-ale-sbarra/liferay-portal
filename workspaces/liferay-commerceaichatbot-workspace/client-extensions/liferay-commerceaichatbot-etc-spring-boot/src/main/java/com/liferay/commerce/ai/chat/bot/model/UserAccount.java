/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import com.liferay.petra.string.StringBundler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

		return Objects.equals(_id, userAccount.getId());
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

	public String getId() {
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
		return Objects.hash(_id);
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

	public void setId(String id) {
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
		return StringBundler.concat(
			"UserAccount{id='", _id, "\', email='", _email, "\', firstName='",
			_firstName, "\', lastName='", _lastName, "\', phone='", _phone,
			"\', address=", _address, '}');
	}

	private Map<String, String> _address = new HashMap<>();
	private String _email;
	private String _firstName;
	private String _id;
	private String _lastName;
	private String _phone;

}