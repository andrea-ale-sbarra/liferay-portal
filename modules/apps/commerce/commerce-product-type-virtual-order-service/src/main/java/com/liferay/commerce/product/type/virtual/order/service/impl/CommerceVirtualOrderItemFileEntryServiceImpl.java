/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.type.virtual.order.service.impl;

import com.liferay.commerce.product.type.virtual.order.service.base.CommerceVirtualOrderItemFileEntryServiceBaseImpl;
import com.liferay.portal.aop.AopService;

import org.osgi.service.component.annotations.Component;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = {
		"json.web.service.context.name=commerce",
		"json.web.service.context.path=CommerceVirtualOrderItemFileEntry"
	},
	service = AopService.class
)
public class CommerceVirtualOrderItemFileEntryServiceImpl
	extends CommerceVirtualOrderItemFileEntryServiceBaseImpl {
}