/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.list.type.contributor;

import java.util.List;

/**
 * @author Andrea Sbarra
 */
public interface ListTypeDefinitionContributorRegistry {

	public ListTypeDefinitionContributor getListTypeDefinitionContributor(String key);

	public List<ListTypeDefinitionContributor> getListTypeDefinitionContributors();
}
