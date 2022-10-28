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
import com.liferay.commerce.product.exception.NoSuchCProductException;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.permission.CommerceProductViewPermission;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.wish.list.model.CommerceWishList;
import com.liferay.commerce.wish.list.model.CommerceWishListItem;
import com.liferay.commerce.wish.list.service.CommerceWishListItemService;
import com.liferay.commerce.wish.list.service.CommerceWishListService;
import com.liferay.headless.commerce.core.util.ServiceContextHelper;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.Product;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.WishList;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.WishListItem;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.ProductDTOConverter;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.ProductDTOConverterContext;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.WishListDTOConverter;
import com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter.WishListItemDTOConverter;
import com.liferay.headless.commerce.delivery.catalog.resource.v1_0.WishListResource;

import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mahmoud Azzam
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/wish-list.properties",
	scope = ServiceScope.PROTOTYPE, service = WishListResource.class
)
public class WishListResourceImpl extends BaseWishListResourceImpl {

	@Override
	public Page<WishList> getChannelWishListsPage(Long channelId, Long accountId,
	  	Pagination pagination) throws Exception {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.fetchCommerceChannel(channelId);

		List<CommerceWishList> commerceWishLists =
			_commerceWishListService.getCommerceWishLists(
				commerceChannel.getSiteGroupId(), contextUser.getUserId(),
				pagination.getStartPosition(), pagination.getEndPosition(),
				null);

		return Page.of(
			_getWishListsWithWishListItems(channelId, accountId, commerceWishLists),
			pagination, _commerceWishListService.getCommerceWishListsCount(
				contextUser.getGroupId(), contextUser.getUserId()));
	}

	@Override
	public WishList getWishList(Long channelId, Long accountId, Long wishListId)
		throws Exception {

		CommerceWishList commerceWishList =
			_commerceWishListService.getCommerceWishList(wishListId);

		return _getWishListWithWishListItems(channelId, accountId, commerceWishList);
	}

	@Override
	public WishList postChannelWishList(Long channelId, Long accountId,
		WishList wishList) throws Exception {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannel(channelId);

		ServiceContext serviceContext = _serviceContextHelper.getServiceContext(
			commerceChannel.getSiteGroupId());

		CommerceWishList commerceWishList = _commerceWishListService.addCommerceWishList(
			wishList.getName(), wishList.getDefaultWishList(), serviceContext
		);

		return _getWishListWithWishListItems(channelId, accountId, commerceWishList);
	}

	@Override
	public WishList patchChannelWishList(Long channelId, Long accountId,
		WishList wishList) throws Exception {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannel(channelId);

		ServiceContext serviceContext = _serviceContextHelper.getServiceContext(
			commerceChannel.getSiteGroupId());

		CommerceWishList commerceWishList = _commerceWishListService.updateCommerceWishList(
			wishList.getId(), wishList.getName(), wishList.getDefaultWishList()
		);

		return _getWishListWithWishListItems(channelId, accountId, commerceWishList);
	}

	@Override
	public Response deleteWishList(Long wishListId)
		throws Exception {
		_commerceWishListService.deleteCommerceWishList(wishListId);

		Response.ResponseBuilder responseBuilder = Response.noContent();

		return responseBuilder.build();
	}

	private List<WishList> _getWishListsWithWishListItems(Long channelId,
	  	Long accountId, List<CommerceWishList> commerceWishLists)
		throws Exception {
		List<WishList> wishLists = new ArrayList<>();

		for (CommerceWishList commerceWishList : commerceWishLists) {

			List<CommerceWishListItem> commerceWishListItems =
				_commerceWishListItemService.getCommerceWishListItems(
					commerceWishList.getCommerceWishListId(),
					Integer.MIN_VALUE, Integer.MAX_VALUE,
					null);

			List<WishListItem> wishListItems =
				_toWishlistItems(channelId, accountId, commerceWishListItems);

			wishLists.add(_toWishList(commerceWishList, wishListItems));
		}
		return wishLists;
	}

	private WishList _getWishListWithWishListItems(Long channelId,
	  	Long accountId, CommerceWishList commerceWishList)
		throws Exception {

		List<CommerceWishListItem> commerceWishListItems =
			_commerceWishListItemService.getCommerceWishListItems(
				commerceWishList.getCommerceWishListId(),
				Integer.MIN_VALUE, Integer.MAX_VALUE,
				null);

		List<WishListItem> wishListItems =
			_toWishlistItems(channelId, accountId, commerceWishListItems);

		return _toWishList(commerceWishList, wishListItems);
	}

	private WishList _toWishList(CommerceWishList commerceWishList,
	 	List<WishListItem> wishListItems) throws Exception {

		return _wishListDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				_dtoConverterRegistry, commerceWishList.getCommerceWishListId(),
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser),
			wishListItems);
	}

	private List<WishListItem> _toWishlistItems(Long channelId, Long accountId,
		List<CommerceWishListItem> commerceWishListItems) throws Exception {

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

	@Reference
	private CommerceWishListService _commerceWishListService;

	@Reference
	private WishListDTOConverter _wishListDTOConverter;

	@Reference
	private ServiceContextHelper _serviceContextHelper;

}