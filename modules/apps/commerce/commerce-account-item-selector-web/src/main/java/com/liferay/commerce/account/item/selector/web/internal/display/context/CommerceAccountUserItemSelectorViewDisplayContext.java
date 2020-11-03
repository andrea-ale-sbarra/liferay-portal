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

package com.liferay.commerce.account.item.selector.web.internal.display.context;

import com.liferay.commerce.account.item.selector.web.internal.display.context.util.CommerceAccountItemSelectorRequestHelper;
import com.liferay.commerce.account.item.selector.web.internal.search.CommerceAccountUserItemSelectorChecker;
import com.liferay.commerce.account.model.CommerceAccount;
import com.liferay.commerce.account.service.CommerceAccountService;
import com.liferay.commerce.account.service.CommerceAccountUserRelLocalService;
import com.liferay.portal.kernel.dao.search.RowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portlet.usersadmin.search.UserSearch;
import com.liferay.portlet.usersadmin.search.UserSearchTerms;
import com.liferay.users.admin.kernel.util.UsersAdmin;

import java.util.List;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Andrea Sbarra
 */
public class CommerceAccountUserItemSelectorViewDisplayContext {

	public CommerceAccountUserItemSelectorViewDisplayContext(
		CommerceAccountUserRelLocalService commerceAccountUserRelLocalService,
		UsersAdmin usersAdmin, CommerceAccountService commerceAccountService,
		UserLocalService userLocalService,
		HttpServletRequest httpServletRequest, PortletURL portletURL,
		String itemSelectedEventName) {

		_commerceAccountUserRelLocalService =
			commerceAccountUserRelLocalService;
		_usersAdmin = usersAdmin;
		_commerceAccountService = commerceAccountService;
		_userLocalService = userLocalService;
		_portletURL = portletURL;
		_commerceAccountItemSelectorRequestHelper =
			new CommerceAccountItemSelectorRequestHelper(httpServletRequest);
		_itemSelectedEventName = itemSelectedEventName;

		_portletURL.setParameter(
			"commerceAccountId",
			ParamUtil.getString(
				_commerceAccountItemSelectorRequestHelper.getRenderRequest(),
				"commerceAccountId"));
	}

	public String getItemSelectedEventName() {
		return _itemSelectedEventName;
	}

	public String getOrderByCol() {
		return ParamUtil.getString(
			_commerceAccountItemSelectorRequestHelper.getRenderRequest(),
			SearchContainer.DEFAULT_ORDER_BY_COL_PARAM, "createDate_sortable");
	}

	public String getOrderByType() {
		return ParamUtil.getString(
			_commerceAccountItemSelectorRequestHelper.getRenderRequest(),
			SearchContainer.DEFAULT_ORDER_BY_TYPE_PARAM, "desc");
	}

	public PortletURL getPortletURL() {
		return _portletURL;
	}

	public SearchContainer<User> getSearchContainer() throws PortalException {
		if (_searchContainer != null) {
			return _searchContainer;
		}

		_searchContainer = new UserSearch(
			_commerceAccountItemSelectorRequestHelper.
				getLiferayPortletRequest(),
			getPortletURL());

		_searchContainer.setEmptyResultsMessage("no-users-were-found");

		OrderByComparator<User> orderByComparator =
			_usersAdmin.getUserOrderByComparator(
				getOrderByCol(), getOrderByType());

		RowChecker rowChecker = new CommerceAccountUserItemSelectorChecker(
			_commerceAccountItemSelectorRequestHelper.getRenderResponse(),
			getCommerceAccount(), _commerceAccountUserRelLocalService);

		_searchContainer.setOrderByCol(getOrderByCol());
		_searchContainer.setOrderByComparator(orderByComparator);
		_searchContainer.setOrderByType(getOrderByType());
		_searchContainer.setRowChecker(rowChecker);

		UserSearchTerms userSearchTerms =
			(UserSearchTerms)_searchContainer.getSearchTerms();

		long companyId = CompanyThreadLocal.getCompanyId();
		String keywords = userSearchTerms.getKeywords();
		int status = userSearchTerms.getStatus();

		int total = _userLocalService.searchCount(
			companyId, keywords, status, null);

		_searchContainer.setTotal(total);

		List<User> results = _userLocalService.search(
			companyId, keywords, status, null, _searchContainer.getStart(),
			_searchContainer.getEnd(), orderByComparator);

		_searchContainer.setResults(results);

		return _searchContainer;
	}

	protected CommerceAccount getCommerceAccount() throws PortalException {
		long commerceAccountId = ParamUtil.getLong(
			_commerceAccountItemSelectorRequestHelper.getRenderRequest(),
			"commerceAccountId");

		if (commerceAccountId > 0) {
			return _commerceAccountService.getCommerceAccount(
				commerceAccountId);
		}

		return null;
	}

	private final CommerceAccountItemSelectorRequestHelper
		_commerceAccountItemSelectorRequestHelper;
	private final CommerceAccountService _commerceAccountService;
	private final CommerceAccountUserRelLocalService
		_commerceAccountUserRelLocalService;
	private final String _itemSelectedEventName;
	private final PortletURL _portletURL;
	private SearchContainer<User> _searchContainer;
	private final UserLocalService _userLocalService;
	private final UsersAdmin _usersAdmin;

}