/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import PropTypes from 'prop-types';
import React from 'react';

export function CommerceReturnItemStatusDataRenderer(props) {
	const getLabelType = (label) => {
		switch (label) {
			case 'accepted':
			case 'authorized':
			case 'completed':
			case 'defined':
			case 'partiallyAccepted':
			case 'partiallyAuthorized':
			case 'processed':
				return 'label-success';
			case 'notAccepted':
			case 'notAuthorized':
				return 'label-danger';
			default:
				return 'label-secondary';
		}
	};

	return props.value ? (
		<span className="taglib-workflow-status">
			<span className="workflow-status">
				<strong className={`label ${getLabelType(props.value.key)}`}>
					{props.value.name}
				</strong>
			</span>
		</span>
	) : null;
}

CommerceReturnItemStatusDataRenderer.propTypes = {
	value: PropTypes.shape({
		key: PropTypes.string,
		name: PropTypes.string,
	}),
};
