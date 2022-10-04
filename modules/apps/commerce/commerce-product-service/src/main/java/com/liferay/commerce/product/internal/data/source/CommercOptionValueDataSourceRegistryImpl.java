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

package com.liferay.commerce.product.internal.data.source;

import com.liferay.commerce.product.data.source.CPDataSourceRegistry;
import com.liferay.commerce.product.data.source.CommerceOptionValueDataSource;
import com.liferay.commerce.product.data.source.CommerceOptionValueDataSourceRegistry;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.Validator;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false, immediate = true, service = CommerceOptionValueDataSourceRegistry.class
)
public class CommercOptionValueDataSourceRegistryImpl implements
	CommerceOptionValueDataSourceRegistry {

	@Override
	public CommerceOptionValueDataSource getCommerceOptionValueDataSource(
		String key) {
		if (Validator.isNull(key)) {
			return null;
		}

		for (CommerceOptionValueDataSource commerceOptionValueDataSource : _serviceTrackerList) {
			if (key.equals(commerceOptionValueDataSource.getName())) {
				return commerceOptionValueDataSource;
			}
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				"No commerce product data source registered with key " + key);
		}

		return null;
	}

	@Override
	public List<CommerceOptionValueDataSource> getCommerceOptionValueDataSources() {
		List<CommerceOptionValueDataSource> commerceOptionValueDataSources = new ArrayList<>();

		for (CommerceOptionValueDataSource commerceOptionValueDataSource : _serviceTrackerList) {
			if (Validator.isNotNull(commerceOptionValueDataSource.getName())) {
				commerceOptionValueDataSources.add(commerceOptionValueDataSource);
			}
		}

		return Collections.unmodifiableList(commerceOptionValueDataSources);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, CommerceOptionValueDataSource.class);
	}

	@Deactivate
	protected void deactivate() {
		_serviceTrackerList.close();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommercOptionValueDataSourceRegistryImpl.class);

	private ServiceTrackerList<CommerceOptionValueDataSource> _serviceTrackerList;


}