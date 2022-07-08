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

package com.liferay.headless.commerce.delivery.order.internal.resource.v1_0;

import com.liferay.commerce.exception.NoSuchOrderException;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderNote;
import com.liferay.commerce.service.CommerceOrderNoteService;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.headless.commerce.core.util.ServiceContextHelper;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Order;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.OrderComment;
import com.liferay.headless.commerce.delivery.order.internal.dto.v1_0.OrderCommentDTOConverter;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.OrderCommentResource;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.fields.NestedField;
import com.liferay.portal.vulcan.fields.NestedFieldId;
import com.liferay.portal.vulcan.fields.NestedFieldSupport;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false,
	properties = "OSGI-INF/liferay/rest/v1_0/order-comment.properties",
	scope = ServiceScope.PROTOTYPE,
	service = {NestedFieldSupport.class, OrderCommentResource.class}
)
public class OrderCommentResourceImpl
	extends BaseOrderCommentResourceImpl implements NestedFieldSupport {

	@Override
	public OrderComment getOrderComment(Long orderCommentId) throws Exception {
		CommerceOrderNote commerceOrderNote =
			_commerceOrderNoteService.getCommerceOrderNote(orderCommentId);

		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			commerceOrderNote.getCommerceOrderId());

		if (commerceOrder.isOpen()) {
			throw new NoSuchOrderException();
		}

		return _toOrderComment(orderCommentId);
	}

	@NestedField(parentClass = Order.class, value = "orderComments")
	@Override
	public Page<OrderComment> getOrderCommentsPage(
			@NestedFieldId("id") Long orderId, Pagination pagination)
		throws Exception {

		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			orderId);

		if (commerceOrder.isOpen()) {
			throw new NoSuchOrderException();
		}

		List<CommerceOrderNote> commerceOrderNotes =
			_commerceOrderNoteService.getCommerceOrderNotes(orderId, false);

		int totalItems = _commerceOrderNoteService.getCommerceOrderNotesCount(
			orderId, false);

		return Page.of(
			_toOrderComments(commerceOrderNotes), pagination, totalItems);
	}

	private OrderComment _toOrderComment(Long commerceOrderNoteId)
		throws Exception {

		return _orderCommentDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				_dtoConverterRegistry, commerceOrderNoteId,
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser));
	}

	private List<OrderComment> _toOrderComments(
			List<CommerceOrderNote> commerceOrderNotes)
		throws Exception {

		List<OrderComment> orders = new ArrayList<>();

		for (CommerceOrderNote commerceOrderNote : commerceOrderNotes) {
			orders.add(
				_toOrderComment(commerceOrderNote.getCommerceOrderNoteId()));
		}

		return orders;
	}

	@Reference
	private CommerceOrderNoteService _commerceOrderNoteService;

	@Reference
	private CommerceOrderService _commerceOrderService;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private OrderCommentDTOConverter _orderCommentDTOConverter;

	@Reference
	private ServiceContextHelper _serviceContextHelper;

}