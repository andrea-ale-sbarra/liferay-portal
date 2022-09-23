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


package com.liferay.commerce.product.internal.util;

import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.service.CPAttachmentFileEntryLocalService;
import com.liferay.commerce.product.util.CPDefinitionContributor;
import com.liferay.portal.kernel.exception.PortalException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false, immediate = true, service = CPDefinitionContributor.class
)
public class CPAttachmentFileEntryCPDefinitionContributor
	implements CPDefinitionContributor {

	@Override
	public void onDelete(long cpDefinitionId) throws PortalException {
		_cpAttachmentFileEntryLocalService.deleteCPAttachmentFileEntries(
			CPDefinition.class.getName(), cpDefinitionId);
	}

	@Override
	public void contribute(long oldCPDefinitionId, long newCPDefinitionId) {
		_cpAttachmentFileEntryLocalService.cloneCPAttachmentFileEntry(oldCPDefinitionId, newCPDefinitionId);

	}

	@Reference
	private CPAttachmentFileEntryLocalService _cpAttachmentFileEntryLocalService;

}
