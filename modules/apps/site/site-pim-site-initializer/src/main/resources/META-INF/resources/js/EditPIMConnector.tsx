/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import ClayForm, {
	ClayCheckbox,
	ClayInput,
	ClaySelectWithOption,
} from '@clayui/form';
import {fetch, navigate, sub} from 'frontend-js-web';
import React, {useEffect, useState} from 'react';

interface IConnector {
	key: string;
	name: string;
}

interface IProps {
	apiURL: string;
	backURL: string;
	connectors: IConnector[];
	objectEntryId: number;
}

export default function EditPIMConnector({
	apiURL,
	backURL,
	connectors,
	objectEntryId,
}: IProps) {
	const isNew = Number(objectEntryId) === 0;

	const [active, setActive] = useState(false);
	const [apiSchema, setApiSchema] = useState('');
	const [connectorKey, setConnectorKey] = useState('');
	const [name, setName] = useState('');

	useEffect(() => {
		if (isNew) {
			return;
		}

		const fetchConnector = async () => {
			try {
				const response = await fetch(`${apiURL}/${objectEntryId}`);

				if (!response.ok) {
					throw new Error();
				}

				const data = await response.json();

				setActive(Boolean(data.active));
				setApiSchema(data.apiSchema || '');
				setConnectorKey(data.connectorKey || '');
				setName(data.name || '');
			}
			catch (error) {
				navigate(backURL);
			}
		};

		fetchConnector();
	}, [apiURL, backURL, isNew, objectEntryId]);

	const handleSave = async () => {
		try {
			const response = await fetch(
				isNew ? apiURL : `${apiURL}/${objectEntryId}`,
				{
					body: JSON.stringify({active, apiSchema, connectorKey, name}),
					headers: {
						'Content-Type': 'application/json',
					},
					method: isNew ? 'POST' : 'PUT',
				}
			);

			if (!response.ok) {
				throw new Error();
			}

			navigate(backURL);

			Liferay.Util.openToast({
				message: sub(
					isNew
						? Liferay.Language.get('x-was-published-successfully')
						: Liferay.Language.get('x-was-updated-successfully'),
					name
				),
				type: 'success',
			});
		}
		catch (error) {
			Liferay.Util.openToast({
				message: Liferay.Language.get('an-unexpected-error-occurred'),
				type: 'danger',
			});
		}
	};

	return (
		<ClayForm
			onSubmit={(event) => {
				event.preventDefault();

				handleSave();
			}}
		>
			<ClayForm.Group>
				<label htmlFor="pimConnectorName">
					{Liferay.Language.get('name')}
				</label>

				<ClayInput
					id="pimConnectorName"
					onChange={(event) => setName(event.target.value)}
					required
					type="text"
					value={name}
				/>
			</ClayForm.Group>

			<ClayForm.Group>
				<label htmlFor="pimConnectorKey">
					{Liferay.Language.get('connector')}
				</label>

				<ClaySelectWithOption
					id="pimConnectorKey"
					onChange={(event) => setConnectorKey(event.target.value)}
					options={[
						{
							disabled: true,
							label: Liferay.Language.get('select-a-connector'),
							value: '',
						},
						...connectors.map((connector) => ({
							label: connector.name,
							value: connector.key,
						})),
					]}
					required
					value={connectorKey}
				/>
			</ClayForm.Group>

			<ClayForm.Group>
				<label htmlFor="pimConnectorAPISchema">
					{Liferay.Language.get('api-schema')}
				</label>

				<textarea
					className="form-control"
					id="pimConnectorAPISchema"
					onChange={(event) => setApiSchema(event.target.value)}
					rows={12}
					value={apiSchema}
				/>
			</ClayForm.Group>

			<ClayForm.Group>
				<ClayCheckbox
					checked={active}
					label={Liferay.Language.get('active')}
					onChange={() => setActive((previousActive) => !previousActive)}
				/>
			</ClayForm.Group>

			<ClayButton.Group spaced>
				<ClayButton
					disabled={!name.trim() || !connectorKey}
					displayType="primary"
					type="submit"
				>
					{Liferay.Language.get('save')}
				</ClayButton>

				<ClayButton
					displayType="secondary"
					onClick={() => navigate(backURL)}
				>
					{Liferay.Language.get('cancel')}
				</ClayButton>
			</ClayButton.Group>
		</ClayForm>
	);
}
