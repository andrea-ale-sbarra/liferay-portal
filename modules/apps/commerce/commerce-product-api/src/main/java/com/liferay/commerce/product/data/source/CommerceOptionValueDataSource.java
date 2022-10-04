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

package com.liferay.commerce.product.data.source;

import aQute.bnd.annotation.ProviderType;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.option.CommerceOption;
import com.liferay.commerce.product.option.CommerceOptionValue;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.Portal;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Andrea Sbarra
 */
@ProviderType
public interface CommerceOptionValueDataSource {

	public String getLabel(Locale locale);

	public String getName();

	public Map<CommerceOption, List<CommerceOptionValue>> getCommerceOptionCommerceOptionValueMap(
		long companyId, long scopeGroupId, long cpDefinitionId, int start, int end)
		throws PortalException;

	public Map<CPDefinitionOptionRel, List<CPDefinitionOptionValueRel>>
	getCPDefinitionOptionValueRelsMap(
		long companyId, long scopeGroupId, long cpDefinitionId, int start, int end)
		throws PortalException;

}