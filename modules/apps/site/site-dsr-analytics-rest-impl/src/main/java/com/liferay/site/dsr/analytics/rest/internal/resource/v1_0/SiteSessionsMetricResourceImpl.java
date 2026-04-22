/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.internal.resource.v1_0;

import com.liferay.analytics.settings.rest.manager.AnalyticsSettingsManager;
import com.liferay.portal.kernel.util.Http;
import com.liferay.site.dsr.analytics.rest.dto.v1_0.SiteHistogramMetric;
import com.liferay.site.dsr.analytics.rest.internal.client.DSRAnalyticsCloudClient;
import com.liferay.site.dsr.analytics.rest.resource.v1_0.SiteSessionsMetricResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Gianmarco Brunialti
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/site-sessions-metric.properties",
	scope = ServiceScope.PROTOTYPE,
	service = SiteSessionsMetricResource.class
)
public class SiteSessionsMetricResourceImpl
	extends BaseSiteSessionsMetricResourceImpl {

	@Override
	public SiteHistogramMetric getSiteSessionsMetric(
			String channelId, String[] emailAddresses, String interval,
			String rangeEnd, Integer rangeKey, String rangeStart)
		throws Exception {

		DSRAnalyticsCloudClient dsrAnalyticsCloudClient =
			new DSRAnalyticsCloudClient(_http);

		return dsrAnalyticsCloudClient.getSiteSessionsMetric(
			_analyticsSettingsManager.getAnalyticsConfiguration(
				contextCompany.getCompanyId()),
			channelId, emailAddresses, interval, rangeEnd, rangeKey,
			rangeStart);
	}

	@Reference
	private AnalyticsSettingsManager _analyticsSettingsManager;

	@Reference
	private Http _http;

}
