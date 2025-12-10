/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';


export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

test('COMMERCE-11835 Account Supplier role user can upload diagram file/image', async ({
	
	page,
}) => {
	await page.goto('/');

});
