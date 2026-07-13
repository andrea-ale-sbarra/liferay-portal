/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.display.context;

import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.data.set.model.FDSActionDropdownItemBuilder;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenu;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.CreationMenuBuilder;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.WebKeys;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * @author Andrea Sbarra
 */
public class ViewPIMConnectorsDisplayContext {

	public ViewPIMConnectorsDisplayContext(
		HttpServletRequest httpServletRequest,
		ObjectDefinition objectDefinition) {

		_httpServletRequest = httpServletRequest;
		_objectDefinition = objectDefinition;

		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public String getAPIURL() {
		return StringBundler.concat(
			"/o/search/v1.0/search?emptySearch=true&filter=",
			"objectDefinitionId eq ", _objectDefinition.getObjectDefinitionId(),
			"&nestedFields=embedded");
	}

	public CreationMenu getCreationMenu() {
		return CreationMenuBuilder.addPrimaryDropdownItem(
			dropdownItem -> {
				dropdownItem.setHref(_getEditURL());
				dropdownItem.setLabel(
					LanguageUtil.get(_httpServletRequest, "new-connector"));
			}
		).build();
	}

	public Map<String, Object> getEmptyState() {
		return HashMapBuilder.<String, Object>put(
			"description",
			LanguageUtil.get(
				_httpServletRequest,
				"deploy-a-connector-client-extension-and-configure-it-here")
		).put(
			"title", LanguageUtil.get(_httpServletRequest, "no-connectors-yet")
		).build();
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems() {
		return ListUtil.fromArray(
			FDSActionDropdownItemBuilder.setHref(
				StringBundler.concat(
					_getEditURL(), "&objectEntryId={embedded.id}")
			).setIcon(
				"pencil"
			).setLabel(
				LanguageUtil.get(_httpServletRequest, "edit")
			).setMethod(
				"get"
			).setPermissionKey(
				"update"
			).build(
				"edit"
			),
			FDSActionDropdownItemBuilder.setHref(
				"/o/pim/map-to-commerce/{embedded.id}"
			).setIcon(
				"upload-multiple"
			).setLabel(
				LanguageUtil.get(_httpServletRequest, "map-to-commerce")
			).setMethod(
				"get"
			).setPermissionKey(
				"update"
			).setTarget(
				"async"
			).build(
				"mapToCommerce"
			),
			FDSActionDropdownItemBuilder.setConfirmationMessage(
				LanguageUtil.get(
					_httpServletRequest, "are-you-sure-you-want-to-delete-this")
			).setHref(
				"{embedded.actions.delete.href}"
			).setIcon(
				"trash"
			).setLabel(
				LanguageUtil.get(_httpServletRequest, "delete")
			).setMethod(
				"delete"
			).setPermissionKey(
				"delete"
			).setTarget(
				"headless"
			).build(
				"delete"
			));
	}

	private String _getEditURL() {
		return StringBundler.concat(
			_themeDisplay.getPathFriendlyURLPublic(),
			_themeDisplay.getScopeGroup(
			).getFriendlyURL(),
			"/edit-connector?backURL=", _themeDisplay.getURLCurrent());
	}

	private final HttpServletRequest _httpServletRequest;
	private final ObjectDefinition _objectDefinition;
	private final ThemeDisplay _themeDisplay;

}