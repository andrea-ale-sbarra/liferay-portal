/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.display.context;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.pim.site.initializer.connector.PIMConnector;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Andrea Sbarra
 */
public class EditPIMConnectorDisplayContext {

	public EditPIMConnectorDisplayContext(
		HttpServletRequest httpServletRequest,
		ObjectDefinition objectDefinition, List<PIMConnector> pimConnectors) {

		_httpServletRequest = httpServletRequest;
		_objectDefinition = objectDefinition;
		_pimConnectors = pimConnectors;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public Map<String, Object> getReactData() throws Exception {
		return HashMapBuilder.<String, Object>put(
			"apiURL", "/o" + _objectDefinition.getRESTContextPath()
		).put(
			"backURL", _getBackURL()
		).put(
			"connectors",
			JSONUtil.toJSONArray(
				_pimConnectors,
				pimConnector -> JSONUtil.put(
					"key", pimConnector.getKey()
				).put(
					"name", _getPIMConnectorName(pimConnector)
				))
		).put(
			"objectEntryId",
			ParamUtil.getLong(_httpServletRequest, "objectEntryId")
		).build();
	}

	private String _getBackURL() {
		String backURL = ParamUtil.getString(_httpServletRequest, "backURL");

		if (Validator.isNotNull(backURL)) {
			return backURL;
		}

		return _themeDisplay.getURLCurrent();
	}

	private String _getPIMConnectorName(PIMConnector pimConnector) {
		String name = pimConnector.getName(_themeDisplay.getLocale());

		if (Validator.isNotNull(name)) {
			return name;
		}

		return pimConnector.getKey();
	}

	private final HttpServletRequest _httpServletRequest;
	private final ObjectDefinition _objectDefinition;
	private final List<PIMConnector> _pimConnectors;
	private final ThemeDisplay _themeDisplay;

}
