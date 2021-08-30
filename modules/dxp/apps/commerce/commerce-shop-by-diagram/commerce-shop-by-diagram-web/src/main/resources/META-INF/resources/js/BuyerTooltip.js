/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import ClayAutocomplete from '@clayui/autocomplete';
import ClayButton from '@clayui/button';
import ClayCard from '@clayui/card';
import ClayDropDown from '@clayui/drop-down';
import ClayForm, {ClayInput, ClayRadio, ClayRadioGroup} from '@clayui/form';
import PropTypes from 'prop-types';
import React, {useEffect, useState} from 'react';

const BuyerTooltip = ({
	deletePin,
	namespace,
	searchSkus,
	setRemovePinHandler,
	setShowTooltip,
	showTooltip,
	skus,
	updatePin,
}) => {
	const [pinPositionLabel, setPinPositionLabel] = useState(
		showTooltip.details.label
	);
	const [linkedValue, setLinkedValue] = useState(
		showTooltip.details.linkedToSku
	);
	const [sku, setSku] = useState(showTooltip.details.sku);
	const [quantity, setQuantity] = useState(showTooltip.details.quantity);

	useEffect(() => {
		searchSkus(sku, linkedValue);
	}, [sku, searchSkus, linkedValue]);

	return (
		<ClayCard
			className="admin-tooltip"
			style={{
				left: showTooltip.details.cx,
				top: showTooltip.details.cy,
			}}
		>
			<ClayCard.Body className="row">
				<ClayForm.Group className="col-12 text-left" small>
					<label htmlFor={`${namespace}pin-position`}>
						{Liferay.Language.get('buyer-tooltip')}
					</label>
					{JSON.stringify(showTooltip)}
				</ClayForm.Group>
			</ClayCard.Body>
		</ClayCard>
	);
};

BuyerTooltip.propTypes = {
	setShowTooltip: PropTypes.func,
	showTooltip: PropTypes.shape({
		details: PropTypes.shape({
			cx: PropTypes.double,
			cy: PropTypes.double,
			id: PropTypes.number,
			label: PropTypes.string,
			linkedToSku: PropTypes.oneOf(['sku', 'diagram']),
			quantity: PropTypes.number,
			sku: PropTypes.string,
		}),
		tooltip: PropTypes.bool,
	}),
};

export default BuyerTooltip;
