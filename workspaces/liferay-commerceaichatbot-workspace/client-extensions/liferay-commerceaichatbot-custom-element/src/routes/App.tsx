/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useModal} from '@clayui/modal';
import {useEffect, useState} from 'react';
import {createPortal} from 'react-dom';

import CommerceAIChatBot from '../components/CommerceAIChatBot.tsx';
import DisplayButton from '../components/DisplayButton';

function App() {
	const [container, setContainer] = useState<{
		bottombar: HTMLElement | null;
	}>({
		bottombar: null,
	});

	const modal = useModal({
		defaultOpen: false,

	});

	useEffect(() => {
		setContainer({
			bottombar: document.querySelector('#footer'),
		});
	}, []);

	return (
		<>
			{modal.open && <CommerceAIChatBot modal={modal} />}

			{container.bottombar &&
				createPortal(
					<DisplayButton onClick={() => modal.onOpenChange(true)} />,
					container.bottombar
				)}
		</>
	);
}

export default App;
