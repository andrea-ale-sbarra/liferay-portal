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

package com.liferay.commerce.product.util;

import com.liferay.osgi.util.ServiceTrackerFactory;

import java.util.List;

import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Ethan Bustad
 */
public class CPDefinitionContributorRegistryUtil {

	public static CPDefinitionContributor getCPDefinitionContributor(String key) {
		CPDefinitionContributorRegistry cpDefinitionContributorRegistry =
			_serviceTracker.getService();

		return cpDefinitionContributorRegistry.getCPDefinitionContributor(key);
	}

	public static List<CPDefinitionContributor> getCPDefinitionContributors() {
		CPDefinitionContributorRegistry cpDefinitionContributorRegistry =
			_serviceTracker.getService();

		return cpDefinitionContributorRegistry.getCPDefinitionContributors();
	}

	private static final ServiceTracker<?, CPDefinitionContributorRegistry>
		_serviceTracker = ServiceTrackerFactory.open(
			FrameworkUtil.getBundle(CPDefinitionContributorRegistry.class),
			CPDefinitionContributorRegistry.class);

}