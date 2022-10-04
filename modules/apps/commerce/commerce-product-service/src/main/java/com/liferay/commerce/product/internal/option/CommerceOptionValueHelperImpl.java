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

package com.liferay.commerce.product.internal.option;

import com.liferay.commerce.product.data.source.CommerceOptionValueDataSource;
import com.liferay.commerce.product.data.source.CommerceOptionValueDataSourceRegistry;
import com.liferay.commerce.product.internal.data.source.AssetCategoriesCommerceOptionValueDataSourceImpl;
import com.liferay.commerce.product.internal.util.comparator.CPDefinitionOptionRelComparator;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.model.CPInstance;
import com.liferay.commerce.product.option.CommerceOptionValue;
import com.liferay.commerce.product.option.CommerceOptionValueHelper;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionOptionRelLocalService;
import com.liferay.commerce.product.service.CPDefinitionOptionValueRelLocalService;
import com.liferay.commerce.product.util.CPDefinitionHelper;
import com.liferay.commerce.product.util.JsonHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Igor Beslic
 */
@Component(
	enabled = false, immediate = true, service = CommerceOptionValueHelper.class
)
public class CommerceOptionValueHelperImpl
	implements CommerceOptionValueHelper {

	@Override
	public List<CommerceOptionValue> getCPDefinitionCommerceOptionValues(
		long companyId, long commerceChannelGroupId, long cpDefinitionId, String json)
		throws PortalException {

		List<CommerceOptionValue> commerceOptionValues = new ArrayList<>();

		_filterDataSource(_dataSource(companyId, commerceChannelGroupId, cpDefinitionId), json, commerceOptionValues);

		Map<Long, List<Long>>
			cpDefinitionOptionRelCPDefinitionOptionValueRelIds =
				_cpDefinitionOptionRelLocalService.
					getCPDefinitionOptionRelCPDefinitionOptionValueRelIds(
						cpDefinitionId, json);

		if (cpDefinitionOptionRelCPDefinitionOptionValueRelIds.isEmpty() && commerceOptionValues.isEmpty()) {
			return Collections.emptyList();
		}

		for (Map.Entry<Long, List<Long>> entry :
				cpDefinitionOptionRelCPDefinitionOptionValueRelIds.entrySet()) {

			Long key = entry.getKey();
			List<Long> value = entry.getValue();

			for (long cpDefinitionOptionValueRelId : value) {
				CPDefinitionOptionRel cpDefinitionOptionRel =
					_cpDefinitionOptionRelLocalService.
						fetchCPDefinitionOptionRel(key);

				if (cpDefinitionOptionRel == null) {
					continue;
				}

				CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
					_cpDefinitionOptionValueRelLocalService.
						fetchCPDefinitionOptionValueRel(
							cpDefinitionOptionValueRelId);

				if (cpDefinitionOptionValueRel == null) {
					continue;
				}

				CommerceOptionValue.Builder commerceOptionValueBuilder =
					new CommerceOptionValue.Builder();

				commerceOptionValueBuilder.optionKey(
					cpDefinitionOptionRel.getKey());
				commerceOptionValueBuilder.optionValueKey(
					cpDefinitionOptionValueRel.getKey());

				if (!cpDefinitionOptionRel.isPriceContributor()) {
					commerceOptionValues.add(
						commerceOptionValueBuilder.build());

					continue;
				}

				commerceOptionValueBuilder.priceType(
					cpDefinitionOptionRel.getPriceType());

				commerceOptionValueBuilder.price(
					cpDefinitionOptionValueRel.getPrice());
				commerceOptionValueBuilder.quantity(
					cpDefinitionOptionValueRel.getQuantity());

				CPInstance cpDefinitionOptionValueRelCPInstance =
					cpDefinitionOptionValueRel.fetchCPInstance();

				if (cpDefinitionOptionValueRelCPInstance != null) {
					commerceOptionValueBuilder.cpInstanceId(
						cpDefinitionOptionValueRelCPInstance.getCPInstanceId());

					if (cpDefinitionOptionRel.isPriceTypeDynamic()) {
						commerceOptionValueBuilder.price(
							cpDefinitionOptionValueRelCPInstance.getPrice());
					}
				}

				commerceOptionValues.add(commerceOptionValueBuilder.build());
			}
		}

		return commerceOptionValues;
	}

	private void _filterDataSource(
		Map<CPDefinitionOptionRel, List<CPDefinitionOptionValueRel>> dataSource, String json, List<CommerceOptionValue> commerceOptionValues)
		throws JSONException {

		if (_jsonHelper.isEmpty(json)) {
			return ;
		}

		JSONArray jsonArray = _jsonFactory.createJSONArray();

		if (_jsonHelper.isArray(json)) {
			jsonArray = _jsonFactory.createJSONArray(json);
		}
		else {
			jsonArray.put(_jsonFactory.createJSONObject(json));
		}

		CommerceOptionValue.Builder commerceOptionValueBuilder =
			new CommerceOptionValue.Builder();

		for(Map.Entry<CPDefinitionOptionRel, List<CPDefinitionOptionValueRel>> entry :dataSource.entrySet()) {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				CPDefinitionOptionRel cpDefinitionOptionRel = entry.getKey();
				if (cpDefinitionOptionRel.getKey().equals(
					jsonObject.getString("key"))) {
					JSONArray valueJSONArray = _jsonHelper.getValueAsJSONArray(
						"value", jsonObject);
					for (int j = 0; j < valueJSONArray.length(); j++) {
						for (CPDefinitionOptionValueRel cpDefinitionOptionValueRel : entry.getValue()) {
							if (cpDefinitionOptionValueRel.getKey().equals(
								valueJSONArray.getString(j))) {


								commerceOptionValueBuilder.optionKey(
									cpDefinitionOptionRel.getKey());
								commerceOptionValueBuilder.optionValueKey(
									cpDefinitionOptionValueRel.getKey());

								commerceOptionValueBuilder.priceType(
									cpDefinitionOptionRel.getPriceType());

								commerceOptionValueBuilder.price(
									cpDefinitionOptionValueRel.getPrice());
								commerceOptionValueBuilder.quantity(
									cpDefinitionOptionValueRel.getQuantity());

								CPInstance
									cpDefinitionOptionValueRelCPInstance =
									cpDefinitionOptionValueRel.fetchCPInstance();

								if (cpDefinitionOptionValueRelCPInstance !=
									null) {
									commerceOptionValueBuilder.cpInstanceId(
										cpDefinitionOptionValueRelCPInstance.getCPInstanceId());

									if (cpDefinitionOptionRel.isPriceTypeDynamic()) {
										commerceOptionValueBuilder.price(
											cpDefinitionOptionValueRelCPInstance.getPrice());
									}
								}
							}
						}
					}
				}
			}
		}

		commerceOptionValues.add(commerceOptionValueBuilder.build());
	}

	private Map<CPDefinitionOptionRel, List<CPDefinitionOptionValueRel>> _dataSource(
		long companyId, long commerceChannelGroupId, long cpDefinitionId) throws PortalException {

		String dataSourceName =
			AssetCategoriesCommerceOptionValueDataSourceImpl.NAME;

		CommerceOptionValueDataSource commerceOptionValueDataSource = _commerceOptionValueDataSourceRegistry.getCommerceOptionValueDataSource(
			dataSourceName);

		return commerceOptionValueDataSource.getCPDefinitionOptionValueRelsMap(companyId, commerceChannelGroupId, cpDefinitionId, -1 ,-1);

	}

	@Override
	public CommerceOptionValue toCommerceOptionValue(String json)
		throws JSONException {

		return _toCommerceOptionValue(_jsonFactory.createJSONObject(json));
	}

	@Override
	public List<CommerceOptionValue> toCommerceOptionValues(String json)
		throws JSONException {

		List<CommerceOptionValue> commerceOptionValues = new ArrayList<>();

		JSONArray commerceOptionValuesJSONArray = _jsonHelper.getJSONArray(
			json);

		for (int i = 0; i < commerceOptionValuesJSONArray.length(); i++) {
			JSONObject jsonObject = commerceOptionValuesJSONArray.getJSONObject(
				i);

			JSONArray valueJSONArray = _jsonHelper.getValueAsJSONArray(
				"value", jsonObject);

			if (valueJSONArray.length() > 0) {
				for (int j = 0; j < valueJSONArray.length(); j++) {
					String value = valueJSONArray.getString(j);

					jsonObject.put("value", value);

					commerceOptionValues.add(
						_toCommerceOptionValue(jsonObject));
				}
			}
			else {
				commerceOptionValues.add(_toCommerceOptionValue(jsonObject));
			}
		}

		return commerceOptionValues;
	}

	private CommerceOptionValue _toCommerceOptionValue(JSONObject jsonObject) {
		CommerceOptionValue.Builder commerceOptionValueBuilder =
			new CommerceOptionValue.Builder();

		commerceOptionValueBuilder.optionKey(jsonObject.getString("key"));

		JSONArray valueJSONArray = _jsonHelper.getValueAsJSONArray(
			"value", jsonObject);

		if (valueJSONArray.length() > 0) {
			commerceOptionValueBuilder.optionValueKey(
				valueJSONArray.getString(0));
		}

		if (jsonObject.has("priceType")) {
			commerceOptionValueBuilder.priceType(
				jsonObject.getString("priceType"));
		}

		if (jsonObject.has("price")) {
			commerceOptionValueBuilder.price(
				new BigDecimal(jsonObject.getString("price")));
		}

		if (jsonObject.has("quantity")) {
			commerceOptionValueBuilder.quantity(jsonObject.getInt("quantity"));
		}

		if (jsonObject.has("cpInstanceId")) {
			commerceOptionValueBuilder.cpInstanceId(
				jsonObject.getLong("cpInstanceId"));
		}

		return commerceOptionValueBuilder.build();
	}

	@Reference
	protected CPDefinitionHelper cpDefinitionHelper;
	
	@Reference
	private CPDefinitionLocalService _cpDefinitionLocalService;
	
	@Reference
	private CPDefinitionOptionRelLocalService
		_cpDefinitionOptionRelLocalService;

	@Reference
	private CPDefinitionOptionValueRelLocalService
		_cpDefinitionOptionValueRelLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private CommerceOptionValueDataSourceRegistry
		_commerceOptionValueDataSourceRegistry;

	@Reference
	private JsonHelper _jsonHelper;

}