/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.model.CommerceOrderType;
import com.liferay.commerce.product.model.CPConfigurationList;
import com.liferay.commerce.product.model.CPConfigurationListRel;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CPConfigurationListLocalService;
import com.liferay.commerce.product.service.CPConfigurationListRelLocalServiceUtil;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.service.CommerceOrderTypeLocalServiceUtil;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.OrderType;
import com.liferay.headless.commerce.core.util.DateConfig;
import com.liferay.headless.commerce.core.util.LanguageUtils;
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
public class OrderTypeResourceTest extends BaseOrderTypeResourceTestCase {

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_user = UserTestUtil.addUser(testCompany);

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			testCompany.getCompanyId(), testGroup.getGroupId(),
			_user.getUserId());

		_commerceCatalog = _commerceCatalogLocalService.addCommerceCatalog(
			RandomTestUtil.randomString(),
			AccountConstants.ACCOUNT_ENTRY_ID_DEFAULT,
			RandomTestUtil.randomString(), "USD", "en_US", false,
			_serviceContext);

		DateConfig displayDateConfig = DateConfig.toDisplayDateConfig(
			RandomTestUtil.nextDate(), _user.getTimeZone());
		DateConfig expirationDateConfig = DateConfig.toExpirationDateConfig(
			RandomTestUtil.nextDate(), _user.getTimeZone());

		_commerceOrderType =
			CommerceOrderTypeLocalServiceUtil.addCommerceOrderType(
				RandomTestUtil.randomString(), _user.getUserId(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomLocaleStringMap(),
				RandomTestUtil.randomBoolean(), displayDateConfig.getMonth(),
				displayDateConfig.getDay(), displayDateConfig.getYear(),
				displayDateConfig.getHour(), displayDateConfig.getMinute(), 0,
				expirationDateConfig.getMonth(), expirationDateConfig.getDay(),
				expirationDateConfig.getYear(), expirationDateConfig.getHour(),
				expirationDateConfig.getMinute(), true, _serviceContext);

		_cpConfigurationList =
			_cpConfigurationListLocalService.addCPConfigurationList(
				RandomTestUtil.randomString(), _commerceCatalog.getGroupId(),
				_user.getUserId(), 0, false, RandomTestUtil.randomString(), 0D,
				displayDateConfig.getMonth(), displayDateConfig.getDay(),
				displayDateConfig.getYear(), displayDateConfig.getHour(),
				displayDateConfig.getMinute(), 0, 0, 0, 0, 0, true);
	}

	@Override
	protected String[] getAdditionalAssertFieldNames() {
		return new String[] {"name"};
	}

	@Override
	protected OrderType
			testGetProductConfigurationListOrderTypeOrderType_addOrderType()
		throws Exception {

		return _addOrderType();
	}

	@Override
	protected Long
			testGetProductConfigurationListOrderTypeOrderType_getProductConfigurationListOrderTypeId()
		throws Exception {

		return _getProductConfigurationListOrderTypeId();
	}

	@Override
	protected Long
			testGraphQLGetProductConfigurationListOrderTypeOrderType_getProductConfigurationListOrderTypeId()
		throws Exception {

		return _getProductConfigurationListOrderTypeId();
	}

	@Override
	protected OrderType testGraphQLOrderType_addOrderType() throws Exception {
		return _addOrderType();
	}

	private OrderType _addOrderType() throws Exception {
		return new OrderType() {
			{
				id = _commerceOrderType.getCommerceOrderTypeId();
				name = LanguageUtils.getLanguageIdMap(
					_commerceOrderType.getNameMap());
			}
		};
	}

	private long _getProductConfigurationListOrderTypeId() throws Exception {
		if (_cpConfigurationListRel != null) {
			return _cpConfigurationListRel.getCPConfigurationListRelId();
		}

		_cpConfigurationListRel =
			CPConfigurationListRelLocalServiceUtil.addCPConfigurationListRel(
				_user.getUserId(), CommerceOrderType.class.getName(),
				_commerceOrderType.getCommerceOrderTypeId(),
				_cpConfigurationList.getCPConfigurationListId());

		return _cpConfigurationListRel.getCPConfigurationListRelId();
	}

	@DeleteAfterTestRun
	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@DeleteAfterTestRun
	private CommerceOrderType _commerceOrderType;

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