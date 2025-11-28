package com.liferay.commerce.ai.chat.bot.model;

import java.util.Objects;

public class Channel {

	public Channel() {
	}

	public Channel(String id, String name, String type, Boolean active) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.active = active;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)

			return true;

		if ((o == null) || (getClass() != o.getClass()))

			return false;
		Channel channel = (Channel)o;

		return Objects.equals(id, channel.id);
	}

	public Boolean getActive() {
		return active;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Channel{" + "id='" + id + '\'' + ", name='" + name + '\'' +
			", type='" + type + '\'' + ", active=" + active + '}';
	}

	private Boolean active;
	private String id;
	private String name;
	private String type;

}