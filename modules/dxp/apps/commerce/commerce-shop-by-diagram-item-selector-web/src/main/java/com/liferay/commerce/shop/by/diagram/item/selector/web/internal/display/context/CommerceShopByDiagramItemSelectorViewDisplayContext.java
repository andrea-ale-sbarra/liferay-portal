/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shop.by.diagram.item.selector.web.internal.display.context;

import com.liferay.commerce.product.constants.CPPortletKeys;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.shop.by.diagram.configuration.CSDiagramSettingImageConfiguration;
import com.liferay.commerce.shop.by.diagram.constants.CSDiagramSettingsConstants;
import com.liferay.commerce.shop.by.diagram.item.selector.criterion.CommerceShopByDiagramItemSelectorCriterion;
import com.liferay.commerce.shop.by.diagram.item.selector.web.internal.CommerceShopByDiagramItemSelectorView;
import com.liferay.document.library.constants.DLPortletKeys;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.item.selector.ItemSelectorReturnTypeResolver;
import com.liferay.item.selector.ItemSelectorReturnTypeResolverHandler;
import com.liferay.item.selector.taglib.servlet.taglib.util.RepositoryEntryBrowserTagUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.dao.search.SearchPaginationUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.repository.Repository;
import com.liferay.portal.kernel.repository.RepositoryProviderUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.repository.model.RepositoryEntry;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.util.PropsValues;
import com.liferay.staging.StagingGroupHelper;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Andrea Sbarra
 */
public class CommerceShopByDiagramItemSelectorViewDisplayContext {

	public CommerceShopByDiagramItemSelectorViewDisplayContext(
		CommerceCatalogLocalService commerceCatalogLocalService,
		CommerceShopByDiagramItemSelectorCriterion
			commerceShopByDiagramItemSelectorCriterion,
		CommerceShopByDiagramItemSelectorView
			commerceShopByDiagramItemSelectorView,
		HttpServletRequest httpServletRequest, String itemSelectedEventName,
		ItemSelectorReturnTypeResolverHandler
			itemSelectorReturnTypeResolverHandler,
		PortletURL portletURL, StagingGroupHelper stagingGroupHelper) {

		_commerceCatalogLocalService = commerceCatalogLocalService;
		_commerceShopByDiagramItemSelectorCriterion =
			commerceShopByDiagramItemSelectorCriterion;
		_commerceShopByDiagramItemSelectorView =
			commerceShopByDiagramItemSelectorView;
		_httpServletRequest = httpServletRequest;
		_itemSelectedEventName = itemSelectedEventName;
		_itemSelectorReturnTypeResolverHandler =
			itemSelectorReturnTypeResolverHandler;
		_portletURL = portletURL;
		_stagingGroupHelper = stagingGroupHelper;

		_portalPreferences = PortletPreferencesFactoryUtil.getPortalPreferences(
			httpServletRequest);
		_themeDisplay = (ThemeDisplay)httpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);
	}

	public Folder fetchAttachmentsFolder(long userId, long groupId) {
		return _commerceCatalogLocalService.fetchCatalogFolder(
			userId, groupId, CSDiagramSettingsConstants.FOLDER_NAME);
	}

	public Set<String> getAllowedCreationMenuUIItemKeys() {
		return Collections.emptySet();
	}

	public long getFolderId() throws PortalException {
		if (_folderId == null) {
			_folderId = _getFolderId(_httpServletRequest);
		}

		return _folderId;
	}

	public String[] getImageExtensions() throws ConfigurationException {
		return _getCSDiagramSettingImageConfiguration().imageExtensions();
	}

	public long getImageMaxSize() throws ConfigurationException {
		return _getCSDiagramSettingImageConfiguration().imageMaxSize();
	}

	public String getItemSelectedEventName() {
		return _itemSelectedEventName;
	}

	public ItemSelectorReturnTypeResolver<?, ?>
		getItemSelectorReturnTypeResolver() {

		return _itemSelectorReturnTypeResolverHandler.
			getItemSelectorReturnTypeResolver(
				_commerceShopByDiagramItemSelectorCriterion,
				_commerceShopByDiagramItemSelectorView, FileEntry.class);
	}

	public String getMimeTypeRestriction() {
		return _commerceShopByDiagramItemSelectorCriterion.
			getMimeTypeRestriction();
	}

	public OrderByComparator<FileEntry> getOrderByComparator() {
		return DLUtil.getRepositoryModelOrderByComparator(
			RepositoryEntryBrowserTagUtil.getOrderByCol(
				_httpServletRequest, _portalPreferences),
			RepositoryEntryBrowserTagUtil.getOrderByType(
				_httpServletRequest, _portalPreferences));
	}

	public List<RepositoryEntry> getPortletFileEntries()
		throws PortalException {

		OrderByComparator<Object> repositoryModelOrderByComparator =
			DLUtil.getRepositoryModelOrderByComparator(
				RepositoryEntryBrowserTagUtil.getOrderByCol(
					_httpServletRequest, _portalPreferences),
				RepositoryEntryBrowserTagUtil.getOrderByType(
					_httpServletRequest, _portalPreferences),
				true);

		int[] startAndEnd = _getStartAndEnd();

		Repository repository = _getRepository();

		return ListUtil.toList(
			DLAppServiceUtil.getFoldersAndFileEntriesAndFileShortcuts(
				repository.getRepositoryId(), getFolderId(),
				WorkflowConstants.STATUS_APPROVED, _getMimeTypes(), true, false,
				startAndEnd[0], startAndEnd[1],
				repositoryModelOrderByComparator),
			RepositoryEntry.class::cast);
	}

	public int getPortletFileEntriesCount() throws PortalException {
		Repository repository = _getRepository();

		return DLAppServiceUtil.getFoldersAndFileEntriesAndFileShortcutsCount(
			repository.getRepositoryId(), getFolderId(),
			WorkflowConstants.STATUS_APPROVED, _getMimeTypes(), true, false);
	}

	public PortletURL getPortletURL(
			HttpServletRequest httpServletRequest,
			LiferayPortletResponse liferayPortletResponse)
		throws PortletException, PortalException {

		return PortletURLBuilder.create(
			PortletURLUtil.clone(_portletURL, liferayPortletResponse)
		).setParameter(
			"folderId", getFolderId()
		).setParameter(
			"selectedTab", getTitle(httpServletRequest.getLocale())
		).buildPortletURL();
	}

	public String getTitle(Locale locale) {
		return _commerceShopByDiagramItemSelectorView.getTitle(locale);
	}

	public PortletURL getUploadURL(
		LiferayPortletResponse liferayPortletResponse) {

		return PortletURLBuilder.createActionURL(
			liferayPortletResponse, CPPortletKeys.CP_DEFINITIONS
		).setActionName(
			"/cp_definitions/upload_cs_diagram_setting_image"
		).buildPortletURL();
	}

	private CSDiagramSettingImageConfiguration
			_getCSDiagramSettingImageConfiguration()
		throws ConfigurationException {

		if (_csDiagramSettingImageConfiguration == null) {
			_csDiagramSettingImageConfiguration =
				ConfigurationProviderUtil.getSystemConfiguration(
					CSDiagramSettingImageConfiguration.class);
		}

		return _csDiagramSettingImageConfiguration;
	}

	private long _getFolderId(HttpServletRequest httpServletRequest)
		throws PortalException {

		if (httpServletRequest.getParameter("folderId") != null) {
			return ParamUtil.getLong(
				httpServletRequest, "folderId",
				DLFolderConstants.DEFAULT_PARENT_FOLDER_ID);
		}

		long selectedFileEntryId = ParamUtil.getLong(
			PortalUtil.getOriginalServletRequest(httpServletRequest),
			"selectedItemIds");

		if (selectedFileEntryId != 0) {
			FileEntry fileEntry = DLAppServiceUtil.getFileEntry(
				selectedFileEntryId);

			return fileEntry.getFolderId();
		}

		return DLFolderConstants.DEFAULT_PARENT_FOLDER_ID;
	}

	private String[] _getMimeTypes() {
		return ArrayUtil.append(
			PropsValues.DL_FILE_ENTRY_PREVIEW_IMAGE_MIME_TYPES,
			ContentTypes.IMAGE_SVG_XML);
	}

	private Repository _getRepository() throws PortalException {
		if (_repository != null) {
			return _repository;
		}

		Repository repository = null;

		if (getFolderId() != DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {
			DLFolder dlFolder = DLFolderLocalServiceUtil.fetchDLFolder(
				getFolderId());

			if ((dlFolder != null) && dlFolder.isMountPoint()) {
				repository = RepositoryProviderUtil.getRepository(
					dlFolder.getRepositoryId());
			}
			else {
				repository = RepositoryProviderUtil.getFolderRepository(
					getFolderId());
			}
		}
		else {
			repository = RepositoryProviderUtil.getRepository(
				_getStagingAwareGroupId());
		}

		_repository = repository;

		return _repository;
	}

	private long _getStagingAwareGroupId() {
		if (_groupId != null) {
			return _groupId;
		}

		_groupId = _stagingGroupHelper.getStagedPortletGroupId(
			_themeDisplay.getScopeGroupId(), DLPortletKeys.DOCUMENT_LIBRARY);

		return _groupId;
	}

	private int[] _getStartAndEnd() {
		if (_startAndEnd != null) {
			return _startAndEnd;
		}

		int cur = ParamUtil.getInteger(
			_httpServletRequest, SearchContainer.DEFAULT_CUR_PARAM,
			SearchContainer.DEFAULT_CUR);
		int delta = ParamUtil.getInteger(
			_httpServletRequest, SearchContainer.DEFAULT_DELTA_PARAM,
			SearchContainer.DEFAULT_DELTA);

		_startAndEnd = SearchPaginationUtil.calculateStartAndEnd(cur, delta);

		return _startAndEnd;
	}

	private final CommerceCatalogLocalService _commerceCatalogLocalService;
	private final CommerceShopByDiagramItemSelectorCriterion
		_commerceShopByDiagramItemSelectorCriterion;
	private final CommerceShopByDiagramItemSelectorView
		_commerceShopByDiagramItemSelectorView;
	private CSDiagramSettingImageConfiguration
		_csDiagramSettingImageConfiguration;
	private Long _folderId;
	private Long _groupId;
	private final HttpServletRequest _httpServletRequest;
	private final String _itemSelectedEventName;
	private final ItemSelectorReturnTypeResolverHandler
		_itemSelectorReturnTypeResolverHandler;
	private final PortalPreferences _portalPreferences;
	private final PortletURL _portletURL;
	private Repository _repository;
	private final StagingGroupHelper _stagingGroupHelper;
	private int[] _startAndEnd;
	private final ThemeDisplay _themeDisplay;

}