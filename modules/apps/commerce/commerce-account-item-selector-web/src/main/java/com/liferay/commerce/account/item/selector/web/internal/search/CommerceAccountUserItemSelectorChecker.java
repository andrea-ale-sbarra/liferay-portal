/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.account.item.selector.web.internal.search;

import com.liferay.commerce.account.model.CommerceAccount;
import com.liferay.commerce.account.model.CommerceAccountUserRel;
import com.liferay.commerce.account.service.CommerceAccountUserRelLocalService;
import com.liferay.commerce.account.service.persistence.CommerceAccountUserRelPK;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.model.User;

import javax.portlet.RenderResponse;

/**
 * @author Andrea Sbarra
 */
public class CommerceAccountUserItemSelectorChecker
	extends EmptyOnClickRowChecker {

	public CommerceAccountUserItemSelectorChecker(
		RenderResponse renderResponse, CommerceAccount commerceAccount,
		CommerceAccountUserRelLocalService commerceAccountUserRelLocalService) {

		super(renderResponse);

		_commerceAccount = commerceAccount;
		_commerceAccountUserRelLocalService =
			commerceAccountUserRelLocalService;
	}

	@Override
	public boolean isChecked(Object object) {
		if (_commerceAccount == null) {
			return false;
		}

		User user = (User)object;

		CommerceAccountUserRelPK commerceAccountUserRelPK =
			new CommerceAccountUserRelPK(
				_commerceAccount.getCommerceAccountId(), user.getUserId());

		CommerceAccountUserRel commerceAccountUserRel =
			_commerceAccountUserRelLocalService.fetchCommerceAccountUserRel(
				commerceAccountUserRelPK);

		if (commerceAccountUserRel == null) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isDisabled(Object object) {
		return isChecked(object);
	}

	private final CommerceAccount _commerceAccount;
	private final CommerceAccountUserRelLocalService
		_commerceAccountUserRelLocalService;

}