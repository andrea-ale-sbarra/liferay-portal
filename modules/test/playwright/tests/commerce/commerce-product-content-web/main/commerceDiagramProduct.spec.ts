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


test.describe('Commerce Product Diagrams Tests', () => {
	test('Add A New Pin', async ({
									 apiHelpers,
									 commerceAdminProductDetailsDiagramPage,
									 commerceAdminProductDetailsPage,
									 commerceAdminProductPage,
									 page,
								 }) => {
		// Given a diagram product with an image
		// This section replicates the "Given a diagram product with an image" task from Poshi
		// CommerceJSONProductsAPI._addCommerceProduct( ... )  <-- Poshi

		const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
			name: 'Master Catalog', // Replace with actual catalog details if needed
		});

		const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: 'Diagram T-Shirt',
			productType: 'diagram',
		});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']); // Navigate to product details
		await commerceAdminProductDetailsPage.goToProductDiagram(); // Go to the "Diagram" tab

		// CommerceProductImages.addDiagramImages( ... ) <-- Poshi
		// Replace the diagram image
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

		// Button.clickPublish();  <-- Poshi
		// Alert.viewSuccessMessage(); <-- Poshi
		// These lines publishes the product and validates the operation, you should validate the alert message.

		// When a pin is added on the product
		// This section replicates the "When a pin is added on the product" task from Poshi
		// CommerceDiagram.addPin( ... )  <-- Poshi

		//Add a diagram pin
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for label').fill('Test'); //Adapt this locator!
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for pinNumber').fill('1'); //Adapt this locator!
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for quantity').fill('1'); //Adapt this locator!
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for selectType').selectOption('Not Linked to a Catalog'); //Adapt this locator!

		// Then the pin should be saved on the product diagram
		// This section replicates the "Then the pin should be saved on the product diagram" task from Poshi
		// AssertElementPresent(key_pinNumber = 1, locator1 = "CommerceDiagrams#DIAGRAM_PIN"); <-- Poshi
		await expect(
			commerceAdminProductDetailsDiagramPage.page.locator(
				'CommerceDiagrams#DIAGRAM_PIN' //Adapt this locator!  It's crucial to get the right selector for the pin element.
			)
		).toBeVisible();
	});

	test('Add Diagram Product', async ({
										   apiHelpers,
										   commerceAdminProductPage
									   }) => {
		// CommerceProducts.openProductsAdmin(); <-- Poshi
		// It seems that this action is navigating in admin to reach products, they can be converted to playwright using baseURL and page.goto
		// CPCommerceCatalog.newProductsEntry( ... )  <-- Poshi
		// This function create a new product, and should be implemented to work properly
		const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
			name: 'Master Catalog', // Replace with actual catalog details if needed
		});

		const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: 'Diagram T-Shirt',
			productType: 'diagram'
		});
		// CommerceEntry.viewProductsEntry( ... )  <-- Poshi
		//This function view a product, and should be implemented to work properly

		// CommerceEntry.gotoMenuTab(menuTab = "Diagram");  <-- Poshi
		// It seems that this action is navigating to Diagram tab, they can be converted to playwright using baseURL and page.goto and also the page objects.
		// Click(locator1 = "Icon#BACK");  <-- Poshi
		//Click in the back button
		await commerceAdminProductPage.gotoProduct(product.name['en_US']); // Navigate to product details
		// CommerceNavigator.searchEntry(entryName = "Diagram T-Shirt");  <-- Poshi
		//This is the search action
		// AssertElementPresent( ... )  <-- Poshi
		//Validates the element is present
		await expect(commerceAdminProductPage.productsTableRowLink(product.name['en_US'])).toBeVisible();

		// CommerceNavigator.gotoEntry(entryName = "Diagram T-Shirt");  <-- Poshi
		//Goes to the diagram
		await commerceAdminProductPage.gotoProduct(product.name['en_US']); // Navigate to product details
		// It seems that these actions are navigating in admin to reach Diagram tab of product, they can be converted to playwright using baseURL and page.goto and also the page objects.
	});

	test('Can Drag Function Work After DoubleClick On Mapped Product', async ({
																				  apiHelpers,
																				  commerceAdminProductDetailsDiagramPage,
																				  commerceAdminProductDetailsPage,
																				  commerceAdminProductPage,
																				  page,
																			  }) => {
		// Given a Minium site
		// CommerceAccelerators.initializeNewSiteViaAccelerator(siteName = "Minium");  <-- Poshi

		const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
			name: 'Master Catalog', // Replace with actual catalog details if needed
		});

		const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: 'Diagram T-Shirt',
			productType: 'diagram',
		});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']); // Navigate to product details
		await commerceAdminProductDetailsPage.goToProductDiagram(); // Go to the "Diagram" tab
		// And a diagram product with an image
		// CommerceJSONProductsAPI._addCommerceProduct( ... )  <-- Poshi

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

		// Button.clickPublish();  <-- Poshi
		// Alert.viewSuccessMessage(); <-- Poshi
		//Add a diagram pin
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for label').fill('Test'); //Adapt this locator!
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for pinNumber').fill('1'); //Adapt this locator!
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for quantity').fill('1'); //Adapt this locator!
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for selectType').selectOption('Linked to a SKU'); //Adapt this locator!
		await commerceAdminProductDetailsDiagramPage.page.locator('locator for sku').fill('MIN93015'); //Adapt this locator!
		// CommerceDiagram.addPin( ... )  <-- Poshi
		// When a pin linked to a SKU is created
		// And the entry inside the mapped product section is clicked
		// DoubleClick(key_productName = "ABS Sensor", locator1 = "CommerceDiagrams#DIAGRAM_MAPPED_PRODUCTS_CONTENT");  <-- Poshi

		await page.locator('CommerceDiagrams#DIAGRAM_MAPPED_PRODUCTS_CONTENT').dblclick();

		// Then the zoom value of the diagram product is verified to be not NaN%
		// AssertElementNotPresent(key_zoomValue = "NaN%", locator1 = "CommerceDiagrams#CHECK_DIAGRAM_ZOOM_VALUE");  <-- Poshi
		// Validate the element is not present
		// And then the image can be dragged and dropped in different direction
		// CommerceDiagram.useDragOnBackground(image_name = "Commerce_Black", position = "300,0");  <-- Poshi
		//Drags and drops an element
	});

	test('Can Expand Image To Fit Full Screen', async ({
														   apiHelpers,
														   commerceAdminProductDetailsDiagramPage,
														   commerceAdminProductDetailsPage,
														   commerceAdminProductPage,
														   page,
													   }) => {
		// Given a diagram product with an image
		// CommerceProducts.openProductsAdmin(); <-- Poshi
		// CommerceNavigator.searchEntry(entryName = "Diagram T-Shirt"); <-- Poshi

		const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
			name: 'Master Catalog', // Replace with actual catalog details if needed
		});

		const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: 'Diagram T-Shirt',
			productType: 'diagram',
		});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']); // Navigate to product details
		await commerceAdminProductDetailsPage.goToProductDiagram(); // Go to the "Diagram" tab

		// CommerceEntry.gotoMenuTab(menuTab = "Diagram");  <-- Poshi

		// CommerceProductImages.addDiagramImages( ... )  <-- Poshi
		// Adds a diagram Image

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

		// When the Expand button is clicked
		// CommerceDiagram.clickExpand();  <-- Poshi
		// Clicks the button Expand
		// Then the diagram product image should fill up the page
		// AssertElementNotPresent(locator1 = "ApplicationsMenu#APPLICATIONS_MENU");  <-- Poshi
		// Validate that an element is not present
	});

	test('Cannot Publish A Diagram Product Without An Image', async ({
																		 apiHelpers,
																		 commerceAdminProductDetailsPage,
																		 commerceAdminProductPage,
																		 page,
																	 }) => {
		// Given a diagram product is created
		// CommerceJSONProductsAPI._addCommerceProduct( ... )  <-- Poshi

		const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
			name: 'Master Catalog', // Replace with actual catalog details if needed
		});

		const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: 'Diagram Product',
			productType: 'diagram',
		});

		// CommerceProducts.openProductsAdmin(); <-- Poshi
		// CommerceNavigator.gotoEntry(entryName = "Diagram Product");  <-- Poshi
		await commerceAdminProductPage.gotoProduct(product.name['en_US']); // Navigate to product details
		// When I try to publish the diagram product without an image
		// CommerceEntry.gotoMenuTab(menuTab = "Diagram");  <-- Poshi

		// Button.clickPublish();  <-- Poshi
		//Clicks in the button Publish
		await commerceAdminProductDetailsPage.publishLink.click();
		// Then the product cannot be published.
		// AssertTextEquals.assertText(locator1 = "Message#ERROR", value1 = "Close Error:Please select an existing file.");  <-- Poshi
		// Validates the text of an element

		await expect(page.locator("Message#ERROR")).toContainText('Please select an existing file');
	});

	test('Can Replace An Image', async ({
											apiHelpers,
											commerceAdminProductDetailsDiagramPage,
											commerceAdminProductDetailsPage,
											commerceAdminProductPage,
											page,
										}) => {
		// Given a diagram product has an image
		// CommerceJSONProductsAPI._addCommerceProduct( ... )  <-- Poshi

		const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
			name: 'Master Catalog', // Replace with actual catalog details if needed
		});

		const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: 'Diagram Product',
			productType: 'diagram',
		});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']); // Navigate to product details
		await commerceAdminProductDetailsPage.goToProductDiagram(); // Go to the "Diagram" tab
		// CommerceProductImages.addDiagramImages( ... )  <-- Poshi
		//Adds a diagram image

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
		// Button.click(button = "Publish");  <-- Poshi
		// Clicks the publish Button
		// Alert.viewSuccessMessage(); <-- Poshi

		await commerceAdminProductDetailsPage.publishLink.click();
		//Validates the alert message
		// WaitForElementPresent(key_image = "Commerce_Black", locator1 = "CommerceDiagrams#DIAGRAM_FILE");  <-- Poshi
		// Validates if the element is present

		// WaitForElementPresent(key_image = "Commerce_Black", locator1 = "CommerceDiagrams#DIAGRAM_IMAGE_SPECIFIC");  <-- Poshi
		//Validates if the element is present

		// When I replace the image
		// Button.clickTrash();  <-- Poshi
		//Clicks in the trash button
		// AssertElementNotPresent(key_image = "Commerce_Black", locator1 = "CommerceDiagrams#DIAGRAM_FILE");  <-- Poshi
		// Validates if the element is not present
		// CommerceEntry.uploadCatalogEntrySingleImage( ... )  <-- Poshi
		//Uploads a catalog image
		// Button.clickPublish();  <-- Poshi
		//Alert.viewSuccessMessage(); <-- Poshi

		// Then the image should be replaced.
		// WaitForElementPresent(key_image = "Commerce_White", locator1 = "CommerceDiagrams#DIAGRAM_FILE");  <-- Poshi
		// Validates if the element is present

		// WaitForElementPresent(key_image = "Commerce_White", locator1 = "CommerceDiagrams#DIAGRAM_IMAGE_SPECIFIC");  <-- Poshi
		//Validates if the element is present
	});

	test('Change Diagram Type', async ({
										   apiHelpers,
										   commerceAdminProductDetailsDiagramPage,
										   commerceAdminProductDetailsPage,
										   commerceAdminProductPage,
										   page,
									   }) => {
		// Given a diagram product with an svg diagram image
		// CommerceJSONProductsAPI._addCommerceProduct( ... )  <-- Poshi

		const catalog = await apiHelpers.headlessCommerceAdminCatalog.postCatalog({
			name: 'Master Catalog', // Replace with actual catalog details if needed
		});

		const product = await apiHelpers.headlessCommerceAdminCatalog.postProduct({
			catalogId: catalog.id,
			name: 'Diagram Product',
			productType: 'diagram',
		});

		await commerceAdminProductPage.gotoProduct(product.name['en_US']); // Navigate to product details
		await commerceAdminProductDetailsPage.goToProductDiagram(); // Go to the "Diagram" tab
		// CommerceProductImages.addDiagramImages( ... )  <-- Poshi
		//Adds a diagram Image

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

// Button.clickPublish();