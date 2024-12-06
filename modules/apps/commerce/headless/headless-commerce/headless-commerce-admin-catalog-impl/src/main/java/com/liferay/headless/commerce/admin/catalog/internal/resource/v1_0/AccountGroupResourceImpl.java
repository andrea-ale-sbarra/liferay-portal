/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.resource.v1_0;

import com.liferay.commerce.product.model.CPConfigurationListRel;
import com.liferay.commerce.product.service.CPConfigurationListRelService;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.AccountGroup;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductConfigurationListAccountGroup;
import com.liferay.headless.commerce.admin.catalog.resource.v1_0.AccountGroupResource;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.fields.NestedField;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Danny Situ
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/account-group.properties",
	property = "nested.field.support=true", scope = ServiceScope.PROTOTYPE,
	service = AccountGroupResource.class
)
public class AccountGroupResourceImpl extends BaseAccountGroupResourceImpl {

	@NestedField(
		parentClass = ProductConfigurationListAccountGroup.class,
		value = "accountGroup"
	)
	@Override
	public AccountGroup getProductConfigurationListAccountGroupAccountGroup(
			Long id)
		throws Exception {

		CPConfigurationListRel cpConfigurationListRel =
			_cpConfigurationListRelService.getCPConfigurationListRel(id);

		return _accountGroupDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				cpConfigurationListRel.getClassPK(),
				contextAcceptLanguage.getPreferredLocale()));
	}

	@Reference(
		target = "(component.name=com.liferay.headless.commerce.admin.catalog.internal.dto.v1_0.converter.AccountGroupDTOConverter)"
	)
	private DTOConverter<com.liferay.account.model.AccountGroup, AccountGroup>
		_accountGroupDTOConverter;

	@Reference
	private CPConfigurationListRelService _cpConfigurationListRelService;

}