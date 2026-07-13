/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.fragment.renderer;

import com.liferay.fragment.renderer.FragmentRenderer;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.site.pim.site.initializer.connector.PIMConnectorRegistry;
import com.liferay.site.pim.site.initializer.internal.display.context.EditPIMConnectorDisplayContext;

import jakarta.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(service = FragmentRenderer.class)
public class EditPIMConnectorJSPSectionFragmentRenderer
	extends BaseJSPSectionFragmentRenderer<EditPIMConnectorDisplayContext> {

	@Override
	public String getCollectionKey() {
		return "sections";
	}

	@Override
	public String getLabelKey() {
		return "edit-connector";
	}

	@Override
	protected EditPIMConnectorDisplayContext getDisplayContext(
		HttpServletRequest httpServletRequest) {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)httpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		return new EditPIMConnectorDisplayContext(
			httpServletRequest,
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					"PIM_CONNECTOR", themeDisplay.getCompanyId()),
			_pimConnectorRegistry.getConnectors(themeDisplay.getCompanyId()));
	}

	@Override
	protected String getJSPPath() {
		return "/edit_connector.jsp";
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private PIMConnectorRegistry _pimConnectorRegistry;

}