/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.headless.commerce.admin.catalog.resource.v1_0.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.product.model.CPConfigurationList;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.model.CommerceChannelRel;
import com.liferay.commerce.product.service.CPConfigurationListLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.product.service.CommerceChannelRelLocalServiceUtil;
import com.liferay.commerce.test.util.CommerceTestUtil;
import com.liferay.headless.commerce.admin.catalog.client.dto.v1_0.Channel;
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
public class ChannelResourceTest extends BaseChannelResourceTestCase {

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
		return new String[] {
			"externalReferenceCode", "name", "siteGroupId", "type"
		};
	}

	@Override
	protected Channel testGetProductConfigurationListChannelChannel_addChannel()
		throws Exception {

		return _addChannel();
	}

	@Override
	protected Long
			testGetProductConfigurationListChannelChannel_getProductConfigurationListChannelId()
		throws Exception {

		return _getProductConfigurationListChannelId();
	}

	@Override
	protected Channel testGraphQLChannel_addChannel() throws Exception {
		return _addChannel();
	}

	@Override
	protected Long
			testGraphQLGetProductConfigurationListChannelChannel_getProductConfigurationListChannelId()
		throws Exception {

		return _getProductConfigurationListChannelId();
	}

	private Channel _addChannel() throws Exception {
		_commerceChannel = CommerceTestUtil.addCommerceChannel(
			testGroup.getGroupId(), "USD");

		return new Channel() {
			{
				currencyCode = _commerceChannel.getCommerceCurrencyCode();
				externalReferenceCode =
					_commerceChannel.getExternalReferenceCode();
				id = _commerceChannel.getCommerceChannelId();
				name = _commerceChannel.getName();
				siteGroupId = _commerceChannel.getSiteGroupId();
				type = _commerceChannel.getType();
			}
		};
	}

	private long _getProductConfigurationListChannelId() throws Exception {
		if (_commerceChannelRel != null) {
			return _commerceChannelRel.getCommerceChannelRelId();
		}

		_commerceChannelRel =
			CommerceChannelRelLocalServiceUtil.addCommerceChannelRel(
				CPConfigurationList.class.getName(),
				_cpConfigurationList.getCPConfigurationListId(),
				_commerceChannel.getCommerceChannelId(), _serviceContext);

		return _commerceChannelRel.getCommerceChannelRelId();
	}

	@DeleteAfterTestRun
	private CommerceCatalog _commerceCatalog;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@DeleteAfterTestRun
	private CommerceChannel _commerceChannel;

	@DeleteAfterTestRun
	private CommerceChannelRel _commerceChannelRel;

	@DeleteAfterTestRun
	private CPConfigurationList _cpConfigurationList;

	@Inject
	private CPConfigurationListLocalService _cpConfigurationListLocalService;

	private ServiceContext _serviceContext;

	@DeleteAfterTestRun
	private User _user;

}