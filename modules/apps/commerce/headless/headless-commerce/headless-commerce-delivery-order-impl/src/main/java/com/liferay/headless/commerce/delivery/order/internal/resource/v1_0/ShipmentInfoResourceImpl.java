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
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.commerce.service.CommerceShipmentService;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Order;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.ShipmentInfo;
import com.liferay.headless.commerce.delivery.order.internal.dto.v1_0.ShipmentInfoDTOConverter;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.ShipmentInfoResource;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.fields.NestedField;
import com.liferay.portal.vulcan.fields.NestedFieldId;
import com.liferay.portal.vulcan.fields.NestedFieldSupport;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.util.TransformUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false,
	properties = "OSGI-INF/liferay/rest/v1_0/shipment-info.properties",
	scope = ServiceScope.PROTOTYPE,
	service = {NestedFieldSupport.class, ShipmentInfoResource.class}
)
public class ShipmentInfoResourceImpl
	extends BaseShipmentInfoResourceImpl implements NestedFieldSupport {

	@NestedField(parentClass = Order.class, value = "shipmentInfos")
	@Override
	public Page<ShipmentInfo> getOrderShipmentInfosPage(
			@NestedFieldId("id") Long orderId, Pagination pagination)
		throws Exception {

		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			orderId);

		if (commerceOrder.isOpen()) {
			throw new NoSuchOrderException();
		}

		return Page.of(
			TransformUtil.transform(
				_commerceShipmentService.getCommerceShipmentsByOrderId(
					orderId, pagination.getStartPosition(),
					pagination.getEndPosition()),
				commerceShipment -> _toShipmentInfo(
					commerceShipment.getCommerceShipmentId())),
			pagination,
			_commerceShipmentService.getCommerceShipmentsCountByOrderId(
				orderId));
	}

	private ShipmentInfo _toShipmentInfo(long commerceShipmentId)
		throws Exception {

		return _shipmentInfoDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				_dtoConverterRegistry, commerceShipmentId,
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser));
	}

	@Reference
	private CommerceOrderService _commerceOrderService;

	@Reference
	private CommerceShipmentService _commerceShipmentService;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private ShipmentInfoDTOConverter _shipmentInfoDTOConverter;

}