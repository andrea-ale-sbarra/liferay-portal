/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.account.service.AccountGroupRelLocalServiceUtil;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPConfigurationList;
import com.liferay.commerce.product.model.CPConfigurationListRel;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPConfigurationListLocalService;
import com.liferay.commerce.product.service.CPConfigurationListRelLocalServiceUtil;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.AccountGroup;
import com.liferay.headless.commerce.core.util.DateConfig;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * @author Danny Situ
 */
@RunWith(Arquillian.class)
public class AccountGroupResourceTest extends BaseAccountGroupResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_accountEntry = _accountEntryLocalService.addAccountEntry(
			_user.getUserId(), 0, RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), null,
			RandomTestUtil.randomString() + "@liferay.com", null, null,
			"business", 1, _serviceContext);

		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			RandomTestUtil.randomString(),
			AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), "USD", "en_US", false,
			_serviceContext);

		DateConfig dateConfig = DateConfig.toDisplayDateConfig(
			RandomTestUtil.nextDate(), _user.getTimeZone());

		_cpConfigurationList =
			_cpConfigurationListLocalService.addCPConfigurationList(
				RandomTestUtil.randomString(), _commerceCatalog.getGroupId(),
				_user.getUserId(), 0, false, RandomTestUtil.randomString(), 0D,
				dateConfig.getMonth(), dateConfig.getDay(),
				dateConfig.getYear(), dateConfig.getHour(),
				dateConfig.getMinute(), 0, 0, 0, 0, 0, true);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name"};
	}

	@Override
	protected AccountGroup
			testGetProductConfigurationListAccountGroupAccountGroup_addAccountGroup()
		throws Exception {

		return _addAccountGroup();
	}

	@Override
	protected Long
			testGetProductConfigurationListAccountGroupAccountGroup_getProductConfigurationListAccountGroupId()
		throws Exception {

		return _getProductConfigurationListAccountGroupId();
	}

	@Override
	protected AccountGroup testGraphQLAccountGroup_addAccountGroup()
		throws Exception {

		return _addAccountGroup();
	}

	@Override
	protected Long
			testGraphQLGetProductConfigurationListAccountGroupAccountGroup_getProductConfigurationListAccountGroupId()
		throws Exception {

		return _getProductConfigurationListAccountGroupId();
	}

	private AccountGroup _addAccountGroup() throws Exception {
		_accountGroup = _accountGroupLocalService.addAccountGroup(
			_user.getUserId(), RandomTestUtil.randomString(),
			RandomTestUtil.randomString(), _serviceContext);

		AccountGroupRelLocalServiceUtil.addAccountGroupRel(
			_accountGroup.getAccountGroupId(), AccountEntry.class.getName(),
			_accountEntry.getAccountEntryId());

		return new AccountGroup() {
			{
				id = _accountGroup.getAccountGroupId();
				name = _accountGroup.getName();
			}
		};
	}

	private long _getProductConfigurationListAccountGroupId() throws Exception {
		if (_cpConfigurationListRel != null) {
			return _cpConfigurationListRel.getCPConfigurationListRelId();
		}

		_cpConfigurationListRel =
			CPConfigurationListRelLocalServiceUtil.addCPConfigurationListRel(
				_user.getUserId(), AccountGroup.class.getName(),
				_accountGroup.getAccountGroupId(),
				_cpConfigurationList.getCPConfigurationListId());

		return _cpConfigurationListRel.getCPConfigurationListRelId();
	}

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@Inject
	private AccountEntryLocalService _accountEntryLocalService;

	@DeleteAfterTestRun
	private com.liferay.account.model.AccountGroup _accountGroup;

	@Inject
	private AccountGroupLocalService _accountGroupLocalService;

	@DeleteAfterTestRun
	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@DeleteAfterTestRun
	private CPConfigurationList _cpConfigurationList;

	@Inject
	private CPConfigurationListLocalService _cpConfigurationListLocalService;

	@DeleteAfterTestRun
	private CPConfigurationListRel _cpConfigurationListRel;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}