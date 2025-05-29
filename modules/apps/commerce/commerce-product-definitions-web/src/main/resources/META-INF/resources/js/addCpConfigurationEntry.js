/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {openModal} from 'frontend-js-components-web';

function openAddCPConfigurationEntryModal({
	addCPConfigurationEntryRenderURL,
	namespace,
}) {
	openModal({
		buttons: [
			{
				displayType: 'secondary',
				label: Liferay.Language.get('cancel'),
				onClick: ({processClose}) => {
					processClose();
				},
				type: 'button',
			},
			{
				displayType: 'primary',
				label: Liferay.Language.get('submit'),
				onClick: ({processClose}) => {
					const iframeContentWindow = document.querySelector(
						'#add-new-product-configuration-modal iframe'
					).contentWindow;

					const iframeDocument = iframeContentWindow.document;

					const saveButton = iframeDocument.querySelector(
						`#${namespace}saveButton`
					);

					saveButton.click();
					processClose();
				},
				type: 'button',
			},
		],
		height: '32rem',
		id: 'add-new-product-configuration-modal',
		size: 'md',
		title: Liferay.Language.get('add-new-product-configuration'),
		url: addCPConfigurationEntryRenderURL,
	});
}

export default function main({
	addCPConfigurationEntryRenderURL,
	editCPConfigurationEntryRenderURL,
	namespace,
	windowState,
}) {
	const handler = () =>
		openAddCPConfigurationEntryModal({
			addCPConfigurationEntryRenderURL,
			editCPConfigurationEntryRenderURL,
			namespace,
			windowState,
		});

	Liferay.on('addCPConfigurationEntry', handler);

	return {
		dispose: () => {
			Liferay.detach('addCPConfigurationEntry', handler);
		},
	};
}
