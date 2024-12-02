/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Autocomplete, CommerceServiceProvider} from 'commerce-frontend-js';
import {createPortletURL, openModal, openToast} from 'frontend-js-web';

export default function ({namespace}) {

	const commerceCatalogIdSelect = document.getElementById(`${namespace}commerceCatalogId`);
	console.log("STEFANO");
	let commerceCatalogId = 0;

	commerceCatalogIdSelect.addEventListener('change', (event) => {
		commerceCatalogId = event.target.value;

		Autocomplete('autocomplete', 'autocomplete-root', {
			apiUrl: `/o/headless-commerce-admin-catalog/v1.0/product-configuration-lists?commerceCatalogId=${commerceCatalogId}`,
			inputId: `${namespace}parentCPConfigurationId`,
			inputName: `${namespace}parentCPConfigurationId`,
			itemsKey: 'id',
			itemsLabel: 'name',
			required: true,
		});
	});

	Autocomplete('autocomplete', 'autocomplete-root', {
		apiUrl: `/o/headless-commerce-admin-catalog/v1.0/product-configuration-lists`,
		disabled: true,
		inputId: `${namespace}parentCPConfigurationId`,
		inputName: `${namespace}parentCPConfigurationId`,
		itemsKey: 'id',
		itemsLabel: 'name',
		required: true,
	});
}
