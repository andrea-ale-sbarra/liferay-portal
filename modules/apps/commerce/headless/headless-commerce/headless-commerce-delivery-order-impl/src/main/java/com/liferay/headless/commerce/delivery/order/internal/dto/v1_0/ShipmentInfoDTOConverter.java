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

import com.liferay.commerce.model.CommerceShipment;
import com.liferay.commerce.service.CommerceShipmentService;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.ShipmentInfo;
import com.liferay.portal.vulcan.dto.converter.DTOConverter;
import com.liferay.portal.vulcan.dto.converter.DTOConverterContext;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false,
	property = "dto.class.name=com.liferay.commerce.model.CommerceShipment",
	service = {DTOConverter.class, ShipmentInfoDTOConverter.class}
)
public class ShipmentInfoDTOConverter
	implements DTOConverter<CommerceShipment, ShipmentInfo> {

	@Override
	public String getContentType() {
		return ShipmentInfo.class.getSimpleName();
	}

	@Override
	public ShipmentInfo toDTO(DTOConverterContext dtoConverterContext)
		throws Exception {

		CommerceShipment commerceShipment =
			_commerceShipmentService.getCommerceShipment(
				(Long)dtoConverterContext.getId());

		return new ShipmentInfo() {
			{
				accountId = commerceShipment.getCommerceAccountId();

				carrier = commerceShipment.getCarrier();
				createDate = commerceShipment.getCreateDate();

				expectedDate = commerceShipment.getExpectedDate();

				id = commerceShipment.getCommerceShipmentId();
				modifiedDate = commerceShipment.getModifiedDate();
				shippingAddressId = commerceShipment.getCommerceAddressId();
				shippingDate = commerceShipment.getShippingDate();
				shippingMethodId =
					commerceShipment.getCommerceShippingMethodId();
				shippingOptionName = commerceShipment.getShippingOptionName();
				trackingNumber = commerceShipment.getTrackingNumber();
				userName = commerceShipment.getUserName();
			}
		};
	}

	@Reference
	private CommerceShipmentService _commerceShipmentService;

}