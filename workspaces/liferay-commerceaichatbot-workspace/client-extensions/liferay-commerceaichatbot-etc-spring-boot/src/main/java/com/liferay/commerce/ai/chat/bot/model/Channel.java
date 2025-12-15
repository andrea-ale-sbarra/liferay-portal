/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import java.util.Objects;

/**
 * @author Ivica Cardic
 */
public class Channel {

	public Channel() {
	}

	public Channel(String id, String name, String type, Boolean active) {
		_id = id;
		_name = name;
		_type = type;
		_active = active;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (getClass() != object.getClass())) {
			return false;
		}

		Channel channel = (Channel)object;

		return Objects.equals(_id, channel.getId());
	}

	public Boolean getActive() {
		return _active;
	}

	public String getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public String getType() {
		return _type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_id);
	}

	public void setActive(Boolean active) {
		_active = active;
	}

	public void setId(String id) {
		_id = id;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setType(String type) {
		_type = type;
	}

	@Override
	public String toString() {
		return new StringBuilder(
		).append(
			"Channel{id='"
		).append(
			_id
		).append(
			"\', name='"
		).append(
			_name
		).append(
			"\', type='"
		).append(
			_type
		).append(
			"\', active="
		).append(
			_active
		).append(
			"}"
		).toString();
	}

	private Boolean _active;
	private String _id;
	private String _name;
	private String _type;

}