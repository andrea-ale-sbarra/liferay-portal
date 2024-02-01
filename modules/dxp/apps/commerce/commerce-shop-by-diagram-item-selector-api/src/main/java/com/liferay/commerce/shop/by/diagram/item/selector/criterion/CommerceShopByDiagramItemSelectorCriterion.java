/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shop.by.diagram.item.selector.criterion;

import com.liferay.item.selector.BaseItemSelectorCriterion;
import com.liferay.item.selector.constants.ItemSelectorCriterionConstants;
import com.liferay.item.selector.criteria.upload.criterion.UploadItemSelectorCriterion;

/**
 * @author Andrea Sbarra
 */
public class CommerceShopByDiagramItemSelectorCriterion extends BaseItemSelectorCriterion {

	@Override
	public String getMimeTypeRestriction() {
		return ItemSelectorCriterionConstants.MIME_TYPE_RESTRICTION_IMAGE;
	}
}