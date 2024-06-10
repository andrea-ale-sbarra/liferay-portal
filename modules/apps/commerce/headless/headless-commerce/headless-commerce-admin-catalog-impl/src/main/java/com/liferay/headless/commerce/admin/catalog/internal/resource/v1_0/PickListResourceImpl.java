/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.resource.v1_0;

import com.liferay.commerce.product.model.CPSpecificationOption;
import com.liferay.commerce.product.service.CPSpecificationOptionService;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.PickList;
import com.liferay.headless.commerce.admin.catalog.resource.v1_0.PickListResource;

import com.liferay.headless.commerce.core.util.ServiceContextHelper;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.service.ListTypeDefinitionService;
import com.liferay.petra.string.StringPool;
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
	properties = "OSGI-INF/liferay/rest/v1_0/pick-list.properties",
	scope = ServiceScope.PROTOTYPE, service = PickListResource.class
)
public class PickListResourceImpl extends BasePickListResourceImpl {

	@Override
	public Page<PickList> getSpecificationIdPickListsPage(Long id)
		throws Exception {
		CPSpecificationOption cpSpecificationOption =
			_cpSpecificationOptionService.getCPSpecificationOption(id);

		long listTypeDefinitionId =
			cpSpecificationOption.getListTypeDefinitionId();

		if (listTypeDefinitionId == 0) {
			return super.getSpecificationIdPickListsPage(id);
		}

		ListTypeDefinition listTypeDefinition =
			_listTypeDefinitionService.getListTypeDefinition(
				listTypeDefinitionId);

		return Page.of(
			Collections.singletonList(_toPickList(listTypeDefinition))
		);
	}

	@Override
	public PickList postSpecificationIdPickList(Long id, PickList pickList)
		throws Exception {

		ListTypeDefinition listTypeDefinition =
			_listTypeDefinitionService.addListTypeDefinition(
				StringPool.BLANK,
				HashMapBuilder.put(_getLocale(),
					pickList.getName()).build(), false,
				Collections.emptyList());

		CPSpecificationOption cpSpecificationOption =
			_cpSpecificationOptionService.getCPSpecificationOption(id);

		_cpSpecificationOptionService.updateCPSpecificationOption(
			cpSpecificationOption.getCPSpecificationOptionId(),
			cpSpecificationOption.getCPOptionCategoryId(),
			listTypeDefinition.getListTypeDefinitionId(),
			cpSpecificationOption.getTitleMap(),
			cpSpecificationOption.getDescriptionMap(),
			cpSpecificationOption.getFacetable(),
			cpSpecificationOption.getKey(),
			cpSpecificationOption.getPriority(),
			_serviceContextHelper.getServiceContext()
		);

		return _toPickList(listTypeDefinition);
	}

	@Reference
	private ListTypeDefinitionService _listTypeDefinitionService;

	private Locale _getLocale() {
		if (contextUser != null) {
			return contextUser.getLocale();
		}

		return contextAcceptLanguage.getPreferredLocale();
	}

	private PickList _toPickList(
		ListTypeDefinition
			listTypeDefinition) {

		if (listTypeDefinition == null) {
			return null;
		}

		Locale locale = _getLocale();

		return new PickList() {
			{
				setDateCreated(listTypeDefinition::getCreateDate);
				setDateModified(
					listTypeDefinition::getModifiedDate);
				setExternalReferenceCode(
					listTypeDefinition::getExternalReferenceCode);
				setId(
					listTypeDefinition::getListTypeDefinitionId);
				setName(() -> listTypeDefinition.getName(locale));
				setName_i18n(
					() -> LocalizedMapUtil.getI18nMap(
						listTypeDefinition.getNameMap()));
				setSystem(listTypeDefinition::isSystem);
			}
		};
	}
	@Reference
	private ServiceContextHelper _serviceContextHelper;

	@Reference
	private CPSpecificationOptionService _cpSpecificationOptionService;
}