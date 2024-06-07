/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import React from 'react';

export function CommerceReturnItemAcceptedDataRenderer({
	itemData: {accepted, authorizeReturnWithoutReturningProducts, authorized},
}) {
	return (
		<>
			{authorized > 0 && authorizeReturnWithoutReturningProducts
				? Liferay.Language.get('not-required')
				: accepted}
		</>
	);
}
