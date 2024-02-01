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
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.item.selector.ItemSelectorReturnTypeResolver;
import com.liferay.item.selector.ItemSelectorReturnTypeResolverHandler;
import com.liferay.item.selector.taglib.servlet.taglib.util.RepositoryEntryBrowserTagUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.portlet.PortletURLUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.repository.model.RepositoryEntry;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

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
		PortletURL portletURL) {

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

		_portalPreferences = PortletPreferencesFactoryUtil.getPortalPreferences(
			httpServletRequest);
	}

	public Folder fetchAttachmentsFolder(long userId, long groupId) {
		return _commerceCatalogLocalService.fetchCatalogFolder(
			userId, groupId, CSDiagramSettingsConstants.FOLDER_NAME);
	}

	public Set<String> getAllowedCreationMenuUIItemKeys() {
		return Collections.emptySet();
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

	public List<RepositoryEntry> getPortletFileEntries(long groupId, long folderId)
		throws PortalException {

		return	ListUtil.toList(PortletFileRepositoryUtil.getPortletFileEntries(
						groupId, folderId, WorkflowConstants.STATUS_APPROVED,
						QueryUtil.ALL_POS, QueryUtil.ALL_POS,
						getOrderByComparator()),
					RepositoryEntry.class::cast);
	}

	public int getPortletFileEntriesCount(long groupId, long folderId)
		throws PortalException {

		return PortletFileRepositoryUtil.getPortletFileEntriesCount(
			groupId, folderId, WorkflowConstants.STATUS_APPROVED);
	}

	public PortletURL getPortletURL(
			HttpServletRequest httpServletRequest,
			LiferayPortletResponse liferayPortletResponse)
		throws PortletException {

		return PortletURLBuilder.create(
			PortletURLUtil.clone(_portletURL, liferayPortletResponse)
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

	private final CommerceCatalogLocalService _commerceCatalogLocalService;
	private final CommerceShopByDiagramItemSelectorCriterion
		_commerceShopByDiagramItemSelectorCriterion;
	private final CommerceShopByDiagramItemSelectorView
		_commerceShopByDiagramItemSelectorView;
	private CSDiagramSettingImageConfiguration
		_csDiagramSettingImageConfiguration;
	private final HttpServletRequest _httpServletRequest;
	private final String _itemSelectedEventName;
	private final ItemSelectorReturnTypeResolverHandler
		_itemSelectorReturnTypeResolverHandler;
	private final PortalPreferences _portalPreferences;
	private final PortletURL _portletURL;

}