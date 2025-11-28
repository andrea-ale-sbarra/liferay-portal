package com.liferay.commerce.ai.chat.bot.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents customer information
 */
public class UserAccount {


	@Override
	public boolean equals(Object o) {
		if (this == o)

			return true;

		if ((o == null) || (getClass() != o.getClass()))

			return false;
		UserAccount userAccount = (UserAccount)o;

		return Objects.equals(id, userAccount.id);
	}

	public Map<String, String> getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getId() {
		return id;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPhone() {
		return phone;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public void setAddress(Map<String, String> address) {
		this.address =
			address != null ? new HashMap<>(address) : new HashMap<>();
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public String toString() {
		return "UserAccount{" + "id='" + id + '\'' + ", email='" + email + '\'' +
			", firstName='" + firstName + '\'' + ", lastName='" + lastName +
				'\'' + ", phone='" + phone + '\'' + ", address=" + address +
					'}';
	}

	private Map<String, String> address = new HashMap<>();
	private String email;
	private String firstName;
	private String id;
	private String lastName;
	private String phone;

}