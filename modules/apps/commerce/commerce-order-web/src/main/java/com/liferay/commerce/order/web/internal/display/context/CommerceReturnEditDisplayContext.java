/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.order.web.internal.display.context;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.commerce.constants.CommerceReturnConstants;
import com.liferay.commerce.currency.util.CommercePriceFormatter;
import com.liferay.commerce.frontend.model.HeaderActionModel;
import com.liferay.commerce.model.CommerceAddress;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.model.CommerceReturn;
import com.liferay.commerce.model.CommerceReturnItem;
import com.liferay.commerce.order.web.internal.display.context.helper.CommerceReturnRequestHelper;
import com.liferay.commerce.service.CommerceOrderItemLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.list.type.model.ListTypeDefinition;
import com.liferay.list.type.model.ListTypeEntry;
import com.liferay.list.type.service.ListTypeDefinitionService;
import com.liferay.list.type.service.ListTypeEntryService;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Region;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.FastDateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.webserver.WebServerServletTokenUtil;

import java.io.Serializable;

import java.math.BigDecimal;

import java.text.DateFormat;
import java.text.Format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.portlet.RenderRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Stefano Motta
 */
public class CommerceReturnEditDisplayContext {

	public CommerceReturnEditDisplayContext(
			AccountEntryLocalService accountEntryLocalService,
			CommerceOrderLocalService commerceOrderLocalService,
			CommerceOrderItemLocalService commerceOrderItemLocalService,
			CommercePriceFormatter commercePriceFormatter, Language language,
			ListTypeDefinitionService listTypeDefinitionService,
			ListTypeEntryService listTypeEntryService,
			ObjectEntryLocalService objectEntryLocalService,
			ObjectRelationshipLocalService objectRelationshipLocalService,
			RenderRequest renderRequest)
		throws PortalException {

		_accountEntryLocalService = accountEntryLocalService;
		_commerceOrderLocalService = commerceOrderLocalService;
		_commerceOrderItemLocalService = commerceOrderItemLocalService;
		_commercePriceFormatter = commercePriceFormatter;
		_language = language;
		_listTypeDefinitionService = listTypeDefinitionService;
		_listTypeEntryService = listTypeEntryService;
		_objectEntryLocalService = objectEntryLocalService;
		_objectRelationshipLocalService = objectRelationshipLocalService;

		_commerceReturnRequestHelper = new CommerceReturnRequestHelper(
			renderRequest);

		ThemeDisplay themeDisplay =
			_commerceReturnRequestHelper.getThemeDisplay();

		_commerceDateTimeFormat = FastDateFormatFactoryUtil.getDateTime(
			DateFormat.SHORT, DateFormat.SHORT, themeDisplay.getLocale(),
			themeDisplay.getTimeZone());

		long commerceReturnId = getCommerceReturnId();

		if (commerceReturnId > 0) {
			_commerceReturn = new CommerceReturn(
				objectEntryLocalService.getObjectEntry(commerceReturnId));
		}
		else {
			_commerceReturn = null;
		}
	}

	public String getAmountFormatted(BigDecimal amount) throws PortalException {
		CommerceOrder commerceOrder = getCommerceReturnCommerceOrder();

		return _commercePriceFormatter.format(
			commerceOrder.getCommerceCurrency(), amount,
			_commerceReturnRequestHelper.getLocale());
	}

	public CommerceReturn getCommerceReturn() {
		return _commerceReturn;
	}

	public AccountEntry getCommerceReturnAccountEntry() throws PortalException {
		if (_commerceReturn == null) {
			return null;
		}

		if (_accountEntry != null) {
			return _accountEntry;
		}

		_accountEntry = _accountEntryLocalService.getAccountEntry(
			_commerceReturn.getAccountId());

		return _accountEntry;
	}

	public String getCommerceReturnAccountEntryThumbnailURL()
		throws PortalException {

		if (_commerceReturn == null) {
			return StringPool.BLANK;
		}

		AccountEntry accountEntry = getCommerceReturnAccountEntry();

		ThemeDisplay themeDisplay =
			_commerceReturnRequestHelper.getThemeDisplay();

		StringBundler sb = new StringBundler(5);

		sb.append(themeDisplay.getPathImage());
		sb.append("/organization_logo?img_id=");
		sb.append(accountEntry.getLogoId());

		if (accountEntry.getLogoId() > 0) {
			sb.append("&t=");
			sb.append(
				WebServerServletTokenUtil.getToken(accountEntry.getLogoId()));
		}

		return sb.toString();
	}

	public CommerceOrder getCommerceReturnCommerceOrder()
		throws PortalException {

		if (_commerceReturn == null) {
			return null;
		}

		if (_commerceOrder != null) {
			return _commerceOrder;
		}

		_commerceOrder = _commerceOrderLocalService.getCommerceOrder(
			_commerceReturn.getOrderId());

		return _commerceOrder;
	}

	public long getCommerceReturnId() {
		if (_commerceReturnId > 0) {
			return _commerceReturnId;
		}

		_commerceReturnId = ParamUtil.getLong(
			_commerceReturnRequestHelper.getRequest(), "commerceReturnId");

		return _commerceReturnId;
	}

	public CommerceReturnItem getCommerceReturnItem() {
		if (_commerceReturnItem != null) {
			return _commerceReturnItem;
		}

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			getCommerceReturnItemId());

		if (objectEntry == null) {
			return _commerceReturnItem;
		}

		_commerceReturnItem = new CommerceReturnItem(objectEntry);

		return _commerceReturnItem;
	}

	public CommerceOrderItem getCommerceReturnItemCommerceOrderItem()
		throws PortalException {

		if (_commerceReturnItem == null) {
			return null;
		}

		if (_commerceOrderItem != null) {
			return _commerceOrderItem;
		}

		ObjectEntry objectEntry = _objectEntryLocalService.fetchObjectEntry(
			getCommerceReturnItemId());

		Map<String, Serializable> values = objectEntry.getValues();

		_commerceOrderItem =
			_commerceOrderItemLocalService.getCommerceOrderItem(
				GetterUtil.getLong(
					values.get(
						"r_commerceOrderItemToCommerceReturnItems_" +
							"commerceOrderItemId")));

		return _commerceOrderItem;
	}

	public List<FDSActionDropdownItem>
			getCommerceReturnItemFDSActionDropdownItems()
		throws PortalException {

		CommerceReturn commerceReturn = getCommerceReturn();

		List<ObjectEntry> commerceReturnItems = _getCommerceReturnItems();

		if (StringUtil.equals(
				commerceReturn.getReturnStatus(),
				CommerceReturnConstants.RETURN_STATUS_DRAFT) ||
			_isCreateRefund(
				_toReturnItemStatusMap(commerceReturnItems),
				commerceReturnItems)) {

			return Collections.emptyList();
		}

		HttpServletRequest httpServletRequest =
			_commerceReturnRequestHelper.getRequest();

		return ListUtil.fromArray(
			new FDSActionDropdownItem(
				PortletURLBuilder.create(
					PortletProviderUtil.getPortletURL(
						_commerceReturnRequestHelper.getRequest(),
						CommerceReturn.class.getName(),
						PortletProvider.Action.MANAGE)
				).setMVCRenderCommandName(
					"/commerce_return_item/edit_commerce_return_item"
				).setParameter(
					"commerceReturnItemId", "{id}"
				).setWindowState(
					LiferayWindowState.POP_UP
				).buildString(),
				null, "edit", _language.get(httpServletRequest, "edit"), "get",
				"get", "sidePanel"),
			new FDSActionDropdownItem(
				null, null, "delete",
				_language.get(httpServletRequest, "delete"), "delete", "delete",
				"headless"));
	}

	public long getCommerceReturnItemId() {
		if (_commerceReturnItemId > 0) {
			return _commerceReturnItemId;
		}

		_commerceReturnItemId = ParamUtil.getLong(
			_commerceReturnRequestHelper.getRequest(), "commerceReturnItemId");

		return _commerceReturnItemId;
	}

	public String getDateTimeFormatted(Date date) {
		if (date == null) {
			return StringPool.BLANK;
		}

		return _commerceDateTimeFormat.format(date);
	}

	public String getDescriptiveAddress(CommerceAddress commerceAddress) {
		StringBundler sb = new StringBundler(5);

		sb.append(HtmlUtil.escape(commerceAddress.getCity()));
		sb.append(StringPool.COMMA_AND_SPACE);

		try {
			Region region = commerceAddress.getRegion();

			if (region != null) {
				sb.append(HtmlUtil.escape(region.getName()));
				sb.append(StringPool.COMMA_AND_SPACE);
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException);
			}
		}

		sb.append(HtmlUtil.escape(commerceAddress.getZip()));

		return sb.toString();
	}

	public List<HeaderActionModel> getHeaderActionModels() throws Exception {
		List<HeaderActionModel> headerActionModels = new ArrayList<>();

		CommerceReturn commerceReturn = getCommerceReturn();

		if (StringUtil.equals(
				commerceReturn.getReturnStatus(),
				CommerceReturnConstants.RETURN_STATUS_DRAFT) ||
			StringUtil.equals(
				commerceReturn.getReturnStatus(),
				CommerceReturnConstants.RETURN_STATUS_REJECTED)) {

			return headerActionModels;
		}

		List<ObjectEntry> commerceReturnItems = _getCommerceReturnItems();

		LiferayPortletResponse liferayPortletResponse =
			_commerceReturnRequestHelper.getLiferayPortletResponse();

		Map<String, List<ObjectEntry>> returnItemStatusMap =
			_toReturnItemStatusMap(commerceReturnItems);

		if (StringUtil.equals(
				commerceReturn.getReturnStatus(),
				CommerceReturnConstants.RETURN_STATUS_PENDING) &&
			returnItemStatusMap.containsKey("toBeRejectedItems")) {

			List<ObjectEntry> toBeRejectedItems = returnItemStatusMap.get(
				"toBeRejectedItems");

			if (commerceReturnItems.size() == toBeRejectedItems.size()) {
				headerActionModels.add(
					new HeaderActionModel(
						"btn-primary",
						liferayPortletResponse.getNamespace() + "fm",
						PortletURLBuilder.createActionURL(
							liferayPortletResponse
						).setActionName(
							"/commerce_return/edit_commerce_return"
						).buildString(),
						null, "reject"));

				return headerActionModels;
			}
		}

		boolean createRefund = _isCreateRefund(
			returnItemStatusMap, commerceReturnItems);

		if (createRefund) {
			headerActionModels.add(
				new HeaderActionModel(
					"btn-primary disabled",
					liferayPortletResponse.getNamespace() + "fm",
					PortletURLBuilder.createActionURL(
						liferayPortletResponse
					).setActionName(
						"/commerce_return/edit_commerce_return"
					).buildString(),
					null, "create-refund"));

			return headerActionModels;
		}

		HeaderActionModel headerActionModel = new HeaderActionModel(
			"btn-primary", liferayPortletResponse.getNamespace() + "fm",
			PortletURLBuilder.createActionURL(
				liferayPortletResponse
			).setActionName(
				"/commerce_return/edit_commerce_return"
			).buildString(),
			null, "save-and-continue");

		if (getCommerceReturn().getRequestedItems() <= 0) {
			headerActionModel.setAdditionalClasses("disabled");
		}

		headerActionModels.add(headerActionModel);

		return headerActionModels;
	}

	public String getListTypeEntriesByExternalReferenceCodeURL() {
		return StringBundler.concat(
			"/o/headless-admin-list-type/v1.0/list-type-definitions",
			"/by-external-reference-code/L_COMMERCE_RETURN_RESOLUTION_METHODS",
			"/list-type-entries");
	}

	public String getResolutionMethodName() throws PortalException {
		ListTypeDefinition listTypeDefinition =
			_listTypeDefinitionService.
				fetchListTypeDefinitionByExternalReferenceCode(
					"L_COMMERCE_RETURN_RESOLUTION_METHODS",
					_commerceReturnRequestHelper.getCompanyId());

		if (listTypeDefinition == null) {
			return StringPool.BLANK;
		}

		CommerceReturnItem commerceReturnItem = getCommerceReturnItem();

		if (commerceReturnItem == null) {
			return StringPool.BLANK;
		}

		for (ListTypeEntry listTypeEntry :
				_listTypeEntryService.getListTypeEntries(
					listTypeDefinition.getListTypeDefinitionId(),
					QueryUtil.ALL_POS, QueryUtil.ALL_POS)) {

			if (Objects.equals(
					listTypeEntry.getKey(),
					commerceReturnItem.getReturnResolutionMethod())) {

				return listTypeEntry.getName(
					_commerceReturnRequestHelper.getLocale());
			}
		}

		return StringPool.BLANK;
	}

	private List<ObjectEntry> _getCommerceReturnItems() throws PortalException {
		ObjectEntry commerceReturnObjectEntry =
			_objectEntryLocalService.getObjectEntry(getCommerceReturnId());

		ObjectRelationship commerceReturnToCommerceReturnItems =
			_objectRelationshipLocalService.getObjectRelationship(
				commerceReturnObjectEntry.getObjectDefinitionId(),
				"commerceReturnToCommerceReturnItems");

		return _objectEntryLocalService.getOneToManyObjectEntries(
			commerceReturnObjectEntry.getGroupId(),
			commerceReturnToCommerceReturnItems.getObjectRelationshipId(),
			commerceReturnObjectEntry.getObjectEntryId(), true, null,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS);
	}

	private boolean _isCreateRefund(
		Map<String, List<ObjectEntry>> returnItemStatusMap,
		List<ObjectEntry> commerceReturnItems) {

		CommerceReturn commerceReturn = getCommerceReturn();

		boolean createRefund = false;

		if (StringUtil.equals(
				commerceReturn.getReturnStatus(),
				CommerceReturnConstants.RETURN_STATUS_PROCESSING)) {

			List<ObjectEntry> toBeProcessedItems = returnItemStatusMap.get(
				"toBeProcessedItems");

			if (ListUtil.isNotEmpty(toBeProcessedItems)) {
				List<ObjectEntry> tobeRefundItems = returnItemStatusMap.get(
					"tobeRefundItems");

				if ((commerceReturnItems.size() == toBeProcessedItems.size()) ||
					(ListUtil.isNotEmpty(tobeRefundItems) &&
					 (commerceReturnItems.size() == tobeRefundItems.size()))) {

					createRefund = true;
				}
			}
		}

		return createRefund;
	}

	private Map<String, List<ObjectEntry>> _toReturnItemStatusMap(
		List<ObjectEntry> commerceReturnItems) {

		Map<String, List<ObjectEntry>> commerceReturnItemMap = new HashMap<>();

		for (ObjectEntry commerceReturnItem : commerceReturnItems) {
			Map<String, Serializable> commerceReturnItemValues =
				commerceReturnItem.getValues();

			String returnItemStatus = GetterUtil.getString(
				commerceReturnItemValues.get("returnItemStatus"));

			if (StringUtil.equals(
					returnItemStatus,
					CommerceReturnConstants.
						RETURN_STATUS_ITEM_NOT_AUTHORIZED)) {

				List<ObjectEntry> toBeRejectedItems = commerceReturnItemMap.get(
					"toBeRejectedItems");

				if (ListUtil.isEmpty(toBeRejectedItems)) {
					toBeRejectedItems = new ArrayList<>();
				}

				toBeRejectedItems.add(commerceReturnItem);

				commerceReturnItemMap.put(
					"toBeRejectedItems", toBeRejectedItems);
			}

			if (StringUtil.equals(
					returnItemStatus,
					CommerceReturnConstants.
						RETURN_STATUS_ITEM_TO_BE_PROCESSED) ||
				StringUtil.equals(
					returnItemStatus,
					CommerceReturnConstants.RETURN_STATUS_ITEM_NOT_ACCEPTED) ||
				StringUtil.equals(
					returnItemStatus,
					CommerceReturnConstants.
						RETURN_STATUS_ITEM_NOT_AUTHORIZED)) {

				List<ObjectEntry> tobeRefundItems = commerceReturnItemMap.get(
					"tobeRefundItems");

				if (ListUtil.isEmpty(tobeRefundItems)) {
					tobeRefundItems = new ArrayList<>();
				}

				tobeRefundItems.add(commerceReturnItem);

				commerceReturnItemMap.put("tobeRefundItems", tobeRefundItems);
			}

			if (StringUtil.equals(
					returnItemStatus,
					CommerceReturnConstants.
						RETURN_STATUS_ITEM_TO_BE_PROCESSED)) {

				List<ObjectEntry> toBeProcessedItems =
					commerceReturnItemMap.get("toBeProcessedItems");

				if (ListUtil.isEmpty(toBeProcessedItems)) {
					toBeProcessedItems = new ArrayList<>();
				}

				toBeProcessedItems.add(commerceReturnItem);

				commerceReturnItemMap.put(
					"toBeProcessedItems", toBeProcessedItems);
			}
		}

		return commerceReturnItemMap;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceReturnEditDisplayContext.class);

	private AccountEntry _accountEntry;
	private final AccountEntryLocalService _accountEntryLocalService;
	private final Format _commerceDateTimeFormat;
	private CommerceOrder _commerceOrder;
	private CommerceOrderItem _commerceOrderItem;
	private final CommerceOrderItemLocalService _commerceOrderItemLocalService;
	private final CommerceOrderLocalService _commerceOrderLocalService;
	private final CommercePriceFormatter _commercePriceFormatter;
	private final CommerceReturn _commerceReturn;
	private long _commerceReturnId;
	private CommerceReturnItem _commerceReturnItem;
	private long _commerceReturnItemId;
	private final CommerceReturnRequestHelper _commerceReturnRequestHelper;
	private final Language _language;
	private final ListTypeDefinitionService _listTypeDefinitionService;
	private final ListTypeEntryService _listTypeEntryService;
	private final ObjectEntryLocalService _objectEntryLocalService;
	private final ObjectRelationshipLocalService
		_objectRelationshipLocalService;

}