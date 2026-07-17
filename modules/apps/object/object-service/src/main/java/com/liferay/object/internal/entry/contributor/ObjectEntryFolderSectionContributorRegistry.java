/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.entry.contributor;

import com.liferay.object.entry.contributor.ObjectEntryFolderSectionContributor;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Andrea Sbarra
 */
@Component(service = ObjectEntryFolderSectionContributorRegistry.class)
public class ObjectEntryFolderSectionContributorRegistry {

	public String getSection(String externalReferenceCode) {
		for (ObjectEntryFolderSectionContributor
				objectEntryFolderSectionContributor :
					_objectEntryFolderSectionContributors) {

			String section =
				objectEntryFolderSectionContributor.getSection(
					externalReferenceCode);

			if (section != null) {
				return section;
			}
		}

		return null;
	}

	@Reference(
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private volatile List<ObjectEntryFolderSectionContributor>
		_objectEntryFolderSectionContributors;

}
