/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import java.util.List;

/**
 * @author Ivica Cardic
 */
public class AccountBrief {

	public String getExternalReferenceCode() {
		return _externalReferenceCode;
	}

	public long getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public List<RoleBrief> getRoleBriefs() {
		return _roleBriefs;
	}

	public String getType() {
		return _type;
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

	public void setRoleBriefs(List<RoleBrief> roleBriefs) {
		_roleBriefs = roleBriefs;
	}

	public void setType(String type) {
		_type = type;
	}

	private String _externalReferenceCode;
	private long _id;
	private String _name;
	private List<RoleBrief> _roleBriefs;
	private String _type;

}