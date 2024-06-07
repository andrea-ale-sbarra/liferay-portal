/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.internal.object.contributor;

import com.liferay.commerce.constants.CommerceReturnConstants;
import com.liferay.commerce.model.CommerceOrder;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.service.CommerceChannelLocalService;
import com.liferay.commerce.service.CommerceOrderLocalService;
import com.liferay.object.entry.ObjectEntryContext;
import com.liferay.object.entry.contributor.ObjectEntryValuesContributor;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Crescenzo Rega
 */
@Component(service = ObjectEntryValuesContributor.class)
public class CommerceReturnObjectEntryValuesContributor
	implements ObjectEntryValuesContributor {

	@Override
	public void contribute(ObjectEntryContext objectEntryContext) {
		Map<String, Serializable> values = objectEntryContext.getValues();

		try {
			ObjectDefinition objectDefinition =
				_objectDefinitionLocalService.getObjectDefinition(
					objectEntryContext.getObjectDefinitionId());

			if (StringUtil.equals(
					objectDefinition.getName(), "CommerceReturn")) {

				return;
			}

			CommerceOrder commerceOrder =
				_commerceOrderLocalService.getCommerceOrder(
					GetterUtil.getLong(
						values.get(
							"r_commerceOrderToCommerceReturns_" +
								"commerceOrderId")));

			CommerceChannel commerceChannel =
				_commerceChannelLocalService.getCommerceChannelByOrderGroupId(
					commerceOrder.getGroupId());

			values.put("channelGroupId", commerceOrder.getGroupId());

			values.put("channelId", commerceChannel.getCommerceChannelId());
			values.put("channelName", commerceChannel.getName());

			ObjectEntry commerceReturn =
				_objectEntryLocalService.fetchObjectEntry(
					GetterUtil.getLong(values.get("c_commerceReturnId")));

			if (commerceReturn == null) {
				commerceReturn = _objectEntryLocalService.fetchObjectEntry(
					GetterUtil.getString(values.get("externalReferenceCode")),
					objectDefinition.getObjectDefinitionId());
			}

			ObjectRelationship commerceReturnToCommerceReturnItems =
				_objectRelationshipLocalService.getObjectRelationship(
					commerceReturn.getObjectDefinitionId(),
					"commerceReturnToCommerceReturnItems");

			List<ObjectEntry> commerceReturnItems =
				_objectEntryLocalService.getOneToManyObjectEntries(
					commerceReturn.getGroupId(),
					commerceReturnToCommerceReturnItems.
						getObjectRelationshipId(),
					commerceReturn.getObjectEntryId(), true, null,
					QueryUtil.ALL_POS, QueryUtil.ALL_POS);

			Map<String, List<ObjectEntry>> returnItemStatusMap =
				_toReturnItemStatusMap(commerceReturnItems);

			String nextReturnStatus = _calculateNextReturnStatus(
				commerceReturn, returnItemStatusMap);

			if (StringUtil.equals(
					nextReturnStatus,
					CommerceReturnConstants.RETURN_STATUS_AUTHORIZED)) {

				for (ObjectEntry commerceReturnItem : commerceReturnItems) {
					String nextReturnItemStatus = null;

					Map<String, Serializable> commerceReturnItemValues =
						commerceReturnItem.getValues();

					String returnItemStatus = GetterUtil.getString(
						commerceReturnItemValues.get("returnItemStatus"));

					if (StringUtil.equals(
							returnItemStatus,
							CommerceReturnConstants.
								RETURN_STATUS_ITEM_AUTHORIZED) ||
						StringUtil.equals(
							returnItemStatus,
							CommerceReturnConstants.
								RETURN_STATUS_ITEM_PARTIALLY_AUTHORIZED) ||
						StringUtil.equals(
							returnItemStatus,
							CommerceReturnConstants.
								RETURN_STATUS_ITEM_TO_BE_ACCEPTED)) {

						nextReturnItemStatus =
							CommerceReturnConstants.
								RETURN_STATUS_ITEM_TO_BE_ACCEPTED;
					}
					else if (StringUtil.equals(
								returnItemStatus,
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_NOT_ACCEPTED)) {

						nextReturnItemStatus =
							CommerceReturnConstants.
								RETURN_STATUS_ITEM_NOT_ACCEPTED;
					}
					else {
						nextReturnItemStatus =
							CommerceReturnConstants.
								RETURN_STATUS_ITEM_NOT_AUTHORIZED;
					}

					if (Validator.isNotNull(nextReturnItemStatus)) {
						commerceReturnItemValues.put(
							"returnItemStatus", nextReturnItemStatus);
						commerceReturnItemValues.put(
							"skipCommerceReturnItemContributor", true);

						_objectEntryLocalService.updateObjectEntry(
							commerceReturnItem.getUserId(),
							commerceReturnItem.getObjectEntryId(),
							commerceReturnItemValues, new ServiceContext());
					}
				}
			}
			else if (StringUtil.equals(
						nextReturnStatus,
						CommerceReturnConstants.RETURN_STATUS_PROCESSING)) {

				int size = 0;

				if (returnItemStatusMap.containsKey("definedReturnItems")) {
					List<ObjectEntry> definedReturnItems =
						returnItemStatusMap.get("definedReturnItems");

					size += definedReturnItems.size();
				}

				if (returnItemStatusMap.containsKey("notAcceptedReturnItems")) {
					List<ObjectEntry> notAcceptedReturnItems =
						returnItemStatusMap.get("notAcceptedReturnItems");

					size += notAcceptedReturnItems.size();
				}

				if (returnItemStatusMap.containsKey("notAuthorizedItems")) {
					List<ObjectEntry> notAuthorizedItems =
						returnItemStatusMap.get("notAuthorizedItems");

					size += notAuthorizedItems.size();
				}

				if (commerceReturnItems.size() == size) {
					for (ObjectEntry commerceReturnItem : commerceReturnItems) {
						Map<String, Serializable> commerceReturnItemValues =
							commerceReturnItem.getValues();

						String returnItemStatus = GetterUtil.getString(
							commerceReturnItemValues.get("returnItemStatus"));

						if (StringUtil.equals(
								returnItemStatus,
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_DEFINED)) {

							commerceReturnItemValues.put(
								"returnItemStatus",
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_TO_BE_PROCESSED);
							commerceReturnItemValues.put(
								"skipCommerceReturnItemContributor", true);

							_objectEntryLocalService.updateObjectEntry(
								commerceReturnItem.getUserId(),
								commerceReturnItem.getObjectEntryId(),
								commerceReturnItemValues, new ServiceContext());
						}
					}
				}
				else {
					for (ObjectEntry commerceReturnItem : commerceReturnItems) {
						String nextReturnItemStatus = null;

						Map<String, Serializable> commerceReturnItemValues =
							commerceReturnItem.getValues();

						String returnItemStatus = GetterUtil.getString(
							commerceReturnItemValues.get("returnItemStatus"));

						if (StringUtil.equals(
								returnItemStatus,
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_ACCEPTED) ||
							StringUtil.equals(
								returnItemStatus,
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_AUTHORIZED) ||
							StringUtil.equals(
								returnItemStatus,
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_PARTIALLY_ACCEPTED) ||
							StringUtil.equals(
								returnItemStatus,
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_PARTIALLY_AUTHORIZED)) {

							nextReturnItemStatus =
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_TO_BE_DEFINED;
						}
						else if (StringUtil.equals(
									returnItemStatus,
									CommerceReturnConstants.
										RETURN_STATUS_ITEM_TO_BE_ACCEPTED)) {

							nextReturnItemStatus =
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_NOT_ACCEPTED;
						}
						else if (StringUtil.equals(
									returnItemStatus,
									CommerceReturnConstants.
										RETURN_STATUS_ITEM_TO_BE_AUTHORIZED)) {

							nextReturnItemStatus =
								CommerceReturnConstants.
									RETURN_STATUS_ITEM_NOT_AUTHORIZED;
						}

						if (Validator.isNotNull(nextReturnItemStatus)) {
							commerceReturnItemValues.put(
								"returnItemStatus", nextReturnItemStatus);
							commerceReturnItemValues.put(
								"skipCommerceReturnItemContributor", true);

							_objectEntryLocalService.updateObjectEntry(
								commerceReturnItem.getUserId(),
								commerceReturnItem.getObjectEntryId(),
								commerceReturnItemValues, new ServiceContext());
						}
					}
				}
			}

			values.put("returnStatus", nextReturnStatus);
		}
		catch (Exception exception) {
			_log.error(exception);
		}
	}

	private String _calculateNextReturnStatus(
		ObjectEntry commerceReturn,
		Map<String, List<ObjectEntry>> returnItemStatusMap) {

		List<ObjectEntry> authorizedReturnItems = returnItemStatusMap.get(
			"authorizedReturnItems");

		String nextReturnStatus = CommerceReturnConstants.RETURN_STATUS_PENDING;

		if (returnItemStatusMap.containsKey("acceptedReturnItems") ||
			returnItemStatusMap.containsKey("definedReturnItems") ||
			returnItemStatusMap.containsKey("toBeDefinedReturnItems")) {

			nextReturnStatus = CommerceReturnConstants.RETURN_STATUS_PROCESSING;
		}
		else if (ListUtil.isNotEmpty(authorizedReturnItems)) {
			List<ObjectEntry> authorizeReturnWithoutReturningProducts =
				ListUtil.filter(
					authorizedReturnItems,
					authorizedReturnItem -> {
						Map<String, Serializable> authorizedReturnItemValues =
							authorizedReturnItem.getValues();

						return Boolean.parseBoolean(
							String.valueOf(
								authorizedReturnItemValues.get(
									"authorizeReturnWithoutReturning" +
										"Products")));
					});

			if (authorizedReturnItems.size() ==
					authorizeReturnWithoutReturningProducts.size()) {

				nextReturnStatus =
					CommerceReturnConstants.RETURN_STATUS_PROCESSING;
			}
			else {
				nextReturnStatus =
					CommerceReturnConstants.RETURN_STATUS_AUTHORIZED;
			}
		}
		else if (returnItemStatusMap.containsKey("notAcceptedReturnItems") ||
				 returnItemStatusMap.containsKey("toBeAcceptedReturnItems")) {

			nextReturnStatus = CommerceReturnConstants.RETURN_STATUS_AUTHORIZED;
		}
		else if (returnItemStatusMap.containsKey("toBeAuthorizedReturnItems")) {
			nextReturnStatus = CommerceReturnConstants.RETURN_STATUS_PENDING;
		}
		else {
			Map<String, Serializable> commerceReturnValues =
				commerceReturn.getValues();

			if (StringUtil.equals(
					GetterUtil.getString(
						commerceReturnValues.get("returnStatus")),
					CommerceReturnConstants.RETURN_STATUS_PENDING)) {

				nextReturnStatus =
					CommerceReturnConstants.RETURN_STATUS_REJECTED;
			}
		}

		return nextReturnStatus;
	}

	private Map<String, List<ObjectEntry>> _toReturnItemStatusMap(
		List<ObjectEntry> commerceReturnItems) {

		Map<String, List<ObjectEntry>> commerceReturnItemMap = new HashMap<>();

		for (ObjectEntry commerceReturnItem : commerceReturnItems) {
			Map<String, Serializable> commerceReturnItemValues =
				commerceReturnItem.getValues();

			String returnItemStatus = GetterUtil.getString(
				commerceReturnItemValues.get("returnItemStatus"));

			String key = null;

			if (StringUtil.equals(
					returnItemStatus,
					CommerceReturnConstants.RETURN_STATUS_ITEM_ACCEPTED) ||
				StringUtil.equals(
					returnItemStatus,
					CommerceReturnConstants.
						RETURN_STATUS_ITEM_PARTIALLY_ACCEPTED)) {

				key = "acceptedReturnItems";
			}
			else if (StringUtil.equals(
						returnItemStatus,
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_AUTHORIZED) ||
					 StringUtil.equals(
						 returnItemStatus,
						 CommerceReturnConstants.
							 RETURN_STATUS_ITEM_PARTIALLY_AUTHORIZED)) {

				key = "authorizedReturnItems";
			}
			else if (StringUtil.equals(
						returnItemStatus,
						CommerceReturnConstants.RETURN_STATUS_ITEM_DEFINED)) {

				key = "definedReturnItems";
			}
			else if (StringUtil.equals(
						returnItemStatus,
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_NOT_ACCEPTED)) {

				key = "notAcceptedReturnItems";
			}
			else if (StringUtil.equals(
						returnItemStatus,
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_NOT_AUTHORIZED)) {

				key = "notAuthorizedItems";
			}
			else if (StringUtil.equals(
						returnItemStatus,
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_TO_BE_AUTHORIZED)) {

				key = "toBeAuthorizedReturnItems";
			}
			else if (StringUtil.equals(
						returnItemStatus,
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_TO_BE_ACCEPTED)) {

				key = "toBeAcceptedReturnItems";
			}
			else if (StringUtil.equals(
						returnItemStatus,
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_TO_BE_DEFINED)) {

				key = "toBeDefinedReturnItems";
			}
			else if (StringUtil.equals(
						returnItemStatus,
						CommerceReturnConstants.
							RETURN_STATUS_ITEM_TO_BE_PROCESSED)) {

				key = "toBeProcessedReturnItems";
			}

			if (Validator.isNotNull(key)) {
				List<ObjectEntry> items = commerceReturnItemMap.get(key);

				if (ListUtil.isEmpty(items)) {
					items = new ArrayList<>();
				}

				items.add(commerceReturnItem);

				commerceReturnItemMap.put(key, items);
			}
		}

		return commerceReturnItemMap;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CommerceReturnObjectEntryValuesContributor.class);

	@Reference
	private CommerceChannelLocalService _commerceChannelLocalService;

	@Reference
	private CommerceOrderLocalService _commerceOrderLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;

}