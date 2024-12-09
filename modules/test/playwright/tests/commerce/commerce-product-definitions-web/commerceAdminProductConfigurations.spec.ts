/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {applicationsMenuPageTest} from '../../../fixtures/applicationsMenuPageTest';
import {commercePagesTest} from '../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../fixtures/dataApiHelpersTest';
import {featureFlagsTest} from '../../../fixtures/featureFlagsTest';
import {loginTest} from '../../../fixtures/loginTest';
import getRandomString from '../../../utils/getRandomString';

export const test = mergeTests(
	applicationsMenuPageTest,
	commercePagesTest,
	dataApiHelpersTest,
	featureFlagsTest({
		'LPD-10889': true,
	}),
	loginTest()
);

test('LPD-42555 Verify configuration list table appears', async ({
	applicationsMenuPage,
	commerceAdminProductConfigurationListsPage,
}) => {
	await applicationsMenuPage.goToCommerceProductConfigurationLists(false);

	await expect(
		commerceAdminProductConfigurationListsPage.table
	).toBeVisible();
});

test('LPD-41420 Verify configuration list eligibility management is available', async ({
	apiHelpers,
	applicationsMenuPage,
	commerceAdminProductConfigurationListsPage,
	page,
}) => {
	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog();

	const productConfigurationList =
		await apiHelpers.headlessCommerceAdminCatalog.postProductConfigurationList(
			catalog.id,
			getRandomString()
		);

	const accountGroup = await apiHelpers.headlessAdminUser.postAccountGroup({
		name: getRandomString(),
	});

	apiHelpers.data.push({id: accountGroup.id, type: 'accountGroup'});

	await apiHelpers.headlessCommerceAdminCatalog.postProductConfigurationListAccountGroup(
		accountGroup.id,
		productConfigurationList.id
	);

	const site = await apiHelpers.headlessSite.createSite({
		name: getRandomString(),
	});

	apiHelpers.data.push({id: site.id, type: 'site'});

	const channel = await apiHelpers.headlessCommerceAdminChannel.postChannel({
		name: getRandomString(),
		siteGroupId: site.id,
	});

	await apiHelpers.headlessCommerceAdminCatalog.postProductConfigurationListChannel(
		channel.id,
		productConfigurationList.id
	);

	const orderType = await apiHelpers.headlessCommerceAdminOrder.postOrderType(
		{
			active: true,
		}
	);

	await apiHelpers.headlessCommerceAdminCatalog.postProductConfigurationListOrderType(
		orderType.id,
		productConfigurationList.id
	);

	await applicationsMenuPage.goToCommerceProductConfigurationLists(false);

	await expect(
		commerceAdminProductConfigurationListsPage.table
	).toBeVisible();

	await expect(
		await page.getByText(productConfigurationList.name)
	).toBeVisible();

	await page.getByText(productConfigurationList.name).click();

	await expect(
		commerceAdminProductConfigurationListsPage.eligibilitiesTab
	).toBeVisible();

	await commerceAdminProductConfigurationListsPage.eligibilitiesTab.click();

	await expect(await page.getByText('Account Eligibility')).toBeVisible();
	await expect(await page.getByText(accountGroup.name)).toBeVisible();
	await expect(await page.getByText('Channel Eligibility')).toBeVisible();
	await expect(await page.getByText(channel.name)).toBeVisible();
	await expect(await page.getByText('Order Type Eligibility')).toBeVisible();
	await expect(await page.getByText(orderType.name['en_US'])).toBeVisible();
});

test('LPD-43390 Create child configuration list', async ({
	applicationsMenuPage,
	commerceAdminProductConfigurationListsPage,
}) => {
	await applicationsMenuPage.goToCommerceProductConfigurationLists(false);

	await expect(
		commerceAdminProductConfigurationListsPage.table
	).toBeVisible();

	await commerceAdminProductConfigurationListsPage.addConfigurationList.click();

	await commerceAdminProductConfigurationListsPage.addConfigurationListName.fill(
		'Test'
	);
	await commerceAdminProductConfigurationListsPage.addConfigurationListPriority.fill(
		'1'
	);
	await commerceAdminProductConfigurationListsPage.addConfigurationListCatalog.selectOption(
		{label: 'Master'}
	);
	await commerceAdminProductConfigurationListsPage.addConfigurationListParentList.click();
	await commerceAdminProductConfigurationListsPage.addConfigurationListParentListElement.click();
	await commerceAdminProductConfigurationListsPage.addConfigurationListSaveButton.click();

	await expect(
		commerceAdminProductConfigurationListsPage.newConfigurationListName
	).toHaveText('Test');
});
