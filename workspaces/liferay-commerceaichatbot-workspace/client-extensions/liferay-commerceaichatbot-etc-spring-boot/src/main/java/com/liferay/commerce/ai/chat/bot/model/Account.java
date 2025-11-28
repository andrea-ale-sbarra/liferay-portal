package com.liferay.commerce.ai.chat.bot.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class Account {

	public Account() {
	}

	public Account(String id, String name, String type, String status) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)

			return true;

		if ((o == null) || (getClass() != o.getClass()))

			return false;
		Account account = (Account)o;

		return Objects.equals(id, account.id);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public String getType() {
		return type;
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

	public void setStatus(String status) {
		this.status = status;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Account{" + "id='" + id + '\'' + ", name='" + name + '\'' +
			", type='" + type + '\'' + ", status='" + status + '\'' + '}';
	}

	private String id;
	private String name;
	private String status;
	private String type;

}