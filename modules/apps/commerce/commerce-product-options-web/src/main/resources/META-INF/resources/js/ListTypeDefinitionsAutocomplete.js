/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {CommerceServiceProvider} from 'commerce-frontend-js';
import {fetch} from 'frontend-js-web';
import ClayAutocomplete from '@clayui/autocomplete';
import ClayForm from '@clayui/form';
import React, {useEffect, useMemo, useState} from 'react';

const AdminCatalogResource = CommerceServiceProvider.AdminCatalogAPI('v1');
const ListTypeDefinitionsAutocomplete = ({namespace}) => {
	const [listTypeDefinitions, setListTypeDefinitions] = useState([]);
	const [value, setValue] = useState('');

	useEffect(() => {
		fetch('/o/headless-admin-list-type/v1.0/list-type-definitions')
			.then((response) => response.json())
			.then(({items}) => setListTypeDefinitions(items));
	}, []);

	const items = useMemo(
		() => listTypeDefinitions.filter(
			({externalReferenceCode, name, name_i18n}) =>
				externalReferenceCode.includes(value) ||
				name.includes(value) ||
				name_i18n[Liferay.ThemeDisplay.getBCP47LanguageId()].includes(value)
		),
		[listTypeDefinitions, value]
	);

	useEffect(() => {
		console.log({items});
	}, [items]);

	return (
		<ClayForm.Group aria-required={true}>
			<label htmlFor={`${namespace}autocomplete`} id={`${namespace}autocomplete-label`}>
				{Liferay.Language.get('name')}
			</label>

			<ClayAutocomplete
				aria-labelledby={`${namespace}autocomplete-label`}
				aria-required={true}
				items={items}
				id={`${namespace}autocomplete`}
				onChange={setValue}
				placeholder={Liferay.Language.get('select-a-picklist')}
				menuTrigger="focus"
				required={true}
				value={value}
			>
				{({id, name_i18n, externalReferenceCode}, index) => (
					<ClayAutocomplete.Item
						key={index}
						onSelect={() => { console.log('selected'); }}
						textValue={`${
							name_i18n[Liferay.ThemeDisplay.getBCP47LanguageId()]
						} - ${externalReferenceCode}`}
					/>
				)}
			</ClayAutocomplete>
		</ClayForm.Group>
	);
};

export default ListTypeDefinitionsAutocomplete;