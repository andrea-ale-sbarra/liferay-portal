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

package com.liferay.headless.commerce.delivery.order.internal.dto.v1_0;

import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderNote;
import com.liferay.commerce.service.CommerceOrderNoteService;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.OrderComment;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false,
	property = "dto.class.name=com.liferay.commerce.model.CommerceOrderNote",
	service = {DTOConverter.class, OrderCommentDTOConverter.class}
)
public class OrderCommentDTOConverter
	implements DTOConverter<CommerceOrderNote, OrderComment> {

	@Override
	public String getContentType() {
		return OrderComment.class.getSimpleName();
	}

	@Override
	public OrderComment toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		CommerceOrderNote commerceOrderNote =
			_commerceOrderNoteService.getCommerceOrderNote(
				(Long)dtoConverterContext.getId());

		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			commerceOrderNote.getCommerceOrderId());

		return new OrderComment() {
			{
				author = commerceOrderNote.getUserName();
				content = commerceOrderNote.getContent();
				id = commerceOrderNote.getCommerceOrderNoteId();
				orderId = commerceOrder.getCommerceOrderId();
				restricted = commerceOrderNote.isRestricted();
			}
		};
	}

	@Reference
	private CommerceOrderNoteService _commerceOrderNoteService;

	@Reference
	private CommerceOrderService _commerceOrderService;

}