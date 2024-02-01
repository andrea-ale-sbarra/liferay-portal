<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
CommerceShopByDiagramItemSelectorViewDisplayContext commerceShopByDiagramItemSelectorViewDisplayContext = (CommerceShopByDiagramItemSelectorViewDisplayContext)request.getAttribute(CommerceShopByDiagramItemSelectorWebKeys.COMMERCE_SHOP_BY_DIAGRAM_ITEM_SELECTOR_VIEW_DISPLAY_CONTEXT);
%>

<liferay-item-selector:repository-entry-browser
	allowedCreationMenuUIItemKeys="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getAllowedCreationMenuUIItemKeys() %>"
	emptyResultsMessage='<%= LanguageUtil.get(resourceBundle, "there-are-no-images") %>'
	extensions="<%= ListUtil.fromArray(commerceShopByDiagramItemSelectorViewDisplayContext.getImageExtensions()) %>"
	folderId="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getFolderId() %>"
	itemSelectedEventName="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getItemSelectedEventName() %>"
	itemSelectorReturnTypeResolver="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getItemSelectorReturnTypeResolver() %>"
	maxFileSize="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getImageMaxSize() %>"
	mimeTypeRestriction="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getMimeTypeRestriction() %>"
	portletURL="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getPortletURL(request, liferayPortletResponse) %>"
	repositoryEntries="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getPortletFileEntries() %>"
	repositoryEntriesCount="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getPortletFileEntriesCount() %>"
	showBreadcrumb="<%= true %>"
	showDragAndDropZone="<%= true %>"
	tabName="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getTitle(locale) %>"
	uploadURL="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getUploadURL(liferayPortletResponse) %>"
/>