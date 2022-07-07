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

import com.liferay.commerce.context.CommerceContextFactory;
import com.liferay.commerce.exception.NoSuchOrderException;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.service.CommerceOrderItemService;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.headless.commerce.core.util.ServiceContextHelper;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Order;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.OrderItem;
import com.liferay.headless.commerce.delivery.order.internal.dto.v1_0.OrderItemDTOConverter;
import com.liferay.headless.commerce.delivery.order.internal.dto.v1_0.OrderItemDTOConverterContext;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.OrderItemResource;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.vulcan.fields.NestedField;
import com.liferay.portal.vulcan.fields.NestedFieldId;
import com.liferay.portal.vulcan.fields.NestedFieldSupport;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.TransformUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false,
	properties = "OSGI-INF/liferay/rest/v1_0/order-item.properties",
	scope = ServiceScope.PROTOTYPE,
	service = {NestedFieldSupport.class, OrderItemResource.class}
)
public class OrderItemResourceImpl
	extends BaseOrderItemResourceImpl implements NestedFieldSupport {

	@Override
	public OrderItem getOrderItem(Long orderItemId) throws Exception {
		CommerceOrderItem commerceOrderItem =
			_commerceOrderItemService.getCommerceOrderItem(orderItemId);

		CommerceOrder commerceOrder = commerceOrderItem.getCommerceOrder();

		if (commerceOrder.isOpen()) {
			throw new NoSuchOrderException();
		}

		return _toOrderItem(
			commerceOrder.getCommerceAccountId(), commerceOrderItem);
	}

	@NestedField(parentClass = Order.class, value = "orderItems")
	@Override
	public Page<OrderItem> getOrderItemsPage(
			@NestedFieldId("id") Long orderId, Long skuId,
			Pagination pagination)
		throws Exception {

		if (orderId == 0) {
			return Page.of(Collections.emptyList());
		}

		return Page.of(
			_filterOrderItems(
				TransformUtil.transform(
					_commerceOrderItemService.getCommerceOrderItems(
						orderId, QueryUtil.ALL_POS, QueryUtil.ALL_POS),
					commerceOrderItem -> {
						if ((skuId != null) &&
							!Objects.equals(
								commerceOrderItem.getCPInstanceId(), skuId)) {

							return null;
						}

						CommerceOrder commerceOrder =
							commerceOrderItem.getCommerceOrder();

						return _toOrderItem(
							commerceOrder.getCommerceAccountId(),
							commerceOrderItem);
					})));
	}

	private List<OrderItem> _filterOrderItems(List<OrderItem> orderItems) {
		Map<Long, OrderItem> orderItemsMap = new HashMap<>();

		for (OrderItem orderItem : orderItems) {
			orderItemsMap.put(orderItem.getId(), orderItem);
		}

		for (OrderItem orderItem : orderItems) {
			Long parentOrderItemId = orderItem.getParentOrderItemId();

			if (parentOrderItemId == null) {
				continue;
			}

			OrderItem parentOrderItem = orderItemsMap.get(parentOrderItemId);

			if (parentOrderItem == null) {
				continue;
			}

			if (parentOrderItem.getOrderItems() == null) {
				parentOrderItem.setOrderItems(new OrderItem[0]);
			}

			parentOrderItem.setOrderItems(
				ArrayUtil.append(parentOrderItem.getOrderItems(), orderItem));

			orderItemsMap.remove(orderItem.getId());
		}

		return new ArrayList(orderItemsMap.values());
	}

	private OrderItem _toOrderItem(
			long commerceAccountId, CommerceOrderItem commerceOrderItem)
		throws Exception {

		return _orderItemDTOConverter.toDTO(
			new OrderItemDTOConverterContext(
				commerceAccountId, commerceOrderItem.getCommerceOrderItemId(),
				contextAcceptLanguage.getPreferredLocale()));
	}

	@Reference
	private CommerceContextFactory _commerceContextFactory;

	@Reference
	private CommerceOrderItemService _commerceOrderItemService;

	@Reference
	private CommerceOrderService _commerceOrderService;

	@Reference
	private OrderItemDTOConverter _orderItemDTOConverter;

	@Reference
	private ServiceContextHelper _serviceContextHelper;

}