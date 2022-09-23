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

import com.liferay.commerce.product.util.CPDefinitionContributor;
import com.liferay.commerce.product.util.CPDefinitionContributorRegistry;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerCustomizerFactory.ServiceWrapper;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Ethan Bustad
 */
@Component(
	enabled = false, immediate = true,
	service = CPDefinitionContributorRegistry.class
)
public class CPDefinitionContributorRegistryImpl
	implements CPDefinitionContributorRegistry {

	@Override
	public CPDefinitionContributor getCPDefinitionContributor(String key) {
		if (Validator.isNull(key)) {
			return null;
		}

		ServiceWrapper<CPDefinitionContributor>
			cpVersionContributorServiceWrapper = _serviceTrackerMap.getService(
				key);

		if (cpVersionContributorServiceWrapper == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No commerce product version contributor registered with " +
						"key " + key);
			}

			return null;
		}

		return cpVersionContributorServiceWrapper.getService();
	}

	@Override
	public List<CPDefinitionContributor> getCPDefinitionContributors() {
		Collection<ServiceWrapper<CPDefinitionContributor>>
			cpVersionContributorServiceWrappers = _serviceTrackerMap.values();

		List<CPDefinitionContributor> cpDefinitionContributors = new ArrayList<>(
			cpVersionContributorServiceWrappers.size());

		for (ServiceWrapper<CPDefinitionContributor>
				cpVersionContributorServiceWrapper :
					cpVersionContributorServiceWrappers) {

			cpDefinitionContributors.add(
				cpVersionContributorServiceWrapper.getService());
		}

		return Collections.unmodifiableList(cpDefinitionContributors);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerMap = ServiceTrackerMapFactory.openSingleValueMap(
			bundleContext, CPDefinitionContributor.class,
			"commerce.product.content.contributor.name",
			ServiceTrackerCustomizerFactory.
				<CPDefinitionContributor>serviceWrapper(bundleContext));
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerMap.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CPDefinitionContributorRegistryImpl.class);

	private ServiceTrackerMap<String, ServiceWrapper<CPDefinitionContributor>>
		_serviceTrackerMap;

}