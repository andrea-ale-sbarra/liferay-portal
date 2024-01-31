/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shop.by.diagram.item.selector.web.internal;

import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.shop.by.diagram.item.selector.constants.CommerceShopByDiagramItemSelectorViewConstants;
import com.liferay.commerce.shop.by.diagram.item.selector.criterion.CommerceShopByDiagramItemSelectorCriterion;
import com.liferay.commerce.shop.by.diagram.item.selector.web.internal.constants.CommerceShopByDiagramItemSelectorWebKeys;
import com.liferay.commerce.shop.by.diagram.item.selector.web.internal.display.context.CommerceShopByDiagramItemSelectorViewDisplayContext;
import com.liferay.item.selector.ItemSelectorReturnType;
import com.liferay.item.selector.ItemSelectorReturnTypeResolverHandler;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.criteria.FileEntryItemSelectorReturnType;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.util.ListUtil;

import java.io.IOException;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.portlet.PortletURL;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	property = "item.selector.view.key=" + CommerceShopByDiagramItemSelectorViewConstants.ITEM_SELECTOR_VIEW_KEY,
	service = ItemSelectorView.class
)
public class CommerceShopByDiagramItemSelectorView
	implements ItemSelectorView<CommerceShopByDiagramItemSelectorCriterion> {

	@Override
	public Class<CommerceShopByDiagramItemSelectorCriterion>
		getItemSelectorCriterionClass() {

		return CommerceShopByDiagramItemSelectorCriterion.class;
	}

	public ServletContext getServletContext() {
		return _servletContext;
	}

	@Override
	public List<ItemSelectorReturnType> getSupportedItemSelectorReturnTypes() {
		return _supportedItemSelectorReturnTypes;
	}

	@Override
	public String getTitle(Locale locale) {
		return _language.get(locale, "commerce-shop-by-diagram-images");
	}

	@Override
	public void renderHTML(
			ServletRequest servletRequest, ServletResponse servletResponse,
			CommerceShopByDiagramItemSelectorCriterion
				commerceShopByDiagramItemSelectorCriterion,
			PortletURL portletURL, String itemSelectedEventName, boolean search)
		throws IOException, ServletException {

		ServletContext servletContext = getServletContext();

		RequestDispatcher requestDispatcher =
			servletContext.getRequestDispatcher(
				"/shop_by_diagram_attachments.jsp");

		CommerceShopByDiagramItemSelectorViewDisplayContext
			commerceShopByDiagramItemSelectorViewDisplayContext =
				new CommerceShopByDiagramItemSelectorViewDisplayContext(
					_commerceCatalogLocalService,
					commerceShopByDiagramItemSelectorCriterion, this,
					(HttpServletRequest)servletRequest, itemSelectedEventName,
					_itemSelectorReturnTypeResolverHandler, portletURL, search);

		servletRequest.setAttribute(
			CommerceShopByDiagramItemSelectorWebKeys.
				COMMERCE_SHOP_BY_DIAGRAM_ITEM_SELECTOR_VIEW_DISPLAY_CONTEXT,
			commerceShopByDiagramItemSelectorViewDisplayContext);

		requestDispatcher.include(servletRequest, servletResponse);
	}

	private static final List<ItemSelectorReturnType>
		_supportedItemSelectorReturnTypes = Collections.unmodifiableList(
			ListUtil.fromArray(new FileEntryItemSelectorReturnType()));

	@Reference
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	@Reference
	private ItemSelectorReturnTypeResolverHandler
		_itemSelectorReturnTypeResolverHandler;

	@Reference
	private Language _language;

	@Reference(
		target = "(osgi.web.symbolicname=com.liferay.commerce.shop.by.diagram.item.selector.web)"
	)
	private ServletContext _servletContext;

}