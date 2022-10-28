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

package com.liferay.headless.commerce.delivery.catalog.internal.resource.v1_0;

import com.liferay.commerce.account.exception.NoSuchAccountException;
import com.liferay.commerce.account.model.CommerceAccount;
import com.liferay.commerce.account.service.CommerceAccountLocalService;
import com.liferay.commerce.account.util.CommerceAccountHelper;
import com.liferay.commerce.context.CommerceContext;
import com.liferay.commerce.context.CommerceContextFactory;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.product.exception.NoSuchCPDefinitionException;
import com.liferay.commerce.product.exception.NoSuchCProductException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.product.util.CPDefinitionHelper;
import com.liferay.commerce.wish.list.model.CommerceWishList;
import com.liferay.commerce.wish.list.model.CommerceWishListItem;
import com.liferay.commerce.wish.list.service.CommerceWishListItemService;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.Product;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.WishList;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.WishListItem;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.PinDTOConverterContext;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.ProductDTOConverter;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.ProductDTOConverterContext;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.WishListDTOConverter;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.WishListItemDTOConverter;
import com.liferay.headless.commerce.delivery.catalog.internal.odata.entity.v1_0.ProductEntityModel;
import com.liferay.headless.commerce.delivery.catalog.resource.v1_0.WishListItemResource;

import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mahmoud Azzam
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/wish-list-item.properties",
	scope = ServiceScope.PROTOTYPE, service = WishListItemResource.class
)
public class WishListItemResourceImpl extends BaseWishListItemResourceImpl {

	@Override
	public Page<WishListItem> getWishListItemsPage(Long channelId, Long accountId,
	   	Long wishListId, Pagination pagination) throws Exception {

		List<CommerceWishListItem> commerceWishListItems =
			_commerceWishListItemService.getCommerceWishListItems(
				wishListId, pagination.getStartPosition(),
				pagination.getEndPosition(), null);

		return Page.of(
			_toWishlistItems(channelId, accountId, commerceWishListItems), pagination,
			_commerceWishListItemService.getCommerceWishListItemsCount(wishListId));
	}

	@Override
	public WishListItem getWishListItem(Long channelId, Long accountId,
		Long wishListId, Long wishListItemId) throws Exception {

		CommerceWishListItem commerceWishListItem =
			_commerceWishListItemService.getCommerceWishListItem(
				wishListItemId);

		return _toWishListItem(channelId, accountId, commerceWishListItem);
	}

	private List<WishListItem> _toWishlistItems(Long channelId, Long accountId,
		List<CommerceWishListItem> commerceWishListItems)
		throws Exception {

		List<WishListItem> wishListItems = new ArrayList<>();

		for (CommerceWishListItem commerceWishListItem : commerceWishListItems) {
			wishListItems.add(_toWishListItem(channelId, accountId, commerceWishListItem));
		}

		return wishListItems;
	}

	private WishListItem _toWishListItem(Long channelId, Long accountId,
	 	CommerceWishListItem commerceWishListItem) throws Exception {

		CPDefinition cpDefinition =
			_cpDefinitionLocalService.fetchCPDefinitionByCProductId(
				commerceWishListItem.getCProductId()
			);

		if (cpDefinition == null) {
			throw new NoSuchCProductException();
		}

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.fetchCommerceChannel(channelId);

		Long commerceAccountId = _getCommerceAccountId(
			accountId, commerceChannel);

		_commerceProductViewPermission.check(
			PermissionThreadLocal.getPermissionChecker(), commerceAccountId,
			commerceChannel.getGroupId(), cpDefinition.getCPDefinitionId());

		Product product = _toProduct(
			_commerceContextFactory.create(
				contextCompany.getCompanyId(), commerceChannel.getGroupId(),
				contextUser.getUserId(), 0, commerceAccountId),
			cpDefinition);

		return _wishListItemDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				_dtoConverterRegistry, commerceWishListItem.getCommerceWishListItemId(),
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser),
			product);
	}

	private Product _toProduct(
		CommerceContext commerceContext, CPDefinition cpDefinition)
		throws Exception {

		return _productDTOConverter.toDTO(
			new ProductDTOConverterContext(
				commerceContext, cpDefinition, cpDefinition.getCPDefinitionId(),
				contextAcceptLanguage.getPreferredLocale()));
	}

	private Long _getCommerceAccountId(
		Long accountId, CommerceChannel commerceChannel)
		throws Exception {

		int countUserCommerceAccounts =
			_commerceAccountHelper.countUserCommerceAccounts(
				contextUser.getUserId(), commerceChannel.getGroupId());

		if (countUserCommerceAccounts > 1) {
			if (accountId == null) {
				throw new NoSuchAccountException();
			}
		}
		else {
			long[] commerceAccountIds =
				_commerceAccountHelper.getUserCommerceAccountIds(
					contextUser.getUserId(), commerceChannel.getGroupId());

			if (commerceAccountIds.length == 0) {
				CommerceAccount commerceAccount =
					_commerceAccountLocalService.getGuestCommerceAccount(
						contextUser.getCompanyId());

				commerceAccountIds = new long[] {
					commerceAccount.getCommerceAccountId()
				};
			}

			return commerceAccountIds[0];
		}

		return accountId;
	}

	@Reference
	private CommerceWishListItemService _commerceWishListItemService;

	@Reference
	private WishListItemDTOConverter _wishListItemDTOConverter;

	@Reference
	private ProductDTOConverter _productDTOConverter;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private CommerceContextFactory _commerceContextFactory;

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private CommerceProductViewPermission _commerceProductViewPermission;

	@Reference
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Reference
	private CommerceAccountHelper _commerceAccountHelper;

	@Reference
	private CommerceAccountLocalService _commerceAccountLocalService;
}