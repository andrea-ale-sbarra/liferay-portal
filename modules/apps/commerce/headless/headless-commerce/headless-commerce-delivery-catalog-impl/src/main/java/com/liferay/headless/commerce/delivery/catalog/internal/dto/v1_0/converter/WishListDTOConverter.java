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

package com.liferay.headless.commerce.delivery.catalog.internal.dto.v1_0.converter;

import com.liferay.commerce.wish.list.model.CommerceWishList;
import com.liferay.commerce.wish.list.service.CommerceWishListService;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.WishList;
import com.liferay.headless.commerce.delivery.catalog.dto.v1_0.WishListItem;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mahmoud Azzam
 */
@Component(
	property = "dto.class.name=com.liferay.headless.commerce.delivery.catalog.dto.v1_0.WishList",
	service = {WishListDTOConverter.class, DTOConverter.class}
)
public class WishListDTOConverter
	implements DTOConverter<List<WishListItem>, WishList> {

	@Override
	public String getContentType() {
		return WishList.class.getSimpleName();
	}

	@Override
	public WishList toDTO(DTOConverterContext dtoConverterContext,
	  	List<WishListItem> passedWishListItems) throws Exception {

		CommerceWishList commerceWishList =
			_commerceWishListService.getCommerceWishList(
				(Long)dtoConverterContext.getId());

		return new WishList() {
			{
				id = commerceWishList.getCommerceWishListId();
				name = commerceWishList.getName();
				defaultWishList = commerceWishList.getDefaultWishList();
				wishListItems = passedWishListItems.toArray(new WishListItem[0]);
			}
		};
	}

	@Reference
	private CommerceWishListService _commerceWishListService;

}