/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import {ClayRadio, ClayRadioGroup} from '@clayui/form';
import {CommerceServiceProvider} from 'commerce-frontend-js';
import React, {useEffect, useState} from 'react';

const InfoBoxModalDeliveryTermInput = ({
	inputValue,
	orderId,
	setInputValue,
	setIsValid,
	setParseResponse,
	spritemap,
}) => {
	const [hasDeliveryTerms, setHasDeliveryTerms] = useState(false);
	const [deliveryTerms, setDeliveryTerms] = useState([]);

	useEffect(() => {
		setParseResponse(() => (field, response) => {
			return response['deliveryTermLabel'];
		});

		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	useEffect(() => {
		CommerceServiceProvider.DeliveryCartAPI('v1')
			.getCartDeliveryTermsPage(orderId)
			.then(({items}) => {
				const deliveryTermsAvailable = !!items.length;

				setHasDeliveryTerms(deliveryTermsAvailable);
				setIsValid(deliveryTermsAvailable);
				setDeliveryTerms(items);
			})
			.catch((error) => {
				setHasDeliveryTerms(false);
				setIsValid(false);
				setDeliveryTerms([]);

				Liferay.Util.openToast({
					message:
						error.detail ||
						error.errorDescription ||
						Liferay.Language.get(
							'an-unexpected-system-error-occurred'
						),
					type: 'danger',
				});
			});
	}, [orderId, setIsValid]);

	return (
		<>
			{hasDeliveryTerms ? (
				<ClayRadioGroup
					defaultValue={inputValue}
					id="infoBoxModalDeliveryTermInput"
					onChange={(value) => {
						setInputValue(value);
					}}
				>
					{deliveryTerms.map((deliveryTerm) => (
						<ClayRadio
							key={deliveryTerm.id}
							label={deliveryTerm.name}
							value={deliveryTerm.id}
						/>
					))}
				</ClayRadioGroup>
			) : (
				<ClayAlert displayType="info" spritemap={spritemap}>
					{Liferay.Language.get(
						'there-are-no-available-delivery-terms'
					)}
				</ClayAlert>
			)}
		</>
	);
};

export default InfoBoxModalDeliveryTermInput;
