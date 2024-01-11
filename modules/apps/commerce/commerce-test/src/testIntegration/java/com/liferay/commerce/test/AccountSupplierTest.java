package com.liferay.commerce.test;

import com.liferay.account.constants.AccountConstants;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.model.AccountGroup;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.commerce.account.test.util.CommerceAccountTestUtil;
import com.liferay.commerce.currency.model.CommerceCurrency;
import com.liferay.commerce.currency.test.util.CommerceCurrencyTestUtil;
import com.liferay.commerce.price.CommerceOrderPriceCalculation;
import com.liferay.commerce.price.CommerceProductPriceCalculation;
import com.liferay.commerce.price.list.service.CommercePriceListAccountRelLocalService;
import com.liferay.commerce.pricing.service.CommercePriceModifierLocalService;
import com.liferay.commerce.pricing.service.CommercePriceModifierRelLocalService;
import com.liferay.commerce.pricing.service.CommercePricingClassCPDefinitionRelLocalService;
import com.liferay.commerce.pricing.service.CommercePricingClassLocalService;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * @author Andrea Sbarra
 */
@RunWith(Arquillian.class)
public class AccountSupplierTest {

	@ClassRule
	@Rule
	public static AggregateTestRule aggregateTestRule = new AggregateTestRule(
		new LiferayIntegrationTestRule(),
		PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_user = UserTestUtil.addUser();

		_serviceContext = ServiceContextTestUtil.getServiceContext(
			_group.getCompanyId(), _group.getGroupId(), _user.getUserId());

		_accountEntry = CommerceAccountTestUtil.addSupplierAccountEntry(
			_user.getUserId(), "SUPPLIER", "supplier@liferay.com",
				_serviceContext);
	}


	private static final int _SCALE = 10;

	private static User _user;

	@DeleteAfterTestRun
	private AccountEntry _accountEntry;

	@Inject
	private AccountGroupLocalService _accountGroupLocalService;

	@Inject
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private CommerceCurrency _commerceCurrency;

	@Inject
	private CommerceOrderLocalService _commerceOrderLocalService;

	@Inject
	private CommerceOrderPriceCalculation _commerceOrderPriceCalculation;

	@Inject
	private CommercePriceListAccountRelLocalService
		_commercePriceListAccountRelLocalService;

	@Inject
	private CommercePriceModifierLocalService
		_commercePriceModifierLocalService;

	@Inject
	private CommercePriceModifierRelLocalService
		_commercePriceModifierRelLocalService;

	@Inject
	private CommercePricingClassCPDefinitionRelLocalService
		_commercePricingClassCPDefinitionRelLocalService;

	@Inject
	private CommercePricingClassLocalService _commercePricingClassLocalService;

	@Inject
	private CommerceProductPriceCalculation _commerceProductPriceCalculation;

	private Group _group;
	private ServiceContext _serviceContext;

}
