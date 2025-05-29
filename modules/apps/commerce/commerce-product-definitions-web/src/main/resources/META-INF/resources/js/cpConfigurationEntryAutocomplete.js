/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {Autocomplete, CommerceServiceProvider} from 'commerce-frontend-js';

export default function ({cpConfigurationListId, dataSetId, namespace}) {
	Autocomplete('autocomplete', 'autocomplete-root', {
		apiUrl: `/o/headless-commerce-admin-catalog/v1.0/products`,
		inputId: `${namespace}productId`,
		inputName: `${namespace}productId`,
		itemsKey: 'id',
		itemsLabel: ['name', 'LANG'],
	});

	let formSubmitted = false;

	Liferay.provide(window, `${namespace}submitForm`, () => {
		if (formSubmitted) {
			return;
		}

		formSubmitted = true;

		const formattedData = {};

		formattedData.entityId = document.querySelector(
			`#${namespace}productId`
		)?.value;

		const AdminCatalogResource =
			CommerceServiceProvider.AdminCatalogAPI('v1');
		AdminCatalogResource.addProductConfigurationEntry(
			cpConfigurationListId,
			formattedData
		)
			.then(() => {
				window.top.Liferay.fire('fds-update-display', {
					id: dataSetId,
				});
			})
			.catch(({message}) => {
				if (message !== 'cancel') {
					window.top.Liferay.Util.openToast({
						message:
							message ||
							Liferay.Language.get(
								'an-unexpected-error-occurred'
							),
						type: 'danger',
					});
				}
				formSubmitted = false;
			});
	});
}
