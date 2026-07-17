/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.entry.contributor;

/**
 * Maps an object entry folder, identified by its external reference code, to the
 * section its object entries belong to (for example the value indexed as
 * <code>cms_section</code>). Register one per consumer that owns a
 * section-defining root folder; the object framework consults all registered
 * contributors when indexing object entries, so a new section can be added
 * without registering a per-object-definition deployer.
 *
 * @author Andrea Sbarra
 */
public interface ObjectEntryFolderSectionContributor {

	/**
	 * Returns the section for the object entry folder with the given external
	 * reference code, or <code>null</code> if this contributor does not own it.
	 * A non-null result also marks the folder as a section root.
	 */
	public String getSection(String externalReferenceCode);

}
