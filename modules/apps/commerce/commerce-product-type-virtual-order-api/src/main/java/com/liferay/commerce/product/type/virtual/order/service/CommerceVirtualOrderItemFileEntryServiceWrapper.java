/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.product.type.virtual.order.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link CommerceVirtualOrderItemFileEntryService}.
 *
 * @author Alessio Antonio Rendina
 * @see CommerceVirtualOrderItemFileEntryService
 * @generated
 */
public class CommerceVirtualOrderItemFileEntryServiceWrapper
	implements CommerceVirtualOrderItemFileEntryService,
			   ServiceWrapper<CommerceVirtualOrderItemFileEntryService> {

	public CommerceVirtualOrderItemFileEntryServiceWrapper() {
		this(null);
	}

	public CommerceVirtualOrderItemFileEntryServiceWrapper(
		CommerceVirtualOrderItemFileEntryService
			commerceVirtualOrderItemFileEntryService) {

		_commerceVirtualOrderItemFileEntryService =
			commerceVirtualOrderItemFileEntryService;
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _commerceVirtualOrderItemFileEntryService.
			getOSGiServiceIdentifier();
	}

	@Override
	public CommerceVirtualOrderItemFileEntryService getWrappedService() {
		return _commerceVirtualOrderItemFileEntryService;
	}

	@Override
	public void setWrappedService(
		CommerceVirtualOrderItemFileEntryService
			commerceVirtualOrderItemFileEntryService) {

		_commerceVirtualOrderItemFileEntryService =
			commerceVirtualOrderItemFileEntryService;
	}

	private CommerceVirtualOrderItemFileEntryService
		_commerceVirtualOrderItemFileEntryService;

}