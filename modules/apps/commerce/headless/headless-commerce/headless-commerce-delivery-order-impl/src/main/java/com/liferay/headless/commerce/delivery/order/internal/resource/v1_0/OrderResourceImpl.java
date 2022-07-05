/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.headless.commerce.delivery.order.internal.resource.v1_0;

import com.liferay.commerce.constants.CommercePaymentConstants;
import com.liferay.commerce.constants.CommercePortletKeys;
import com.liferay.commerce.exception.NoSuchOrderException;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.service.CommerceOrderService;
import com.liferay.commerce.util.CommerceCheckoutStep;
import com.liferay.commerce.util.CommerceCheckoutStepServicesTracker;
import com.liferay.headless.commerce.delivery.order.dto.v1_0.Order;
import com.liferay.headless.commerce.delivery.order.internal.dto.v1_0.OrderDTOConverter;
import com.liferay.headless.commerce.delivery.order.resource.v1_0.OrderResource;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.events.ServicePreAction;
import com.liferay.portal.events.ThemeServicePreAction;
import com.liferay.portal.kernel.encryptor.Encryptor;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.servlet.DummyHttpServletResponse;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.URLCodec;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import com.liferay.portal.vulcan.dto.converter.DefaultDTOConverterContext;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import java.security.Key;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Andrea Sbarra
 */
@Component(
	enabled = false, properties = "OSGI-INF/liferay/rest/v1_0/order.properties",
	scope = ServiceScope.PROTOTYPE, service = OrderResource.class
)
public class OrderResourceImpl extends BaseOrderResourceImpl {

	@Override
	public Page<Order> getChannelAccountOrdersPage(
			Long accountId, Long channelId, Pagination pagination)
		throws Exception {

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannel(channelId);

		List<CommerceOrder> commerceOrders =
			_commerceOrderService.getPlacedCommerceOrders(
				commerceChannel.getGroupId(), accountId, null,
				pagination.getStartPosition(), pagination.getEndPosition());

		return Page.of(
			_toOrders(commerceOrders), pagination,
			_commerceOrderService.getPlacedCommerceOrdersCount(
				commerceChannel.getGroupId(), accountId, null));
	}

	@Override
	public Order getOrder(Long orderId) throws Exception {
		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			orderId);

		if (commerceOrder.isOpen()) {
			throw new NoSuchOrderException();
		}

		return _toOrder(commerceOrder);
	}

	@Override
	public String getOrderPaymentURL(Long orderId, String callbackURL)
		throws Exception {

		CommerceOrder commerceOrder = _commerceOrderService.getCommerceOrder(
			orderId);

		if (commerceOrder.isOpen()) {
			throw new NoSuchOrderException();
		}

		_initThemeDisplay(commerceOrder);

		StringBundler sb = new StringBundler(14);

		sb.append(_portal.getPortalURL(contextHttpServletRequest));
		sb.append(_portal.getPathModule());
		sb.append(CharPool.SLASH);
		sb.append(CommercePaymentConstants.SERVLET_PATH);
		sb.append("?groupId=");
		sb.append(commerceOrder.getGroupId());
		sb.append("&uuid=");
		sb.append(commerceOrder.getUuid());
		sb.append(StringPool.AMPERSAND);

		if (commerceOrder.isGuestOrder()) {
			sb.append("guestToken=");

			Key key = contextCompany.getKeyObj();

			sb.append(
				_encryptor.encrypt(
					key, String.valueOf(commerceOrder.getCommerceOrderId())));

			sb.append(StringPool.AMPERSAND);
		}

		sb.append("nextStep=");

		if (Validator.isNotNull(callbackURL)) {
			sb.append(callbackURL);
		}
		else {
			sb.append(
				URLCodec.encodeURL(
					_getOrderConfirmationCheckoutStepURL(commerceOrder)));
		}

		return sb.toString();
	}

	private String _getOrderConfirmationCheckoutStepURL(
			CommerceOrder commerceOrder)
		throws Exception {

		return PortletURLBuilder.create(
			PortletProviderUtil.getPortletURL(
				contextHttpServletRequest,
				CommercePortletKeys.COMMERCE_CHECKOUT,
				PortletProvider.Action.VIEW)
		).setParameter(
			"checkoutStepName",
			() -> {
				CommerceCheckoutStep commerceCheckoutStep =
					_commerceCheckoutStepServicesTracker.
						getCommerceCheckoutStep("order-confirmation");

				return commerceCheckoutStep.getName();
			}
		).setParameter(
			"commerceOrderUuid", commerceOrder.getUuid()
		).buildString();
	}

	private void _initThemeDisplay(CommerceOrder commerceOrder)
		throws Exception {

		ThemeDisplay themeDisplay =
			(ThemeDisplay)contextHttpServletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		if (themeDisplay != null) {
			return;
		}

		ServicePreAction servicePreAction = new ServicePreAction();

		HttpServletResponse httpServletResponse =
			new DummyHttpServletResponse();

		servicePreAction.servicePre(
			contextHttpServletRequest, httpServletResponse, false);

		ThemeServicePreAction themeServicePreAction =
			new ThemeServicePreAction();

		themeServicePreAction.run(
			contextHttpServletRequest, httpServletResponse);

		themeDisplay = (ThemeDisplay)contextHttpServletRequest.getAttribute(
			WebKeys.THEME_DISPLAY);

		CommerceChannel commerceChannel =
			_commerceChannelLocalService.getCommerceChannelByOrderGroupId(
				commerceOrder.getGroupId());

		themeDisplay.setScopeGroupId(commerceChannel.getSiteGroupId());
	}

	private Order _toOrder(CommerceOrder commerceOrder) throws Exception {
		return _orderDTOConverter.toDTO(
			new DefaultDTOConverterContext(
				_dtoConverterRegistry, commerceOrder.getCommerceOrderId(),
				contextAcceptLanguage.getPreferredLocale(), contextUriInfo,
				contextUser));
	}

	private List<Order> _toOrders(List<CommerceOrder> commerceOrders)
		throws Exception {

		List<Order> orders = new ArrayList<>();

		for (CommerceOrder commerceOrder : commerceOrders) {
			orders.add(_toOrder(commerceOrder));
		}

		return orders;
	}

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private CommerceCheckoutStepServicesTracker
		_commerceCheckoutStepServicesTracker;

	@Reference
	private CommerceOrderService _commerceOrderService;

	@Reference
	private DTOConverterRegistry _dtoConverterRegistry;

	@Reference
	private Encryptor _encryptor;

	@Reference
	private OrderDTOConverter _orderDTOConverter;

	@Reference
	private Portal _portal;

}