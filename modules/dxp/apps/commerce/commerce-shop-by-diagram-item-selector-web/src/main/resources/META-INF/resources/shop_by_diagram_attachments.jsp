<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
CommerceShopByDiagramItemSelectorViewDisplayContext commerceShopByDiagramItemSelectorViewDisplayContext = (CommerceShopByDiagramItemSelectorViewDisplayContext)request.getAttribute(
	CommerceShopByDiagramItemSelectorWebKeys.COMMERCE_SHOP_BY_DIAGRAM_ITEM_SELECTOR_VIEW_DISPLAY_CONTEXT);

int cur = ParamUtil.getInteger(request, SearchContainer.DEFAULT_CUR_PARAM, SearchContainer.DEFAULT_CUR);
int delta = ParamUtil.getInteger(request, SearchContainer.DEFAULT_DELTA_PARAM, SearchContainer.DEFAULT_DELTA);

int[] startAndEnd = SearchPaginationUtil.calculateStartAndEnd(cur, delta);

int start = startAndEnd[0];
int end = startAndEnd[1];

Folder folder = commerceShopByDiagramItemSelectorViewDisplayContext.fetchAttachmentsFolder(themeDisplay.getUserId(), themeDisplay.getRefererGroupId());

List<RepositoryEntry> portletFileEntries = new ArrayList<>();
int portletFileEntriesCount = 0;

if (folder != null) {
	if (commerceShopByDiagramItemSelectorViewDisplayContext.isSearch()) {
		SearchContext searchContext = SearchContextFactory.getInstance(request);

		searchContext.setEnd(end);
		searchContext.setFolderIds(new long[] {folder.getFolderId()});
		searchContext.setStart(start);

		Hits hits = PortletFileRepositoryUtil.searchPortletFileEntries(folder.getRepositoryId(), searchContext);

		portletFileEntriesCount = hits.getLength();

		for (Document doc : hits.getDocs()) {
			long fileEntryId = GetterUtil.getLong(doc.get(Field.ENTRY_CLASS_PK));

			FileEntry fileEntry = null;

			try {
				fileEntry = PortletFileRepositoryUtil.getPortletFileEntry(fileEntryId);
			}
			catch (Exception e) {
				/*
				if (_log.isWarnEnabled()) {
					_log.warn("Documents and Media search index is stale and contains file entry {" + fileEntryId + "}");
				}
				*/

				continue;
			}

			portletFileEntries.add(fileEntry);
		}
	}
	else {
		portletFileEntries.addAll(
			PortletFileRepositoryUtil.getPortletFileEntries(scopeGroupId, folder.getFolderId(), WorkflowConstants.STATUS_APPROVED, start, end, commerceShopByDiagramItemSelectorViewDisplayContext.getOrderByComparator()));
		portletFileEntriesCount = PortletFileRepositoryUtil.getPortletFileEntriesCount(scopeGroupId, folder.getFolderId(), WorkflowConstants.STATUS_APPROVED);
	}
}
%>

<liferay-item-selector:repository-entry-browser
	allowedCreationMenuUIItemKeys="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getAllowedCreationMenuUIItemKeys() %>"
	emptyResultsMessage='<%= LanguageUtil.get(resourceBundle, "there-are-no-blog-attachments") %>'
	extensions="<%= ListUtil.fromArray(commerceShopByDiagramItemSelectorViewDisplayContext.getImageExtensions()) %>"
	itemSelectedEventName="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getItemSelectedEventName() %>"
	itemSelectorReturnTypeResolver="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getItemSelectorReturnTypeResolver() %>"
	maxFileSize="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getImageMaxSize() %>"
	mimeTypeRestriction="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getMimeTypeRestriction() %>"
	portletURL="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getPortletURL(request, liferayPortletResponse) %>"
	repositoryEntries="<%= portletFileEntries %>"
	repositoryEntriesCount="<%= portletFileEntriesCount %>"
	showDragAndDropZone="<%= true %>"
	tabName="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getTitle(locale) %>"
	uploadURL="<%= commerceShopByDiagramItemSelectorViewDisplayContext.getUploadURL(liferayPortletResponse) %>"
/>

<%!
// private static final Log _log = LogFactoryUtil.getLog("com_liferay_blogs_item_selector_web.blogs_attachments_jsp");
%>