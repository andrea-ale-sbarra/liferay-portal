/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {commercePagesTest} from '../../../../fixtures/commercePagesTest';
import {dataApiHelpersTest} from '../../../../fixtures/dataApiHelpersTest';
import {loginTest} from '../../../../fixtures/loginTest';

export const test = mergeTests(
	commercePagesTest,
	dataApiHelpersTest,
	loginTest()
);

test('COMMERCE-11835 Account Supplier role user can upload diagram file/image', async ({
	apiHelpers,
	commerceAdminProductDetailsDiagramPage,
	commerceAdminProductDetailsPage,
	commerceAdminProductPage,
	page,
}) => {
	await page.goto('/');

	const account = await apiHelpers.headlessAdminUser.postAccount({
		name: 'Supplier account',
		type: 'supplier',
	});

	const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
		accountId: account.id,
	});

	await apiHelpers.headlessAdminUser.assignUserToAccountByEmailAddress(
		account.id,
		['demo.unprivileged@liferay.com']
	);

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		productType: 'diagram',
	});

	const rolesResponse = await apiHelpers.headlessAdminUser.getAccountRoles(
		account.id
	);

	const accountSupplierRole = rolesResponse?.items?.filter((role) => {
		return role.name === 'Account Supplier';
	});

	await apiHelpers.headlessAdminUser.assignAccountRoles(
		account.externalReferenceCode,
		accountSupplierRole[0].id,
		'demo.unprivileged@liferay.com'
	);

	await commerceAdminProductPage.gotoProduct(product.name['en_US']);
	await commerceAdminProductDetailsPage.goToProductDiagram();
	await commerceAdminProductDetailsDiagramPage.goToDragAndDropImages();

	await expect(
		commerceAdminProductDetailsDiagramPage.dragAndDropImages
	).toBeVisible();
});

test('COMMERCE-7025 Add a new pin to a diagram product', async ({
																	apiHelpers,
																	commerceAdminProductPage,
																	commerceAdminProductDetailsPage,
																	commerceAdminProductDetailsDiagramPage,
																	page,
																}) => {
	const productName = `Diagram T-Shirt ${getRandomString()}`;
	const catalogName = 'Master';

	let masterCatalog = (await apiHelpers.headlessCommerceAdminCatalog.getCatalogsPage()).items.find(c => c.name === catalogName);
	if (!masterCatalog) {
		masterCatalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({ name: catalogName });
	}

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: masterCatalog.id,
		name: { en_US: productName },
		productType: 'diagram',
	});

	await commerceAdminProductPage.gotoProduct(productName);
	await commerceAdminProductDetailsPage.goToProductDiagram();

	await commerceAdminProductDetailsDiagramPage.goToDragAndDropImages();
	await page
		.frameLocator('iframe[title="Select File"]')
		.getByRole('link', { name: 'Provided by Liferay' })
		.click();
	await page
		.frameLocator('iframe[title="Select File"]')
		.locator(
			'[id="_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_repositoryEntriesSearchContainer_1"] img'
		)
		.click();

	await commerceAdminProductDetailsPage.publishLink.click();
	await waitForAlert(page);

	await commerceAdminProductDetailsDiagramPage.addPin(1, 'Test', 'Not Linked to a Catalog', 1);

	await expect(commerceAdminProductDetailsDiagramPage.diagramPin(1)).toBeVisible();
});

test('COMMERCE-7019 Add Diagram Product', async ({
													 apiHelpers,
													 applicationsMenuPage,
													 commerceAdminProductPage,
													 commerceAdminProductDetailsPage,
													 page,
												 }) => {
	const productName = `Diagram T-Shirt ${getRandomString()}`;
	const productShortDescription = `Diagram T-Shirt Short Description ${getRandomString()}`;
	const productFullDescription = `Diagram T-Shirt Full Description ${getRandomString()}`;
	const catalogName = 'Master';

	let masterCatalog = (await apiHelpers.headlessCommerceAdminCatalog.getCatalogsPage()).items.find(c => c.name === catalogName);
	if (!masterCatalog) {
		masterCatalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({ name: catalogName });
	}

	await applicationsMenuPage.goToProducts();

	await commerceAdminProductPage.openProductAddMenu();
	await commerceAdminProductPage.addProductMenuItem.click();
	await commerceAdminProductPage.generalProductTypeOption.click();

	await commerceAdminProductPage.nameInput.fill(productName);
	await commerceAdminProductPage.shortDescriptionInput.fill(productShortDescription);
	await commerceAdminProductPage.fullDescriptionInput.fill(productFullDescription);
	await commerceAdminProductPage.selectProductType('Diagram');

	await commerceAdminProductPage.catalogSelect.selectOption({ label: catalogName });
	await commerceAdminProductPage.saveButton.click();
	await waitForAlert(page);

	await expect(commerceAdminProductPage.productNameHeading(productName)).toBeVisible();
	await expect(page.getByText(productShortDescription)).toBeVisible();
	await expect(page.getByText(productFullDescription)).toBeVisible();

	await commerceAdminProductDetailsPage.goToProductDiagram();

	await commerceAdminProductDetailsPage.backButton.click();

	await commerceAdminProductPage.managementToolbarSearchInput.fill(productName);
	await commerceAdminProductPage.managementToolbarSearchInput.press('Enter');

	await expect(commerceAdminProductPage.productEntryLink(productName)).toBeVisible();

	await commerceAdminProductPage.productEntryLink(productName).click();
	await expect(commerceAdminProductPage.productNameHeading(productName)).toBeVisible();
});

test('COMMERCE-8865 Can Drag Function Work After Double Click On Mapped Product', async ({
																							 apiHelpers,
																							 applicationsMenuPage,
																							 commerceAdminProductPage,
																							 commerceAdminProductDetailsPage,
																							 commerceAdminProductDetailsDiagramPage,
																							 page,
																						 }) => {
	const siteName = `MiniumSite-${getRandomString()}`;
	const { site, catalog } = await miniumSetUp(apiHelpers, siteName);

	const linkedProductName = 'ABS Sensor';
	const linkedProductSku = 'MIN93015';
	const linkedProduct = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: { en_US: linkedProductName },
		skus: [{ sku: linkedProductSku }],
	});

	const diagramProductName = `Diagram T-Shirt ${getRandomString()}`;
	const diagramProduct = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: catalog.id,
		name: { en_US: diagramProductName },
		productType: 'diagram',
	});

	await applicationsMenuPage.goToProducts();
	await commerceAdminProductPage.managementToolbarSearchInput.fill(diagramProductName);
	await commerceAdminProductPage.managementToolbarSearchInput.press('Enter');
	await commerceAdminProductPage.productEntryLink(diagramProductName).click();
	await commerceAdminProductDetailsPage.goToProductDiagram();

	await commerceAdminProductDetailsDiagramPage.goToDragAndDropImages();
	await page
		.frameLocator('iframe[title="Select File"]')
		.getByRole('link', { name: 'Provided by Liferay' })
		.click();
	await page
		.frameLocator('iframe[title="Select File"]')
		.locator(
			'[id="_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_repositoryEntriesSearchContainer_1"] img'
		)
		.click();

	await commerceAdminProductDetailsPage.publishLink.click();
	await waitForAlert(page);

	await commerceAdminProductDetailsDiagramPage.addPin(
		1,
		linkedProductName,
		'Linked to a SKU',
		1,
		'100,50',
		linkedProductSku,
		linkedProduct.productId
	);
	await commerceAdminProductDetailsPage.publishLink.click();
	await waitForAlert(page);
	await page.reload();

	await commerceAdminProductDetailsDiagramPage.diagramMappedProductContent(linkedProductName).dblclick();

	await expect(commerceAdminProductDetailsDiagramPage.checkDiagramZoomValue()).not.toHaveText('NaN%', { ignoreCase: true });

	const diagramImageLocator = commerceAdminProductDetailsDiagramPage.diagramImage();
	await diagramImageLocator.hover();
	await page.mouse.down();
	await page.mouse.move(page.viewportSize().width / 2 + 300, page.viewportSize().height / 2 + 0);
	await page.mouse.up();
});

test('COMMERCE-7024 Cannot Publish A Diagram Product Without An Image', async ({
																				   apiHelpers,
																				   applicationsMenuPage,
																				   commerceAdminProductPage,
																				   commerceAdminProductDetailsPage,
																				   page,
																			   }) => {
	const productName = `Diagram Product ${getRandomString()}`;
	const catalogName = 'Master';

	let masterCatalog = (await apiHelpers.headlessCommerceAdminCatalog.getCatalogsPage()).items.find(c => c.name === catalogName);
	if (!masterCatalog) {
		masterCatalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({ name: catalogName });
	}

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: masterCatalog.id,
		name: { en_US: productName },
		productType: 'diagram',
	});

	await applicationsMenuPage.goToProducts();
	await commerceAdminProductPage.managementToolbarSearchInput.fill(productName);
	await commerceAdminProductPage.managementToolbarSearchInput.press('Enter');
	await commerceAdminProductPage.productEntryLink(productName).click();

	await commerceAdminProductDetailsPage.goToProductDiagram();

	await commerceAdminProductDetailsPage.publishLink.click();

	await expect(page.locator('.alert-danger')).toBeVisible();
	await expect(page.locator('.alert-danger')).toContainText('Please select an existing file.');
});

test('COMMERCE-7024 User is able to remove and replace a diagram product image', async ({
																							apiHelpers,
																							applicationsMenuPage,
																							commerceAdminProductPage,
																							commerceAdminProductDetailsPage,
																							commerceAdminProductDetailsDiagramPage,
																							page,
																						}) => {
	const productName = `Diagram Product ${getRandomString()}`;
	const catalogName = 'Master';
	const originalImageTitle = 'Black';
	const newImageTitle = 'White';

	let masterCatalog = (await apiHelpers.headlessCommerceAdminCatalog.getCatalogsPage()).items.find(c => c.name === catalogName);
	if (!masterCatalog) {
		masterCatalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({ name: catalogName });
	}

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: masterCatalog.id,
		name: { en_US: productName },
		productType: 'diagram',
	});

	await applicationsMenuPage.goToProducts();
	await commerceAdminProductPage.managementToolbarSearchInput.fill(productName);
	await commerceAdminProductPage.managementToolbarSearchInput.press('Enter');
	await commerceAdminProductPage.productEntryLink(productName).click();

	await commerceAdminProductDetailsPage.goToProductDiagram();

	await commerceAdminProductDetailsDiagramPage.goToDragAndDropImages();
	await page
		.frameLocator('iframe[title="Select File"]')
		.getByRole('link', { name: 'Provided by Liferay' })
		.click();
	await page
		.frameLocator('iframe[title="Select File"]')
		.locator(
			'[id="_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_repositoryEntriesSearchContainer_1"] img'
		)
		.click();

	await commerceAdminProductDetailsPage.publishLink.click();
	await waitForAlert(page);

	await expect(commerceAdminProductDetailsDiagramPage.imageThumbnail(originalImageTitle)).toBeVisible();

	await commerceAdminProductDetailsDiagramPage.imageTrashButton(originalImageTitle).click();
	await waitForAlert(page);

	await expect(commerceAdminProductDetailsDiagramPage.imageThumbnail(originalImageTitle)).not.toBeVisible();

	await commerceAdminProductDetailsDiagramPage.goToDragAndDropImages();

	await page
		.frameLocator('iframe[title="Select File"]')
		.getByRole('link', { name: 'Provided by Liferay' })
		.click();
	await page
		.frameLocator('iframe[title="Select File"]')
		.locator(
			'[id="_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_repositoryEntriesSearchContainer_1"] img'
		)
		.click();

	await commerceAdminProductDetailsPage.publishLink.click();
	await waitForAlert(page);

	await expect(commerceAdminProductDetailsDiagramPage.imageThumbnail(newImageTitle)).toBeVisible();
});

test('COMMERCE-7100 Verify user can change a diagram product type to SVG', async ({
																					  apiHelpers,
																					  applicationsMenuPage,
																					  commerceAdminProductPage,
																					  commerceAdminProductDetailsPage,
																					  commerceAdminProductDetailsDiagramPage,
																					  page,
																				  }) => {
	const productName = `Diagram Product SVG ${getRandomString()}`;
	const catalogName = 'Master';

	let masterCatalog = (await apiHelpers.headlessCommerceAdminCatalog.getCatalogsPage()).items.find(c => c.name === catalogName);
	if (!masterCatalog) {
		masterCatalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({ name: catalogName });
	}

	const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
		catalogId: masterCatalog.id,
		name: { en_US: productName },
		productType: 'diagram',
	});

	await applicationsMenuPage.goToProducts();
	await commerceAdminProductPage.managementToolbarSearchInput.fill(productName);
	await commerceAdminProductPage.managementToolbarSearchInput.press('Enter');
	await commerceAdminProductPage.productEntryLink(productName).click();
	await commerceAdminProductDetailsPage.goToProductDiagram();

	await commerceAdminProductDetailsDiagramPage.goToDragAndDropImages();
	await page
		.frameLocator('iframe[title="Select File"]')
		.getByRole('link', { name: 'Provided by Liferay' })
		.click();
	await page
		.frameLocator('iframe[title="Select File"]')
		.locator(
			'[id="_com_liferay_item_selector_web_portlet_ItemSelectorPortlet_repositoryEntriesSearchContainer_1"] img[src$=".svg"]'
		)
		.click();

	await commerceAdminProductDetailsPage.publishLink.click();
	await waitForAlert(page);

	await commerceAdminProductDetailsDiagramPage.addPin(1, 'Test Pin', 'Linked to a SKU', 1, '50,50', 'SKU123', product.productId);
	await commerceAdminProductDetailsPage.publishLink.click();
	await waitForAlert(page);
	await page.reload();

	await expect(commerceAdminProductDetailsDiagramPage.settingsTypeDropdown).toHaveValue('default');

	await commerceAdminProductDetailsDiagramPage.diagramPin(1).click();
	await expect(commerceAdminProductDetailsDiagramPage.pinTooltip).toBeVisible();
	await commerceAdminProductDetailsDiagramPage.pinTooltipCancelButton.click();

	await commerceAdminProductDetailsDiagramPage.settingsTypeDropdown.selectOption('svg');

	await commerceAdminProductDetailsDiagramPage.diagramPin(1).click();

	await expect(commerceAdminProductDetailsDiagramPage.pinTooltip).toBeVisible();
	await expect(commerceAdminProductDetailsDiagramPage.pinTooltipInfo('Pin number: 1')).toBeVisible();
	await expect(commerceAdminProductDetailsDiagramPage.pinTooltipInfo('Type: SKU')).toBeVisible();
	await expect(commerceAdminProductDetailsDiagramPage.pinTooltipInfo('Quantity: 1')).toBeVisible();

	await commerceAdminProductDetailsDiagramPage.pinTooltipCancelButton.click();

	await page.locator('div.diagram-canvas svg rect').first().click();

	await expect(commerceAdminProductDetailsDiagramPage.pinTooltip).not.toBeVisible();
});

test('COMMERCE-7126 Remove Diagram Product', async ({
														apiHelpers,
														applicationsMenuPage,
														commerceAdminProductPage,
														commerceAdminProductDetailsPage,
														page,
													}) => {
	const productName = `Diagram T-Shirt ${getRandomString()}`;
	const productShortDescription = `Diagram T-Shirt Short Description ${getRandomString()}`;
	const productFullDescription = `Diagram T-Shirt Full Description ${getRandomString()}`;
	const catalogName = 'Master';

	let masterCatalog = (await apiHelpers.headlessCommerceAdminCatalog.getCatalogsPage()).items.find(c => c.name === catalogName);
	if (!masterCatalog) {
		masterCatalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({ name: catalogName });
	}

	await applicationsMenuPage.goToProducts();

	await commerceAdminProductPage.openProductAddMenu();
	await commerceAdminProductPage.addProductMenuItem.click();
	await commerceAdminProductPage.generalProductTypeOption.click();

	await commerceAdminProductPage.nameInput.fill(productName);
	await commerceAdminProductPage.shortDescriptionInput.fill(productShortDescription);
	await commerceAdminProductPage.fullDescriptionInput.fill(productFullDescription);
	await commerceAdminProductPage.selectProductType('Diagram');

	await commerceAdminProductPage.catalogSelect.selectOption({ label: catalogName });
	await commerceAdminProductPage.saveButton.click();
	await waitForAlert(page);

	await expect(commerceAdminProductPage.productNameHeading(productName)).toBeVisible();
	await expect(page.getByText(productShortDescription)).toBeVisible();
	await expect(page.getByText(productFullDescription)).toBeVisible();

	await commerceAdminProductDetailsPage.goToProductDiagram();

	await commerceAdminProductDetailsPage.backButton.click();

	await commerceAdminProductPage.managementToolbarSearchInput.fill(productName);
	await commerceAdminProductPage.managementToolbarSearchInput.press('Enter');

	await expect(commerceAdminProductPage.productEntryLink(productName)).toBeVisible();

	await commerceAdminProductPage.productEntryVerticalEllipsis(productName).click();
	await page.getByRole('menuitem', { name: 'Delete' }).click();
	await commerceAdminProductPage.confirmDeleteButton.click();

	await expect(commerceAdminProductPage.productEntryLink(productName)).not.toBeVisible();

	await expect(commerceAdminProductPage.emptySearchContainer).toBeVisible();

	await expect(page.locator('.alert-danger')).not.toBeVisible();
});
