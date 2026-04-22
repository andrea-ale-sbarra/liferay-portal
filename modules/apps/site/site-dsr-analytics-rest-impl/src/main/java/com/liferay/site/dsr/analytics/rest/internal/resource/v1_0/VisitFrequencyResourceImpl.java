/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.internal.resource.v1_0;

import com.liferay.site.dsr.analytics.rest.resource.v1_0.VisitFrequencyResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Gianmarco Brunialti
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/visit-frequency.properties",
	scope = ServiceScope.PROTOTYPE, service = VisitFrequencyResource.class
)
public class VisitFrequencyResourceImpl extends BaseVisitFrequencyResourceImpl {
}
// LIFERAY-REST-BUILDER-HASH:1230983112