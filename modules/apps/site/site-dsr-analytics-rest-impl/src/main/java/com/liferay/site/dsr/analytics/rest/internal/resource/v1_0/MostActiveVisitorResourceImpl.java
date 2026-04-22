/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.internal.resource.v1_0;

import com.liferay.analytics.settings.rest.manager.AnalyticsSettingsManager;
import com.liferay.portal.kernel.util.Http;
import com.liferay.site.dsr.analytics.rest.dto.v1_0.MostActiveVisitorsPage;
import com.liferay.site.dsr.analytics.rest.internal.client.DSRAnalyticsCloudClient;
import com.liferay.site.dsr.analytics.rest.resource.v1_0.MostActiveVisitorResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Gianmarco Brunialti
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/most-active-visitor.properties",
	scope = ServiceScope.PROTOTYPE, service = MostActiveVisitorResource.class
)
public class MostActiveVisitorResourceImpl
	extends BaseMostActiveVisitorResourceImpl {

	@Override
	public MostActiveVisitorsPage getMostActiveVisitor(
			String channelId, Integer rangeKey, Integer size, Integer start)
		throws Exception {

		DSRAnalyticsCloudClient dsrAnalyticsCloudClient =
			new DSRAnalyticsCloudClient(_http);

		return dsrAnalyticsCloudClient.getMostActiveVisitorsPage(
			_analyticsSettingsManager.getAnalyticsConfiguration(
				contextCompany.getCompanyId()),
			channelId, rangeKey, size, start);
	}

	@Reference
	private AnalyticsSettingsManager _analyticsSettingsManager;

	@Reference
	private Http _http;

}
