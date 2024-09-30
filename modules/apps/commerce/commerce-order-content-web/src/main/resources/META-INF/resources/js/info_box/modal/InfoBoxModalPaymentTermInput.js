/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayAlert from '@clayui/alert';
import {ClayRadio, ClayRadioGroup} from '@clayui/form';
import {CommerceServiceProvider} from 'commerce-frontend-js';
import React, {useEffect, useState} from 'react';

const InfoBoxModalPaymentTermInput = ({
	inputValue,
	orderId,
	setInputValue,
	setIsValid,
	setParseResponse,
	spritemap,
}) => {
	const [hasPaymentTerms, setHasPaymentTerms] = useState(false);
	const [paymentTerms, setPaymentTerms] = useState([]);

	useEffect(() => {
		setParseResponse(() => (field, response) => {
			return response['paymentTermLabel'];
		});

		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, []);

	useEffect(() => {
		CommerceServiceProvider.DeliveryCartAPI('v1')
			.getCartPaymentTermsPage(orderId)
			.then(({items}) => {
				const paymentTermsAvailable = !!items.length;

				setHasPaymentTerms(paymentTermsAvailable);
				setIsValid(paymentTermsAvailable);
				setPaymentTerms(items);
			})
			.catch((error) => {
				setHasPaymentTerms(false);
				setIsValid(false);
				setPaymentTerms([]);

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
			{hasPaymentTerms ? (
				<ClayRadioGroup
					defaultValue={inputValue}
					id="infoBoxModalPaymentTermInput"
					onChange={(value) => {
						setInputValue(value);
					}}
				>
					{paymentTerms.map((paymentTerm) => (
						<ClayRadio
							key={paymentTerm.id}
							label={paymentTerm.name}
							value={paymentTerm.id}
						/>
					))}
				</ClayRadioGroup>
			) : (
				<ClayAlert displayType="info" spritemap={spritemap}>
					{Liferay.Language.get(
						'there-are-no-available-payment-terms'
					)}
				</ClayAlert>
			)}
		</>
	);
};

export default InfoBoxModalPaymentTermInput;
