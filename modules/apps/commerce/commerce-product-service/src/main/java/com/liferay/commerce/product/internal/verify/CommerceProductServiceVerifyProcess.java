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

package com.liferay.commerce.product.internal.verify;

import com.liferay.commerce.constants.CommerceDestinationNames;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBusUtil;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.verify.VerifyProcess;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	enabled = false, immediate = true,
	property = "verify.process.name=com.liferay.commerce.product.service",
	service = {CommerceProductServiceVerifyProcess.class, VerifyProcess.class}
)
public class CommerceProductServiceVerifyProcess extends VerifyProcess {

	@Override
	protected void doVerify() throws Exception {
		verifyDefaultCatalogs();
	}

	protected void verifyDefaultCatalogs() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			_companyLocalService.forEachCompanyId(
				companyId -> {
					List<CommerceCatalog> commerceCatalogs =
						_commerceCatalogLocalService.getCommerceCatalogs(
							companyId, true);

					if (commerceCatalogs.isEmpty()) {
						CommerceCatalog commerceCatalog =
							_commerceCatalogLocalService.
								addDefaultCommerceCatalog(companyId);

						Message message = new Message();

						message.put(
							"commerceCatalogId",
							commerceCatalog.getCommerceCatalogId());

						MessageBusUtil.sendMessage(
							CommerceDestinationNames.BASE_PRICE_LIST, message);
					}
				});
		}
	}

	@Reference
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Reference
	private CompanyLocalService _companyLocalService;

}