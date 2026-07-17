/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.entry.contributor;

import com.liferay.object.entry.contributor.ObjectEntryFolderSectionContributor;
import com.liferay.site.pim.site.initializer.internal.constants.PIMObjectEntryFolderConstants;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * @author Andrea Sbarra
 */
@Component(service = ObjectEntryFolderSectionContributor.class)
public class PIMProductObjectEntryFolderSectionContributor
	implements ObjectEntryFolderSectionContributor {

	@Override
	public String getSection(String externalReferenceCode) {
		if (Objects.equals(
				externalReferenceCode,
				PIMObjectEntryFolderConstants.
					EXTERNAL_REFERENCE_CODE_PRODUCTS)) {

			return "products";
		}

		return null;
	}

}
