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

package com.liferay.commerce.inventory.service.impl;

import com.liferay.commerce.inventory.constants.CommerceInventoryConstants;
import com.liferay.commerce.inventory.exception.CommerceInventoryWarehouseItemSkuException;
import com.liferay.commerce.inventory.exception.DuplicateCommerceInventoryWarehouseItemException;
import com.liferay.commerce.inventory.exception.MVCCException;
import com.liferay.commerce.inventory.exception.NoSuchInventoryWarehouseItemException;
import com.liferay.commerce.inventory.model.CIWarehouseItem;
import com.liferay.commerce.inventory.model.CommerceInventoryBookedQuantityTable;
import com.liferay.commerce.inventory.model.CommerceInventoryReplenishmentItemTable;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouse;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouseItem;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouseItemTable;
import com.liferay.commerce.inventory.model.CommerceInventoryWarehouseTable;
import com.liferay.commerce.inventory.service.CommerceInventoryAuditLocalService;
import com.liferay.commerce.inventory.service.base.CommerceInventoryWarehouseItemLocalServiceBaseImpl;
import com.liferay.commerce.inventory.type.CommerceInventoryAuditType;
import com.liferay.commerce.inventory.type.CommerceInventoryAuditTypeRegistry;
import com.liferay.commerce.inventory.type.constants.CommerceInventoryAuditTypeConstants;
import com.liferay.commerce.product.model.CommerceChannel;
import com.liferay.commerce.product.model.CommerceChannelRelTable;
import com.liferay.commerce.product.service.CPInstanceLocalService;
import com.liferay.petra.sql.dsl.DSLFunctionFactoryUtil;
import com.liferay.petra.sql.dsl.DSLQueryFactoryUtil;
import com.liferay.petra.sql.dsl.expression.Predicate;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.GroupTable;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Luca Pellizzon
 * @author Alessio Antonio Rendina
 */
@Component(
	property = "model.class.name=com.liferay.commerce.inventory.model.CommerceInventoryWarehouseItem",
	service = AopService.class
)
public class CommerceInventoryWarehouseItemLocalServiceImpl
	extends CommerceInventoryWarehouseItemLocalServiceBaseImpl {

	@Override
	public CommerceInventoryWarehouseItem addCommerceInventoryWarehouseItem(
			String externalReferenceCode, long userId,
			long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey, int quantity)
		throws PortalException {

		User user = _userLocalService.getUser(userId);

		if (Validator.isBlank(externalReferenceCode)) {
			externalReferenceCode = null;
		}

		_validate(commerceInventoryWarehouseId, sku, unitOfMeasureKey);

		long commerceInventoryWarehouseItemId = counterLocalService.increment();

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.create(
				commerceInventoryWarehouseItemId);

		commerceInventoryWarehouseItem.setExternalReferenceCode(
			externalReferenceCode);
		commerceInventoryWarehouseItem.setCompanyId(user.getCompanyId());
		commerceInventoryWarehouseItem.setUserId(user.getUserId());
		commerceInventoryWarehouseItem.setUserName(user.getFullName());
		commerceInventoryWarehouseItem.setCommerceInventoryWarehouseId(
			commerceInventoryWarehouseId);
		commerceInventoryWarehouseItem.setSku(sku);
		commerceInventoryWarehouseItem.setUnitOfMeasureKey(unitOfMeasureKey);
		commerceInventoryWarehouseItem.setQuantity(quantity);

		return commerceInventoryWarehouseItemPersistence.update(
			commerceInventoryWarehouseItem);
	}

	@Override
	public CommerceInventoryWarehouseItem
			addOrUpdateCommerceInventoryWarehouseItem(
				long userId, long commerceInventoryWarehouseId, String sku,
				String unitOfMeasureKey, int quantity)
		throws PortalException {

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.fetchByCIWI_S_U(
				commerceInventoryWarehouseId, sku, unitOfMeasureKey);

		if (commerceInventoryWarehouseItem == null) {
			return commerceInventoryWarehouseItemLocalService.
				addCommerceInventoryWarehouseItem(
					StringPool.BLANK, userId, commerceInventoryWarehouseId, sku,
					unitOfMeasureKey, quantity);
		}

		return commerceInventoryWarehouseItemLocalService.
			updateCommerceInventoryWarehouseItem(
				userId,
				commerceInventoryWarehouseItem.
					getCommerceInventoryWarehouseItemId(),
				quantity, commerceInventoryWarehouseItem.getMvccVersion());
	}

	@Override
	public CommerceInventoryWarehouseItem
			addOrUpdateCommerceInventoryWarehouseItem(
				String externalReferenceCode, long companyId, long userId,
				long commerceInventoryWarehouseId, String sku,
				String unitOfMeasureKey, int quantity)
		throws PortalException {

		if (Validator.isBlank(externalReferenceCode)) {
			externalReferenceCode = null;
		}
		else {
			CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
				commerceInventoryWarehouseItemPersistence.fetchByERC_C(
					externalReferenceCode, companyId);

			if (commerceInventoryWarehouseItem != null) {
				return commerceInventoryWarehouseItemLocalService.
					updateCommerceInventoryWarehouseItem(
						userId,
						commerceInventoryWarehouseItem.
							getCommerceInventoryWarehouseItemId(),
						quantity,
						commerceInventoryWarehouseItem.getMvccVersion());
			}
		}

		return commerceInventoryWarehouseItemLocalService.
			addCommerceInventoryWarehouseItem(
				externalReferenceCode, userId, commerceInventoryWarehouseId,
				sku, unitOfMeasureKey, quantity);
	}

	@Override
	public int countItemsByCompanyId(long companyId, String sku) {
		return dslQueryCount(
			DSLQueryFactoryUtil.countDistinct(
				CommerceInventoryWarehouseItemTable.INSTANCE.sku
			).from(
				CommerceInventoryWarehouseItemTable.INSTANCE
			).where(
				CommerceInventoryWarehouseItemTable.INSTANCE.companyId.eq(
					companyId
				).and(
					() -> {
						if (Validator.isNull(sku)) {
							return null;
						}

						return DSLFunctionFactoryUtil.lower(
							CommerceInventoryWarehouseItemTable.INSTANCE.sku
						).like(
							StringPool.PERCENT + StringUtil.toLowerCase(sku) +
								StringPool.PERCENT
						);
					}
				)
			));
	}

	@Override
	public void deleteCommerceInventoryWarehouseItems(
		long commerceInventoryWarehouseId) {

		commerceInventoryWarehouseItemPersistence.
			removeByCommerceInventoryWarehouseId(commerceInventoryWarehouseId);
	}

	@Override
	public void deleteCommerceInventoryWarehouseItems(
		long companyId, String sku, String unitOfMeasureKey) {

		commerceInventoryWarehouseItemPersistence.removeByC_S_U(
			companyId, sku, unitOfMeasureKey);
	}

	@Override
	public void deleteCommerceInventoryWarehouseItemsByCompanyId(
		long companyId) {

		commerceInventoryWarehouseItemPersistence.removeByCompanyId(companyId);
	}

	@Override
	public CommerceInventoryWarehouseItem fetchCommerceInventoryWarehouseItem(
			long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey)
		throws PortalException {

		return commerceInventoryWarehouseItemPersistence.fetchByCIWI_S_U(
			commerceInventoryWarehouseId, sku, unitOfMeasureKey);
	}

	@Override
	public CommerceInventoryWarehouseItem getCommerceInventoryWarehouseItem(
			long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey)
		throws PortalException {

		return commerceInventoryWarehouseItemPersistence.findByCIWI_S_U(
			commerceInventoryWarehouseId, sku, unitOfMeasureKey);
	}

	@Override
	public CommerceInventoryWarehouseItem
			getCommerceInventoryWarehouseItemByReferenceCode(
				String externalReferenceCode, long companyId)
		throws PortalException {

		if (Validator.isBlank(externalReferenceCode)) {
			throw new NoSuchInventoryWarehouseItemException();
		}

		return commerceInventoryWarehouseItemPersistence.findByERC_C(
			externalReferenceCode, companyId);
	}

	@Override
	public List<CommerceInventoryWarehouseItem>
		getCommerceInventoryWarehouseItems(
			long commerceInventoryWarehouseId, int start, int end) {

		return commerceInventoryWarehouseItemPersistence.
			findByCommerceInventoryWarehouseId(
				commerceInventoryWarehouseId, start, end);
	}

	@Override
	public List<CommerceInventoryWarehouseItem>
		getCommerceInventoryWarehouseItemsByCompanyId(
			long companyId, int start, int end) {

		return commerceInventoryWarehouseItemPersistence.findByCompanyId(
			companyId, start, end);
	}

	@Override
	public List<CommerceInventoryWarehouseItem>
		getCommerceInventoryWarehouseItemsByCompanyIdAndSku(
			long companyId, String sku, String unitOfMeasureKey, int start,
			int end) {

		return commerceInventoryWarehouseItemPersistence.findByC_S_U(
			companyId, sku, unitOfMeasureKey, start, end);
	}

	@Override
	public List<CommerceInventoryWarehouseItem>
		getCommerceInventoryWarehouseItemsByModifiedDate(
			long companyId, Date startDate, Date endDate, int start, int end) {

		return dslQuery(
			DSLQueryFactoryUtil.select(
				CommerceInventoryWarehouseItemTable.INSTANCE
			).from(
				CommerceInventoryWarehouseItemTable.INSTANCE
			).where(
				CommerceInventoryWarehouseItemTable.INSTANCE.companyId.eq(
					companyId
				).and(
					CommerceInventoryWarehouseItemTable.INSTANCE.modifiedDate.
						gte(startDate)
				).and(
					CommerceInventoryWarehouseItemTable.INSTANCE.modifiedDate.
						lt(endDate)
				)
			).orderBy(
				CommerceInventoryWarehouseItemTable.INSTANCE.sku.ascending(),
				CommerceInventoryWarehouseItemTable.INSTANCE.unitOfMeasureKey.
					ascending()
			));
	}

	@Override
	public int getCommerceInventoryWarehouseItemsCount(
		long commerceInventoryWarehouseId) {

		return commerceInventoryWarehouseItemPersistence.
			countByCommerceInventoryWarehouseId(commerceInventoryWarehouseId);
	}

	@Override
	public int getCommerceInventoryWarehouseItemsCount(
		long companyId, long groupId, String sku, String unitOfMeasureKey) {

		return dslQueryCount(
			DSLQueryFactoryUtil.countDistinct(
				CommerceInventoryWarehouseItemTable.INSTANCE.
					commerceInventoryWarehouseItemId
			).from(
				CommerceInventoryWarehouseItemTable.INSTANCE
			).innerJoinON(
				CommerceChannelRelTable.INSTANCE,
				CommerceChannelRelTable.INSTANCE.classNameId.eq(
					_portal.getClassNameId(
						CommerceInventoryWarehouse.class.getName())
				).and(
					CommerceChannelRelTable.INSTANCE.classPK.eq(
						CommerceInventoryWarehouseItemTable.INSTANCE.
							commerceInventoryWarehouseId)
				)
			).innerJoinON(
				GroupTable.INSTANCE,
				GroupTable.INSTANCE.classNameId.eq(
					_portal.getClassNameId(CommerceChannel.class.getName())
				).and(
					GroupTable.INSTANCE.classPK.eq(
						CommerceChannelRelTable.INSTANCE.commerceChannelId)
				)
			).innerJoinON(
				CommerceInventoryWarehouseTable.INSTANCE,
				CommerceInventoryWarehouseTable.INSTANCE.
					commerceInventoryWarehouseId.eq(
						CommerceInventoryWarehouseItemTable.INSTANCE.
							commerceInventoryWarehouseId)
			).where(
				CommerceInventoryWarehouseItemTable.INSTANCE.companyId.eq(
					companyId
				).and(
					CommerceInventoryWarehouseItemTable.INSTANCE.sku.eq(sku)
				).and(
					() -> {
						if (Validator.isNull(unitOfMeasureKey)) {
							return null;
						}

						return CommerceInventoryWarehouseItemTable.INSTANCE.
							unitOfMeasureKey.eq(unitOfMeasureKey);
					}
				).and(
					CommerceInventoryWarehouseTable.INSTANCE.active.eq(true)
				).and(
					GroupTable.INSTANCE.groupId.eq(groupId)
				)
			));
	}

	@Override
	public int getCommerceInventoryWarehouseItemsCount(
		long companyId, String sku, String unitOfMeasureKey) {

		return commerceInventoryWarehouseItemPersistence.countByC_S_U(
			companyId, sku, unitOfMeasureKey);
	}

	@Override
	public int getCommerceInventoryWarehouseItemsCountByCompanyId(
		long companyId) {

		return commerceInventoryWarehouseItemPersistence.countByCompanyId(
			companyId);
	}

	@Override
	public int getCommerceInventoryWarehouseItemsCountByModifiedDate(
		long companyId, Date startDate, Date endDate) {

		return dslQueryCount(
			DSLQueryFactoryUtil.countDistinct(
				CommerceInventoryWarehouseItemTable.INSTANCE.
					commerceInventoryWarehouseItemId
			).from(
				CommerceInventoryWarehouseItemTable.INSTANCE
			).where(
				CommerceInventoryWarehouseItemTable.INSTANCE.companyId.eq(
					companyId
				).and(
					CommerceInventoryWarehouseItemTable.INSTANCE.modifiedDate.
						gte(
							startDate
						).and(
							CommerceInventoryWarehouseItemTable.INSTANCE.
								modifiedDate.lt(endDate)
						)
				)
			));
	}

	@Override
	public List<CIWarehouseItem> getItemsByCompanyId(
		long companyId, String sku, int start, int end) {

		List<Object[]> objects = dslQuery(
			DSLQueryFactoryUtil.select(
				CommerceInventoryWarehouseItemTable.INSTANCE.sku,
				CommerceInventoryWarehouseItemTable.INSTANCE.unitOfMeasureKey.
					as("UNIT_OF_MEASURE_KEY"),
				DSLFunctionFactoryUtil.sum(
					CommerceInventoryWarehouseItemTable.INSTANCE.quantity
				).as(
					"SUM_STOCK"
				),
				DSLFunctionFactoryUtil.sum(
					CommerceInventoryBookedQuantityTable.INSTANCE.quantity
				).as(
					"SUM_BOOKED"
				),
				DSLFunctionFactoryUtil.sum(
					CommerceInventoryReplenishmentItemTable.INSTANCE.quantity
				).as(
					"SUM_AWAITING"
				)
			).from(
				CommerceInventoryWarehouseItemTable.INSTANCE
			).leftJoinOn(
				CommerceInventoryBookedQuantityTable.INSTANCE,
				CommerceInventoryBookedQuantityTable.INSTANCE.sku.eq(
					CommerceInventoryWarehouseItemTable.INSTANCE.sku
				).and(
					Predicate.withParentheses(
						CommerceInventoryBookedQuantityTable.INSTANCE.
							unitOfMeasureKey.eq(
								CommerceInventoryWarehouseItemTable.INSTANCE.
									unitOfMeasureKey
							).or(
								Predicate.withParentheses(
									CommerceInventoryBookedQuantityTable.
										INSTANCE.unitOfMeasureKey.isNull(
										).and(
											CommerceInventoryWarehouseItemTable.
												INSTANCE.unitOfMeasureKey.
													isNull()
										))
							))
				).and(
					CommerceInventoryBookedQuantityTable.INSTANCE.companyId.eq(
						CommerceInventoryWarehouseItemTable.INSTANCE.companyId)
				)
			).leftJoinOn(
				CommerceInventoryReplenishmentItemTable.INSTANCE,
				CommerceInventoryReplenishmentItemTable.INSTANCE.sku.eq(
					CommerceInventoryWarehouseItemTable.INSTANCE.sku
				).and(
					Predicate.withParentheses(
						CommerceInventoryReplenishmentItemTable.INSTANCE.
							unitOfMeasureKey.eq(
								CommerceInventoryWarehouseItemTable.INSTANCE.
									unitOfMeasureKey
							).or(
								Predicate.withParentheses(
									CommerceInventoryReplenishmentItemTable.
										INSTANCE.unitOfMeasureKey.isNull(
										).and(
											CommerceInventoryWarehouseItemTable.
												INSTANCE.unitOfMeasureKey.
													isNull()
										))
							))
				).and(
					CommerceInventoryReplenishmentItemTable.INSTANCE.companyId.
						eq(
							CommerceInventoryWarehouseItemTable.INSTANCE.
								companyId)
				)
			).where(
				CommerceInventoryWarehouseItemTable.INSTANCE.companyId.eq(
					companyId
				).and(
					() -> {
						if (Validator.isNull(sku)) {
							return null;
						}

						return DSLFunctionFactoryUtil.lower(
							CommerceInventoryWarehouseItemTable.INSTANCE.sku
						).like(
							StringPool.PERCENT + StringUtil.toLowerCase(sku) +
								StringPool.PERCENT
						);
					}
				)
			).groupBy(
				CommerceInventoryWarehouseItemTable.INSTANCE.sku,
				CommerceInventoryWarehouseItemTable.INSTANCE.unitOfMeasureKey
			).orderBy(
				CommerceInventoryWarehouseItemTable.INSTANCE.sku.ascending(),
				CommerceInventoryWarehouseItemTable.INSTANCE.unitOfMeasureKey.
					ascending()
			));

		List<CIWarehouseItem> ciWarehouseItems = new ArrayList<>();

		for (Object[] object : objects) {
			if (object != null) {
				String skuCode = "";

				if ((object.length > 0) && (object[0] != null)) {
					skuCode = (String)object[0];
				}

				int stock = 0;

				if ((object.length > 2) && (object[2] != null)) {
					stock = (int)object[2];
				}

				int booked = 0;

				if ((object.length > 3) && (object[3] != null)) {
					booked = (int)object[3];
				}

				int replenishment = 0;

				if ((object.length > 4) && (object[4] != null)) {
					replenishment = (int)object[4];
				}

				ciWarehouseItems.add(
					new CIWarehouseItem(skuCode, stock, booked, replenishment));
			}
		}

		return ciWarehouseItems;
	}

	@Override
	public int getStockQuantity(
		long companyId, long groupId, String sku, String unitOfMeasureKey) {

		Iterable<Integer> iterable = dslQuery(
			DSLQueryFactoryUtil.select(
				DSLFunctionFactoryUtil.sum(
					DSLFunctionFactoryUtil.subtract(
						CommerceInventoryWarehouseItemTable.INSTANCE.quantity,
						CommerceInventoryWarehouseItemTable.INSTANCE.
							reservedQuantity)
				).as(
					"SUM_VALUE"
				)
			).from(
				CommerceInventoryWarehouseItemTable.INSTANCE
			).innerJoinON(
				CommerceChannelRelTable.INSTANCE,
				CommerceChannelRelTable.INSTANCE.classNameId.eq(
					_portal.getClassNameId(
						CommerceInventoryWarehouse.class.getName())
				).and(
					CommerceChannelRelTable.INSTANCE.classPK.eq(
						CommerceInventoryWarehouseItemTable.INSTANCE.
							commerceInventoryWarehouseId)
				)
			).innerJoinON(
				GroupTable.INSTANCE,
				GroupTable.INSTANCE.classNameId.eq(
					_portal.getClassNameId(CommerceChannel.class.getName())
				).and(
					GroupTable.INSTANCE.classPK.eq(
						CommerceChannelRelTable.INSTANCE.commerceChannelId)
				)
			).innerJoinON(
				CommerceInventoryWarehouseTable.INSTANCE,
				CommerceInventoryWarehouseTable.INSTANCE.
					commerceInventoryWarehouseId.eq(
						CommerceInventoryWarehouseItemTable.INSTANCE.
							commerceInventoryWarehouseId)
			).where(
				CommerceInventoryWarehouseItemTable.INSTANCE.companyId.eq(
					companyId
				).and(
					CommerceInventoryWarehouseItemTable.INSTANCE.sku.eq(sku)
				).and(
					() -> {
						if (Validator.isNull(unitOfMeasureKey)) {
							return CommerceInventoryWarehouseItemTable.INSTANCE.
								unitOfMeasureKey.isNull();
						}

						return CommerceInventoryWarehouseItemTable.INSTANCE.
							unitOfMeasureKey.eq(unitOfMeasureKey);
					}
				).and(
					CommerceInventoryWarehouseTable.INSTANCE.active.eq(true)
				).and(
					GroupTable.INSTANCE.groupId.eq(groupId)
				)
			));

		Iterator<Integer> iterator = iterable.iterator();

		Integer stockQuantity = iterator.next();

		if (stockQuantity == null) {
			return 0;
		}

		return stockQuantity;
	}

	@Override
	public Integer getStockQuantity(
		long companyId, String sku, String unitOfMeasureKey) {

		Iterable<Integer> iterable = dslQuery(
			DSLQueryFactoryUtil.select(
				DSLFunctionFactoryUtil.sum(
					DSLFunctionFactoryUtil.subtract(
						CommerceInventoryWarehouseItemTable.INSTANCE.quantity,
						CommerceInventoryWarehouseItemTable.INSTANCE.
							reservedQuantity)
				).as(
					"SUM_VALUE"
				)
			).from(
				CommerceInventoryWarehouseItemTable.INSTANCE
			).innerJoinON(
				CommerceInventoryWarehouseTable.INSTANCE,
				CommerceInventoryWarehouseTable.INSTANCE.
					commerceInventoryWarehouseId.eq(
						CommerceInventoryWarehouseItemTable.INSTANCE.
							commerceInventoryWarehouseId)
			).where(
				CommerceInventoryWarehouseItemTable.INSTANCE.companyId.eq(
					companyId
				).and(
					CommerceInventoryWarehouseItemTable.INSTANCE.sku.eq(sku)
				).and(
					() -> {
						if (Validator.isNull(unitOfMeasureKey)) {
							return CommerceInventoryWarehouseItemTable.INSTANCE.
								unitOfMeasureKey.isNull();
						}

						return CommerceInventoryWarehouseItemTable.INSTANCE.
							unitOfMeasureKey.eq(unitOfMeasureKey);
					}
				).and(
					CommerceInventoryWarehouseTable.INSTANCE.active.eq(true)
				)
			));

		Iterator<Integer> iterator = iterable.iterator();

		Integer stockQuantity = iterator.next();

		if (stockQuantity == null) {
			return 0;
		}

		return stockQuantity;
	}

	@Override
	public CommerceInventoryWarehouseItem
			increaseCommerceInventoryWarehouseItemQuantity(
				long userId, long commerceInventoryWarehouseItemId,
				int quantity)
		throws PortalException {

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.findByPrimaryKey(
				commerceInventoryWarehouseItemId);

		commerceInventoryWarehouseItem.setQuantity(
			quantity + commerceInventoryWarehouseItem.getQuantity());

		commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.update(
				commerceInventoryWarehouseItem);

		CommerceInventoryAuditType commerceInventoryAuditType =
			_commerceInventoryAuditTypeRegistry.getCommerceInventoryAuditType(
				CommerceInventoryConstants.AUDIT_TYPE_INCREASE_QUANTITY);

		_commerceInventoryAuditLocalService.addCommerceInventoryAudit(
			userId, commerceInventoryWarehouseItem.getSku(),
			commerceInventoryAuditType.getType(),
			commerceInventoryAuditType.getLog(null), quantity);

		return commerceInventoryWarehouseItem;
	}

	@Override
	@Transactional(
		propagation = Propagation.REQUIRED, readOnly = false,
		rollbackFor = Exception.class
	)
	public void moveQuantitiesBetweenWarehouses(
			long userId, long fromCommerceInventoryWarehouseId,
			long toCommerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey, int quantity)
		throws PortalException {

		CommerceInventoryWarehouseItem fromWarehouseItem =
			commerceInventoryWarehouseItemPersistence.findByCIWI_S_U(
				fromCommerceInventoryWarehouseId, sku, unitOfMeasureKey);

		if (quantity > fromWarehouseItem.getQuantity()) {
			throw new PortalException("Quantity to transfer unavailable");
		}

		commerceInventoryWarehouseItemLocalService.
			updateCommerceInventoryWarehouseItem(
				userId, fromWarehouseItem.getCommerceInventoryWarehouseItemId(),
				fromWarehouseItem.getQuantity() - quantity,
				fromWarehouseItem.getMvccVersion());

		CommerceInventoryWarehouseItem toWarehouseItem =
			commerceInventoryWarehouseItemPersistence.findByCIWI_S_U(
				toCommerceInventoryWarehouseId, sku, unitOfMeasureKey);

		commerceInventoryWarehouseItemLocalService.
			updateCommerceInventoryWarehouseItem(
				userId, toWarehouseItem.getCommerceInventoryWarehouseItemId(),
				toWarehouseItem.getQuantity() + quantity,
				toWarehouseItem.getMvccVersion());

		CommerceInventoryAuditType commerceInventoryAuditType =
			_commerceInventoryAuditTypeRegistry.getCommerceInventoryAuditType(
				CommerceInventoryConstants.AUDIT_TYPE_MOVE_QUANTITY);

		_commerceInventoryAuditLocalService.addCommerceInventoryAudit(
			userId, sku, commerceInventoryAuditType.getType(),
			commerceInventoryAuditType.getLog(
				HashMapBuilder.put(
					CommerceInventoryAuditTypeConstants.FROM,
					() -> {
						CommerceInventoryWarehouse
							fromCommerceInventoryWarehouse =
								fromWarehouseItem.
									getCommerceInventoryWarehouse();

						return String.valueOf(
							fromCommerceInventoryWarehouse.getName());
					}
				).put(
					CommerceInventoryAuditTypeConstants.TO,
					() -> {
						CommerceInventoryWarehouse
							toCommerceInventoryWarehouse =
								toWarehouseItem.getCommerceInventoryWarehouse();

						return String.valueOf(
							toCommerceInventoryWarehouse.getName());
					}
				).build()),
			quantity);
	}

	@Override
	public CommerceInventoryWarehouseItem updateCommerceInventoryWarehouseItem(
			long userId, long commerceInventoryWarehouseItemId, int quantity,
			int reservedQuantity, long mvccVersion)
		throws PortalException {

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.findByPrimaryKey(
				commerceInventoryWarehouseItemId);

		if (commerceInventoryWarehouseItem.getMvccVersion() != mvccVersion) {
			throw new MVCCException();
		}

		commerceInventoryWarehouseItem.setQuantity(quantity);
		commerceInventoryWarehouseItem.setReservedQuantity(reservedQuantity);

		commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.update(
				commerceInventoryWarehouseItem);

		CommerceInventoryAuditType commerceInventoryAuditType =
			_commerceInventoryAuditTypeRegistry.getCommerceInventoryAuditType(
				CommerceInventoryConstants.AUDIT_TYPE_UPDATE_WAREHOUSE_ITEM);

		CommerceInventoryWarehouse commerceInventoryWarehouse =
			commerceInventoryWarehouseItem.getCommerceInventoryWarehouse();

		_commerceInventoryAuditLocalService.addCommerceInventoryAudit(
			userId, commerceInventoryWarehouseItem.getSku(),
			commerceInventoryAuditType.getType(),
			commerceInventoryAuditType.getLog(
				HashMapBuilder.put(
					CommerceInventoryAuditTypeConstants.RESERVED,
					String.valueOf(reservedQuantity)
				).put(
					CommerceInventoryAuditTypeConstants.WAREHOUSE,
					String.valueOf(commerceInventoryWarehouse.getName())
				).build()),
			quantity);

		return commerceInventoryWarehouseItem;
	}

	@Override
	public CommerceInventoryWarehouseItem updateCommerceInventoryWarehouseItem(
			long userId, long commerceInventoryWarehouseItemId, int quantity,
			long mvccVersion)
		throws PortalException {

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.findByPrimaryKey(
				commerceInventoryWarehouseItemId);

		if (commerceInventoryWarehouseItem.getMvccVersion() != mvccVersion) {
			throw new MVCCException();
		}

		commerceInventoryWarehouseItem.setQuantity(quantity);

		commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.update(
				commerceInventoryWarehouseItem);

		CommerceInventoryAuditType commerceInventoryAuditType =
			_commerceInventoryAuditTypeRegistry.getCommerceInventoryAuditType(
				CommerceInventoryConstants.AUDIT_TYPE_UPDATE_WAREHOUSE_ITEM);

		CommerceInventoryWarehouse commerceInventoryWarehouse =
			commerceInventoryWarehouseItem.getCommerceInventoryWarehouse();

		_commerceInventoryAuditLocalService.addCommerceInventoryAudit(
			userId, commerceInventoryWarehouseItem.getSku(),
			commerceInventoryAuditType.getType(),
			commerceInventoryAuditType.getLog(
				HashMapBuilder.put(
					CommerceInventoryAuditTypeConstants.WAREHOUSE,
					String.valueOf(commerceInventoryWarehouse.getName())
				).build()),
			quantity);

		return commerceInventoryWarehouseItem;
	}

	private void _validate(
			long commerceInventoryWarehouseId, String sku,
			String unitOfMeasureKey)
		throws PortalException {

		if (Validator.isNull(sku)) {
			throw new CommerceInventoryWarehouseItemSkuException(
				"Sku is mandatory");
		}

		CommerceInventoryWarehouseItem commerceInventoryWarehouseItem =
			commerceInventoryWarehouseItemPersistence.fetchByCIWI_S_U(
				commerceInventoryWarehouseId, sku, unitOfMeasureKey);

		if (commerceInventoryWarehouseItem != null) {
			throw new DuplicateCommerceInventoryWarehouseItemException();
		}
	}

	@Reference
	private CommerceInventoryAuditLocalService
		_commerceInventoryAuditLocalService;

	@Reference
	private CommerceInventoryAuditTypeRegistry
		_commerceInventoryAuditTypeRegistry;

	@Reference
	private CPInstanceLocalService _cpInstanceLocalService;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}