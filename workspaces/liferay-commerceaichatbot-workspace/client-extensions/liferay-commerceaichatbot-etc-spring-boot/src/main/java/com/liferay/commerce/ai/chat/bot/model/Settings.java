/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import org.json.JSONObject;

/**
 * @author Keven Leone
 */
public class Settings {

	public Settings(JSONObject jsonObject) {
		apiKey = jsonObject.getString("apiKey");
		faq = jsonObject.getString("faq");
		modelName = jsonObject.getJSONObject(
			"model"
		).getString(
			"name"
		);
		providerName = jsonObject.getJSONObject(
			"provider"
		).getString(
			"name"
		);
	}

	public String apiKey;
	public String faq;
	public String modelName;
	public String providerName;

}