/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.type.virtual.order.service.impl;

import com.liferay.commerce.product.type.virtual.order.model.CommerceVirtualOrderItemFileEntry;
import com.liferay.commerce.product.type.virtual.order.service.base.CommerceVirtualOrderItemFileEntryLocalServiceBaseImpl;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	property = "model.class.name=com.liferay.commerce.product.type.virtual.order.model.CommerceVirtualOrderItemFileEntry",
	service = AopService.class
)
public class CommerceVirtualOrderItemFileEntryLocalServiceImpl
	extends CommerceVirtualOrderItemFileEntryLocalServiceBaseImpl {

	@Override
	public CommerceVirtualOrderItemFileEntry
			addCommerceVirtualOrderItemFileEntry(
				long userId, long groupId, long commerceOrderItemId,
				long fileEntryId, String Url, String version)
		throws PortalException {

		User user = _userLocalService.getUser(userId);

		long commerceVirtualOrderItemFileEntryId =
			counterLocalService.increment();

		CommerceVirtualOrderItemFileEntry commerceVirtualOrderItemFileEntry =
			commerceVirtualOrderItemFileEntryPersistence.create(
				commerceVirtualOrderItemFileEntryId);

		commerceVirtualOrderItemFileEntry.setGroupId(groupId);
		commerceVirtualOrderItemFileEntry.setCompanyId(user.getCompanyId());
		commerceVirtualOrderItemFileEntry.setUserId(user.getUserId());
		commerceVirtualOrderItemFileEntry.setUserName(user.getFullName());
		commerceVirtualOrderItemFileEntry.setCommerceVirtualOrderItemId(
			commerceOrderItemId);

		return commerceVirtualOrderItemFileEntryPersistence.update(
			commerceVirtualOrderItemFileEntry);
	}

	@Reference
	private UserLocalService _userLocalService;

}