/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.internal.permission;

import com.liferay.account.model.AccountGroupRel;
import com.liferay.account.service.AccountGroupLocalService;
import com.liferay.account.service.AccountGroupRelLocalService;
import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.commerce.product.discovery.CPConfigurationListDiscovery;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.model.CommerceChannelRel;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.product.service.CommerceChannelRelLocalService;
import com.liferay.commerce.product.util.AccountEntryHelper;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 * @author Alessio Antonio Rendina
 */
@Component(service = CommerceProductViewPermission.class)
public class CommerceProductViewPermissionImpl
	implements CommerceProductViewPermission {

	@Override
	public void check(
			PermissionChecker permissionChecker, long commerceAccountId,
			long cpDefinitionId)
		throws PortalException {

		if (contains(permissionChecker, commerceAccountId, cpDefinitionId)) {
			return;
		}

		throw new PrincipalException.MustHavePermission(
			permissionChecker.getUserId(), CPDefinition.class.getName(),
			cpDefinitionId, ActionKeys.VIEW);
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, long commerceAccountId,
			long groupId, long cpDefinitionId)
		throws PortalException {

		if (contains(
				permissionChecker, commerceAccountId, groupId,
				cpDefinitionId)) {

			return;
		}

		throw new PrincipalException.MustHavePermission(
			permissionChecker.getUserId(), CPDefinition.class.getName(),
			cpDefinitionId, ActionKeys.VIEW);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long commerceAccountId,
			long cpDefinitionId)
		throws PortalException {

		return _isAccountEnabled(
			commerceAccountId,
			_cpDefinitionLocalService.getCPDefinition(cpDefinitionId));
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long commerceAccountId,
			long groupId, long cpDefinitionId)
		throws PortalException {

		CPDefinition cpDefinition = _cpDefinitionLocalService.getCPDefinition(
			cpDefinitionId);

		//		if (FeatureFlagManagerUtil.isEnabled("LPD-10889")) {

		// 			CPConfigurationList cpConfigurationList =

		//				_cpConfigurationListDiscovery.getCPConfigurationList(
		//					cpDefinition.getCompanyId(), cpDefinition.getGroupId(),
		//					commerceAccountId, _getCommerceChannelId(groupId), 0);

		//
		//			return cpDefinition.isVisible(
		//				cpConfigurationList.getCPConfigurationListId());
		//		}

		if (!_isChannelEnabled(groupId, cpDefinition) ||
			_isProductExcluded(commerceAccountId, cpDefinition)) {

			return false;
		}

		return _isAccountEnabled(commerceAccountId, cpDefinition);
	}

	private long _getCommerceChannelId(long groupId) {
		Group group = _groupLocalService.fetchGroup(groupId);

		String className = group.getClassName();

		if (className.equals(CommerceChannel.class.getName())) {
			return group.getClassPK();
		}

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.fetchCommerceChannelBySiteGroupId(
				groupId);

		if (commerceChannel != null) {
			return commerceChannel.getCommerceChannelId();
		}

		return 0;
	}

	private boolean _hasMatchingCategory(
		List<Long> ids, List<AssetCategory> categories) {

		if ((ids == null) || ids.isEmpty() || (categories == null) ||
			categories.isEmpty()) {

			return false;
		}

		Set<Long> idSet = new HashSet<>(ids);

		for (AssetCategory category : categories) {
			if (idSet.contains(category.getCategoryId())) {
				return true;
			}
		}

		return false;
	}

	private boolean _isAccountEnabled(
			long commerceAccountId, CPDefinition cpDefinition)
		throws PortalException {

		if (!cpDefinition.isAccountGroupFilterEnabled()) {
			return true;
		}

		List<AccountGroupRel> accountGroupRels =
			_accountGroupRelLocalService.getAccountGroupRels(
				CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		long[] accountGroupIds = _accountGroupLocalService.getAccountGroupIds(
			commerceAccountId);

		for (AccountGroupRel accountGroupRel : accountGroupRels) {
			if (ArrayUtil.contains(
					accountGroupIds, accountGroupRel.getAccountGroupId())) {

				return true;
			}
		}

		return false;
	}

	private boolean _isChannelEnabled(long groupId, CPDefinition cpDefinition) {
		if (!cpDefinition.isChannelFilterEnabled()) {
			return true;
		}

		List<CommerceChannelRel> commerceChannelRels =
			_commerceChannelRelLocalService.getCommerceChannelRels(
				CPDefinition.class.getName(), cpDefinition.getCPDefinitionId(),
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		long commerceChannelId = _getCommerceChannelId(groupId);

		for (CommerceChannelRel commerceChannelRel : commerceChannelRels) {
			if (commerceChannelRel.getCommerceChannelId() ==
					commerceChannelId) {

				return true;
			}
		}

		return false;
	}

	private boolean _isProductExcluded(
			long commerceAccountId, CPDefinition cpDefinition)
		throws PortalException {

		long cpProductId = cpDefinition.getCProductId();

		List<Long> accountExcludedProductsIds =
			_accountEntryHelper.getAccountExcludedProductsIds(
				commerceAccountId);

		for (long excludedId : accountExcludedProductsIds) {
			if (excludedId == cpProductId) {
				return true;
			}
		}

		List<Long> accountExcludedCategoriesIds =
			_accountEntryHelper.getAccountExcludedCategoriesIds(
				commerceAccountId);

		List<AssetCategory> categories =
			assetCategoryLocalService.getCategories(
				"com.liferay.commerce.product.model.CPDefinition",
				cpDefinition.getCPDefinitionId());

		return _hasMatchingCategory(accountExcludedCategoriesIds, categories);
	}

	@Reference
	private AccountEntryHelper _accountEntryHelper;

	@Reference
	private AccountGroupLocalService _accountGroupLocalService;

	@Reference
	private AccountGroupRelLocalService _accountGroupRelLocalService;

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private CommerceChannelRelLocalService _commerceChannelRelLocalService;

	@Reference
	private CPConfigurationListDiscovery _cpConfigurationListDiscovery;

	@Reference
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	AssetCategoryLocalService assetCategoryLocalService;

}