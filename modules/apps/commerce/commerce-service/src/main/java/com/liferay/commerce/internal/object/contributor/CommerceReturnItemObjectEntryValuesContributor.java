/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.object.contributor;

import com.liferay.commerce.constants.CommerceReturnConstants;
import com.liferay.commerce.currency.model.CommerceMoney;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.model.CommerceOrderItem;
import com.liferay.commerce.price.CommerceOrderItemPrice;
import com.liferay.commerce.price.CommerceOrderPriceCalculation;
import com.liferay.commerce.service.CommerceOrderItemLocalService;
import com.liferay.object.entry.ObjectEntryContext;
import com.liferay.object.entry.contributor.ObjectEntryValuesContributor;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.BigDecimalUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.math.BigDecimal;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Crescenzo Rega
 */
@Component(service = ObjectEntryValuesContributor.class)
public class CommerceReturnItemObjectEntryValuesContributor
	implements ObjectEntryValuesContributor {

	@Override
	public void contribute(ObjectEntryContext objectEntryContext) {
		Map<String, Serializable> values = objectEntryContext.getValues();

		try {
			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.getObjectDefinition(
					objectEntryContext.getObjectDefinitionId());

			if (!StringUtil.equals(
					objectDefinition.getName(), "CommerceReturnItem")) {

				return;
			}

			_calculateAmount(values);

			if (Boolean.parseBoolean(
					String.valueOf(
						values.get("skipCommerceReturnItemContributor")))) {

				return;
			}

			ObjectEntry commerceReturnObjectEntry =
				_objectEntryLocalService.getObjectEntry(
					GetterUtil.getLong(
						values.get(
							"r_commerceReturnToCommerceReturnItems_c_" +
								"commerceReturnId")));

			Map<String, Serializable> commerceReturnValues =
				commerceReturnObjectEntry.getValues();

			String returnStatus = GetterUtil.getString(
				commerceReturnValues.get("returnStatus"));

			if (StringUtil.equals(
					returnStatus,
					CommerceReturnConstants.RETURN_STATUS_DRAFT)) {

				return;
			}

			long accepted = GetterUtil.getLong(values.get("accepted"));
			long authorized = GetterUtil.getLong(values.get("authorized"));
			boolean authorizeReturnWithoutReturningProducts = Boolean.valueOf(
				String.valueOf(
					values.get("authorizeReturnWithoutReturningProducts")));

			long quantity = GetterUtil.getLong(values.get("quantity"));

			if (authorized == 0) {
				values.put(
					"returnItemStatus",
					CommerceReturnConstants.RETURN_STATUS_ITEM_NOT_AUTHORIZED);
			}
			else if (StringUtil.equals(
						returnStatus,
						CommerceReturnConstants.RETURN_STATUS_PENDING)) {

				if (authorized < quantity) {
					values.put(
						"returnItemStatus",
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_PARTIALLY_AUTHORIZED);
				}
				else {
					values.put(
						"returnItemStatus",
						CommerceReturnConstants.RETURN_STATUS_ITEM_AUTHORIZED);
				}
			}
			else if (StringUtil.equals(
						returnStatus,
						CommerceReturnConstants.RETURN_STATUS_AUTHORIZED)) {

				if (((authorized <= quantity) &&
					 authorizeReturnWithoutReturningProducts) ||
					(accepted == authorized)) {

					values.put(
						"returnItemStatus",
						CommerceReturnConstants.RETURN_STATUS_ITEM_ACCEPTED);
				}
				else if (accepted == 0) {
					values.put(
						"returnItemStatus",
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_NOT_ACCEPTED);
				}
				else {
					values.put(
						"returnItemStatus",
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_PARTIALLY_ACCEPTED);
				}
			}
			else if (StringUtil.equals(
						returnStatus,
						CommerceReturnConstants.RETURN_STATUS_PROCESSING)) {

				if ((authorized <= quantity) &&
					(authorizeReturnWithoutReturningProducts ||
					 (accepted > 0))) {

					if (Validator.isNotNull(
							GetterUtil.getString(
								values.get("returnResolutionMethod")))) {

						values.put(
							"returnItemStatus",
							CommerceReturnConstants.RETURN_STATUS_ITEM_DEFINED);
					}
					else {
						values.put(
							"returnItemStatus",
							CommerceReturnConstants.
								RETURN_STATUS_ITEM_TO_BE_DEFINED);
					}
				}
				else {
					values.put(
						"returnItemStatus",
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_NOT_ACCEPTED);
				}
			}
		}
		catch (Exception exception) {
			_log.error(exception);
		}
	}

	private void _calculateAmount(Map<String, Serializable> values)
		throws PortalException {

		CommerceOrderItem commerceOrderItem =
			_commerceOrderItemService.fetchCommerceOrderItem(
				GetterUtil.getLong(
					values.get(
						"r_commerceOrderItemToCommerceReturnItems_" +
							"commerceOrderItemId")));

		if (commerceOrderItem != null) {
			CommerceOrder commerceOrder = commerceOrderItem.getCommerceOrder();

			CommerceOrderItemPrice commerceOrderItemPricePerUnit =
				_commerceOrderPriceCalculation.getCommerceOrderItemPricePerUnit(
					commerceOrder.getCommerceCurrency(), commerceOrderItem);

			CommerceMoney finalPrice =
				commerceOrderItemPricePerUnit.getFinalPrice();

			values.put(
				"amount",
				BigDecimalUtil.multiply(
					new BigDecimal(String.valueOf(values.get("quantity"))),
					finalPrice.getPrice()));
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceReturnItemObjectEntryValuesContributor.class);

	@Reference
	private CommerceOrderItemLocalService _commerceOrderItemService;

	@Reference
	private CommerceOrderPriceCalculation _commerceOrderPriceCalculation;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

}