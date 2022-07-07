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
import com.liferay.commerce.model.CommerceAddress;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.service.CommerceAddressService;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Address;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Order;
import com.liferay.headless.commerce.delivery.order.internal.dto.v1_0.AddressDTOConverter;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.AddressResource;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.fields.NestedField;
import com.liferay.portal.vulcan.fields.NestedFieldSupport;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false,
	properties = "OSGI-INF/liferay/rest/v1_0/address.properties",
	service = {AddressResource.class, NestedFieldSupport.class}
)
public class AddressResourceImpl
	extends BaseAddressResourceImpl implements NestedFieldSupport {

	@NestedField(parentClass = Order.class, value = "billingAddress")
	@Override
	public Address getOrderBillingAddres(Long orderId) throws Exception {
		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			orderId);

		if (commerceOrder.isOpen()) {
			throw new NoSuchOrderException();
		}

		CommerceAddress commerceAddress =
			_commerceAddressService.getCommerceAddress(
				commerceOrder.getBillingAddressId());

		return _addressDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				_dtoConverterRegistry, commerceAddress.getCommerceAddressId(),
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser));
	}

	@NestedField(parentClass = Order.class, value = "shippingAddress")
	@Override
	public Address getOrderShippingAddres(Long orderId) throws Exception {
		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			orderId);

		if (commerceOrder.isOpen()) {
			throw new NoSuchOrderException();
		}

		CommerceAddress commerceAddress =
			_commerceAddressService.getCommerceAddress(
				commerceOrder.getShippingAddressId());

		return _addressDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				_dtoConverterRegistry, commerceAddress.getCommerceAddressId(),
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser));
	}

	@Reference
	private AddressDTOConverter _addressDTOConverter;

	@Reference
	private CommerceAddressService _commerceAddressService;

	@Reference
	private CommerceOrderService _commerceOrderService;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

}