/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Autocomplete, CommerceServiceProvider, commerceEvents} from 'commerce-frontend-js';

const AdminCatalogResource = CommerceServiceProvider.AdminCatalogAPI('v1');

async function assignPicklist(specificationId, picklistId) {}

export default function assignCPSpecificationOptionPicklist({
	namespace,
	cpSpecificationOptionId,
}) {
	let picklistId = 0;

	Liferay.on('picklist-id-selected', (id) => {
		picklistId = id;
	})

	Liferay.provide(
		window,
		`${namespace}storeToParentForm`,
		(form) => {
			if (picklistId) {}
		}
	)
}