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
import React, {useLayoutEffect, useEffect, useState} from 'react';

const AdminTooltip = ({
	namespace,
	setRemovePinHandler,
	setShowTooltip,
	showTooltip,
	endpointURL,
	deletePin,
	updatePin,
}) => {
	const [pinLabel, setPinLabel] = useState(showTooltip.details.label);
	const [linkedValue, setLinkedValue] = useState(
		showTooltip.details.linked_to_sku
	);
	const [sku, setSku] = useState(showTooltip.details.sku || "");
	const [quantity, setQuantity] = useState(showTooltip.details.quantity);
	const [skus, setSkus] = useState([])

	const getSkus = (query) => {
		let queryParam = ""
		if (query) {
			queryParam = `?search=${query}`
		}
		return fetch(`${endpointURL}skus/${queryParam}`, {
			headers: new Headers({
				Accept: 'application/json',
				'Content-Type': 'application/json',
			})
		}).then((response) =>  response.json())
		.then((jsonResponse) => {
			console.log({jsonResponse})
			setSkus(jsonResponse)
			return jsonResponse.items
		});
	}
	// const searchSkus = () => {
	// 	return fetch(`${pinsEndpoint}skus`, {
	// 		headers: new Headers({
	// 			Accept: 'application/json',
	// 			'Content-Type': 'application/json',
	// 		})
	// 	})
	// 		.then((response) => response.json())
	// 		.then((jsonResponse) => {
	// 			console.log(jsonResponse)
	// 			setSkus(jsonResponse.items)
	// 			return jsonResponse
	// 		})
	// }

	useEffect(() => {
		const skuLoaded = getSkus(sku)
		console.log({skuLoaded})
	}, [sku])

	return (
		<ClayCard
			className="admin-tooltip"
			style={{
				left: showTooltip.details.cx,
				top: showTooltip.details.cy,
			}}
		>
			<ClayCard.Body className="row">
				<ClayForm.Group className="col-12 form-group-sm text-left">
					<label htmlFor={`${namespace}pin-position`}>
						{Liferay.Language.get('position')}
					</label>

					<ClayInput
						id={`${namespace}pin-position`}
						onChange={(event) => setPinLabel(event.target.value)}
						placeholder={Liferay.Language.get(
							'insert-your-name-here'
						)}
						type="text"
						value={pinLabel}
					/>
				</ClayForm.Group>

				<ClayForm.Group className="col-12 form-group-sm">
					<ClayRadioGroup
						className="d-flex justify-content-start mt-4"
						inline
						onSelectedValueChange={(val) => setLinkedValue(val)}
						selectedValue={linkedValue}
					>
						<ClayRadio
							label={Liferay.Language.get('linked-to-sku')}
							value="sku"
						/>

						<ClayRadio
							label={Liferay.Language.get('linked-to-diagram')}
							value="diagram"
						/>
					</ClayRadioGroup>
				</ClayForm.Group>

				<ClayForm.Group className="col-9 form-group-sm text-left">
					<label htmlFor={`${namespace}pin-sku`}>
						{Liferay.Language.get('select-sku')}
					</label>
					<ClayAutocomplete>
						<ClayAutocomplete.Input
							onChange={event => setSku(event.target.value)}
							value={sku}
						/>
						<ClayAutocomplete.DropDown
							active={skus}
						>
							<ClayDropDown.ItemList>
								{skus && !skus.length && (
									<ClayDropDown.Item className="disabled">
										{"No Results Found"}
									</ClayDropDown.Item>
								)}
								{
									skus?.length &&
									skus.map(item => (
									 <ClayAutocomplete.Item
										 key={item.id}
										 match={sku}
										 value={item.sku}
									 />
								 ))}
							</ClayDropDown.ItemList>
						</ClayAutocomplete.DropDown>
					</ClayAutocomplete>
				</ClayForm.Group>

				<ClayForm.Group className="col-3 form-group-sm">
					<label htmlFor={`${namespace}pin-quantity`}>
						{Liferay.Language.get('quantity')}
					</label>

					<ClayInput
						id={`${namespace}pin-quantity`}
						onChange={(event) =>
							setQuantity(parseInt(event.target.value, 10))
						}
						type="number"
						value={quantity}
					/>
				</ClayForm.Group>

				<ClayForm.Group className="col-6 d-flex form-group-sm justify-content-start mt-4">
					<ClayButton
						displayType="link"
						onClick={() => {
							const pinToDelete = ({
								id: showTooltip.details.id,
								number: pinLabel,
								positionX: showTooltip.details.cx,
								positionY: showTooltip.details.cy,
							});
							deletePin(pinToDelete)
							setRemovePinHandler({
								handler: true,
								pin: showTooltip.details.id,
							});
							setShowTooltip({
								details: {
									cx: null,
									cy: null,
									id: null,
									label: null,
									linked_to_sku: 'sku',
									quantity: null,
									sku: '',
								},
								tooltip: false,
							});
						}}
					>
						{Liferay.Language.get('delete')}
					</ClayButton>
				</ClayForm.Group>

				<ClayForm.Group className="col-6 d-flex form-group-sm justify-content-between mt-4">
					<ClayButton
						displayType="secondary"
						onClick={() => {
							setShowTooltip({
								details: {
									cx: null,
									cy: null,
									id: null,
									label: '',
									linked_to_sku: 'sku',
									quantity: null,
									sku: '',
								},
								tooltip: false,
							})
						}
						}
					>
						{Liferay.Language.get('close')}
					</ClayButton>
					<ClayButton
						displayType="primary"
						onClick={() => {
							const node = {	
								id: showTooltip.details.id,
								number: pinLabel,
								positionX: showTooltip.details.cx,
								positionY: showTooltip.details.cy,
							
							}
							updatePin(node);
							setShowTooltip({
								details: {
									cx: showTooltip.details.cx,
									cy: showTooltip.details.cy,
									id: showTooltip.details.id,
									label: pinLabel,
									linked_to_sku: linkedValue,
									quantity,
									sku: sku || "",
								},
								tooltip: false,
							});
						}}
					>
						{Liferay.Language.get('save')}
					</ClayButton>
				</ClayForm.Group>
			</ClayCard.Body>
		</ClayCard>
	);
};

AdminTooltip.propTypes = {
	setShowTooltip: PropTypes.func,
	showTooltip: PropTypes.shape({
		details: PropTypes.shape({
			cx: PropTypes.double,
			cy: PropTypes.double,
			id: PropTypes.number,
			label: PropTypes.string,
			linked_to_sku: PropTypes.oneOf(['sku', 'diagram']),
			quantity: PropTypes.number,
			sku: PropTypes.string,
		}),
		tooltip: PropTypes.bool,
	}),
};

export default AdminTooltip;
