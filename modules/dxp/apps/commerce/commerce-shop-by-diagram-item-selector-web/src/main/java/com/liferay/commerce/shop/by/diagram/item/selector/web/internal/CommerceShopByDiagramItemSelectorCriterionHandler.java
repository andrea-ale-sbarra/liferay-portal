/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shop.by.diagram.item.selector.web.internal;

import com.liferay.commerce.shop.by.diagram.item.selector.criterion.CommerceShopByDiagramItemSelectorCriterion;
import com.liferay.item.selector.BaseItemSelectorCriterionHandler;
import com.liferay.item.selector.ItemSelectorCriterionHandler;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Andrea Sbarra
 */
@Component(service = ItemSelectorCriterionHandler.class)
public class CommerceShopByDiagramItemSelectorCriterionHandler
	extends BaseItemSelectorCriterionHandler
		<CommerceShopByDiagramItemSelectorCriterion> {

	@Override
	public Class<CommerceShopByDiagramItemSelectorCriterion>
		getItemSelectorCriterionClass() {

		return CommerceShopByDiagramItemSelectorCriterion.class;
	}

	@Activate
	@Override
	protected void activate(BundleContext bundleContext) {
		super.activate(bundleContext);
	}

	@Deactivate
	@Override
	protected void deactivate() {
		super.deactivate();
	}

}