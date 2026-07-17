/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.entry.contributor;

import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.entry.contributor.ObjectEntryFolderSectionContributor;

import java.util.Objects;

import org.osgi.service.component.annotations.Component;

/**
 * @author Andrea Sbarra
 */
@Component(service = ObjectEntryFolderSectionContributor.class)
public class DefaultObjectEntryFolderSectionContributor
	implements ObjectEntryFolderSectionContributor {

	@Override
	public String getSection(String externalReferenceCode) {
		if (Objects.equals(
				externalReferenceCode,
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_CONTENTS)) {

			return "contents";
		}

		if (Objects.equals(
				externalReferenceCode,
				ObjectEntryFolderConstants.EXTERNAL_REFERENCE_CODE_FILES)) {

			return "files";
		}

		return null;
	}

}
