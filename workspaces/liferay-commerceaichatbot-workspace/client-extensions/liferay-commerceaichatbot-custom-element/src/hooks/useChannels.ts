/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useEffect, useState} from 'react';

import useAIWizardContentOAuth2 from './useAIWizardOAuth2';

const useChannels = () => {
	const aiWizardOAuth2 = useAIWizardContentOAuth2();
	const [data, setData] = useState<any[]>([]);

	useEffect(() => {
		aiWizardOAuth2.getChannels().then((channels) => {
			setData(channels);
		}).catch((error) => {
			console.error('Error fetching channels:', error);
		});
	}, [aiWizardOAuth2]);

	return {data};
};

export default useChannels;
