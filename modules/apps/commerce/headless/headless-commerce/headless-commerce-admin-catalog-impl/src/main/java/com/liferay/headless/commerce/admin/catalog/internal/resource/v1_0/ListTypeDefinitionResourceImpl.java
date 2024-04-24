/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.resource.v1_0;

import com.liferay.commerce.product.model.CPSpecificationOption;
import com.liferay.commerce.product.service.CPSpecificationOptionService;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.ListTypeDefinition;
import com.liferay.headless.commerce.admin.catalog.resource.v1_0.ListTypeDefinitionResource;

import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeDefinitionService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.Collections;
import java.util.Locale;

/**
 * @author Zoltán Takács
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/list-type-definition.properties",
	scope = ServiceScope.PROTOTYPE, service = ListTypeDefinitionResource.class
)
public class ListTypeDefinitionResourceImpl
	extends BaseListTypeDefinitionResourceImpl {


	@Override
	public Page<ListTypeDefinition> getSpecificationIdListTypeDefinitionsPage(
		Long id) throws Exception {
		CPSpecificationOption cpSpecificationOption =
			_cpSpecificationOptionService.getCPSpecificationOption(id);

		return Page.of(Collections.singletonList( _toListTypeDefinition(
			_listTypeDefinitionService.getListTypeDefinition(
				34704))));
	}

	@Reference
	private ListTypeDefinitionService _listTypeDefinitionService;

	private Locale _getLocale() {
		if (contextUser != null) {
			return contextUser.getLocale();
		}

		return contextAcceptLanguage.getPreferredLocale();
	}

	private ListTypeDefinition _toListTypeDefinition(
		com.liferay.list.type.model.ListTypeDefinition
			serviceBuilderListTypeDefinition) {

		if (serviceBuilderListTypeDefinition == null) {
			return null;
		}

		Locale locale = _getLocale();

		return new ListTypeDefinition() {
			{
				setDateCreated(serviceBuilderListTypeDefinition::getCreateDate);
				setDateModified(
					serviceBuilderListTypeDefinition::getModifiedDate);
				setExternalReferenceCode(
					serviceBuilderListTypeDefinition::getExternalReferenceCode);
				setId(
					serviceBuilderListTypeDefinition::getListTypeDefinitionId);
				setName(() -> serviceBuilderListTypeDefinition.getName(locale));
				setName_i18n(
					() -> LocalizedMapUtil.getI18nMap(
						serviceBuilderListTypeDefinition.getNameMap()));
				setSystem(serviceBuilderListTypeDefinition::isSystem);
			}
		};
	}

	@Reference
	private CPSpecificationOptionService _cpSpecificationOptionService;
}