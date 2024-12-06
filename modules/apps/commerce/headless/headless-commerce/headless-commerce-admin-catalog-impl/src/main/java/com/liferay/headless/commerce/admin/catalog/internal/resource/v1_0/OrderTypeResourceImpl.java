/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.internal.resource.v1_0;

import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.product.model.CPConfigurationListRel;
import com.liferay.commerce.product.service.CPConfigurationListRelService;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.OrderType;
import com.liferay.headless.commerce.admin.catalog.dto.v1_0.ProductConfigurationListOrderType;
import com.liferay.headless.commerce.admin.catalog.resource.v1_0.OrderTypeResource;
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
	properties = "OSGI-INF/liferay/rest/v1_0/order-type.properties",
	property = "nested.field.support=true", scope = ServiceScope.PROTOTYPE,
	service = OrderTypeResource.class
)
public class OrderTypeResourceImpl extends BaseOrderTypeResourceImpl {

	@NestedField(
		parentClass = ProductConfigurationListOrderType.class,
		value = "orderType"
	)
	@Override
	public OrderType getProductConfigurationListOrderTypeOrderType(Long id)
		throws Exception {

		CPConfigurationListRel cpConfigurationListRel =
			_cpConfigurationListRelService.getCPConfigurationListRel(id);

		return _orderTypeDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				cpConfigurationListRel.getClassPK(),
				contextAcceptLanguage.getPreferredLocale()));
	}

	@Reference
	private CPConfigurationListRelService _cpConfigurationListRelService;

	@Reference(
		target = "(component.name=com.liferay.headless.commerce.admin.catalog.internal.dto.v1_0.converter.OrderTypeDTOConverter)"
	)
	private DTOConverter<CommerceOrderType, OrderType> _orderTypeDTOConverter;

}