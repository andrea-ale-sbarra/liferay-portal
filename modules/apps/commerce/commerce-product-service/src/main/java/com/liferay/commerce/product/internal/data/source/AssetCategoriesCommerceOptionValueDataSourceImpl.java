package com.liferay.commerce.product.internal.data.source;

import com.liferay.asset.kernel.model.AssetCategory;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetCategoryLocalService;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.commerce.product.catalog.CPCatalogEntry;
import com.liferay.commerce.product.catalog.CPQuery;
import com.liferay.commerce.product.catalog.CPSku;
import com.liferay.commerce.product.constants.CPConstants;
import com.liferay.commerce.product.data.source.CPDataSource;
import com.liferay.commerce.product.data.source.CPDataSourceResult;
import com.liferay.commerce.product.data.source.CommerceOptionValueDataSource;
import com.liferay.commerce.product.internal.util.comparator.CPDefinitionOptionRelComparator;
import com.liferay.commerce.product.model.CPDefinition;
import com.liferay.commerce.product.model.CPDefinitionOptionRel;
import com.liferay.commerce.product.model.CPDefinitionOptionValueRel;
import com.liferay.commerce.product.model.impl.CPDefinitionOptionRelImpl;
import com.liferay.commerce.product.model.impl.CPDefinitionOptionValueRelImpl;
import com.liferay.commerce.product.option.CommerceOption;
import com.liferay.commerce.product.option.CommerceOptionValue;
import com.liferay.commerce.product.service.CPDefinitionLocalService;
import com.liferay.commerce.product.service.CPDefinitionLocalServiceUtil;
import com.liferay.commerce.product.util.CPDefinitionHelper;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

@Component(
	enabled = false, immediate = true,
	property = "commerce.option.value.data.source.name=" + AssetCategoriesCommerceOptionValueDataSourceImpl.NAME,
	service = CommerceOptionValueDataSource.class
)
public class AssetCategoriesCommerceOptionValueDataSourceImpl implements
	CommerceOptionValueDataSource {

	public static final String NAME = "assetCategoriesCommerceOptionValue" +
									  "DataSource";

	@Override
	public String getLabel(Locale locale) {
		return _language.get(
			getResourceBundle(locale),
			"products-of-configured-categories");
	}

	@Override
	public String getName() {
		return NAME;
	}

	protected ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundleUtil.getBundle(
			"content.Language", locale, getClass());
	}

	@Override
	public Map<CommerceOption, List<CommerceOptionValue>> getCommerceOptionCommerceOptionValueMap(
		long companyId, long scopeGroupId, long cpDefinitionId, int start,
		int end) throws PortalException {

			Map<CommerceOption, List<CommerceOptionValue>>
				commerceOptionCommerceOptionValueMap = new TreeMap<>();

			AssetEntry assetEntry = _assetEntryLocalService.getEntry(
			CPDefinition.class.getName(), cpDefinitionId);

			for(long categoryId: assetEntry.getCategoryIds()){

				AssetCategory assetCategory = _assetCategoryLocalService.
					fetchAssetCategory(categoryId);

				CPQuery cpQuery = new CPQuery();

				cpQuery.setAnyCategoryIds(new long[]
					{assetCategory.getCategoryId()});

				SearchContext searchContext = new SearchContext();

				searchContext.setAttributes(
					HashMapBuilder.<String, Serializable>put(
						Field.STATUS, WorkflowConstants.STATUS_APPROVED
					).put(
						"excludedCPDefinitionId", cpDefinitionId
					).build());

				CPDataSourceResult cpDataSourceResult =
					cpDefinitionHelper.search(scopeGroupId, searchContext,
						cpQuery, -1, -1);

				List<CommerceOptionValue> commerceOptionValueList =
					new ArrayList<>(cpDataSourceResult.getLength());

				for (CPCatalogEntry cpCatalogEntry:
					cpDataSourceResult.getCPCatalogEntries()){

					CPDefinition cpDefinition =
						CPDefinitionLocalServiceUtil.fetchCPDefinition(
							cpCatalogEntry.getCPDefinitionId());

					List<CPSku> cpSkus = cpCatalogEntry.getCPSkus();

					if(cpSkus.isEmpty() || (cpSkus.size() > 1)){
						continue;
					}

					CPSku cpSku = cpSkus.get(0);

					CommerceOptionValue.Builder commerceOptionValueBuilder =
						new CommerceOptionValue.Builder();

					commerceOptionValueBuilder.optionValueKey(
							FriendlyURLNormalizerUtil.normalize(
								cpCatalogEntry.getName()))
						.optionValueLabel(cpCatalogEntry.getName())
						.cpInstanceUuid(cpSku.getCPInstanceUuid())
						.cProductId(cpDefinition.getCProductId())
						.quantity(1);

					commerceOptionValueList.add(
						commerceOptionValueBuilder.build());
				}

				CommerceOption.Builder commerceOptionBuilder =
					new CommerceOption.Builder();

				commerceOptionBuilder
					.formFieldTypeName("select")
					.optionKey(FriendlyURLNormalizerUtil.normalize(
						assetCategory.getName()))
					.name(assetCategory.getName())
					.priceType(CPConstants.PRODUCT_OPTION_PRICE_TYPE_DYNAMIC)
					.skuContributor(false);

				commerceOptionCommerceOptionValueMap.put(
					commerceOptionBuilder.build(), commerceOptionValueList);
			}

			return commerceOptionCommerceOptionValueMap;
	}

	@Override
	public Map<CPDefinitionOptionRel, List<CPDefinitionOptionValueRel>> getCPDefinitionOptionValueRelsMap(
		long companyId, long scopeGroupId, long cpDefinitionId, int start,
		int end) throws PortalException {

		Map<CPDefinitionOptionRel, List<CPDefinitionOptionValueRel>> cpDefinitionOptionRelsMap = new TreeMap<>(new CPDefinitionOptionRelComparator());


		AssetEntry assetEntry = _assetEntryLocalService.getEntry(
			CPDefinition.class.getName(), cpDefinitionId);

		for(long categoryId: assetEntry.getCategoryIds()) {

			AssetCategory assetCategory = _assetCategoryLocalService.
				fetchAssetCategory(categoryId);

			CPDefinitionOptionRel cpDefinitionOptionRel =
				new CPDefinitionOptionRelImpl();

			cpDefinitionOptionRel.setDDMFormFieldTypeName("select");
			cpDefinitionOptionRel.setKey(
				FriendlyURLNormalizerUtil.normalize(assetCategory.getName()));
			cpDefinitionOptionRel.setName(assetCategory.getName());
			cpDefinitionOptionRel.setPriceType(
				CPConstants.PRODUCT_OPTION_PRICE_TYPE_DYNAMIC);
			cpDefinitionOptionRel.setSkuContributor(false);

			CPQuery cpQuery = new CPQuery();

			cpQuery.setAnyCategoryIds(new long[]
				{categoryId});

			SearchContext searchContext = new SearchContext();

			searchContext.setAttributes(
				HashMapBuilder.<String, Serializable>put(
					Field.STATUS, WorkflowConstants.STATUS_APPROVED
				).put(
					"excludedCPDefinitionId", cpDefinitionId
				).build());

			searchContext.setCompanyId(companyId);

			CPDataSourceResult cpDataSourceResult =
				cpDefinitionHelper.search(scopeGroupId,
					searchContext, cpQuery, -1, -1);

			List<CPDefinitionOptionValueRel> cpDefinitionOptionValueRelList =
				new ArrayList<>(cpDataSourceResult.getLength());

			for (CPCatalogEntry cpCatalogEntry:
				cpDataSourceResult.getCPCatalogEntries()) {

				CPDefinitionOptionValueRel cpDefinitionOptionValueRel =
					new CPDefinitionOptionValueRelImpl();

				List<CPSku> cpSkus = cpCatalogEntry.getCPSkus();

				if(cpSkus.isEmpty() || (cpSkus.size() > 1)){
					continue;
				}

				CPSku cpSku = cpCatalogEntry.getCPSkus().get(0);

				cpDefinitionOptionValueRel.setKey(
					FriendlyURLNormalizerUtil.normalize(
						cpCatalogEntry.getName()));
				cpDefinitionOptionValueRel.setName(cpCatalogEntry.getName());
				cpDefinitionOptionValueRel.setCPInstanceUuid(
					cpSku.getCPInstanceUuid());

				CPDefinition cpDefinition =
					_cpDefinitionLocalService.fetchCPDefinition(
						cpCatalogEntry.getCPDefinitionId());

				cpDefinitionOptionValueRel.setCProductId(
					cpDefinition.getCProductId());
				cpDefinitionOptionValueRel.setQuantity(1);

				cpDefinitionOptionValueRelList.add(cpDefinitionOptionValueRel);
			}
			cpDefinitionOptionRelsMap.put(
				cpDefinitionOptionRel, cpDefinitionOptionValueRelList);
		}

		return cpDefinitionOptionRelsMap;
	}


	@Reference
	private AssetCategoryLocalService _assetCategoryLocalService;
	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	protected CPDefinitionHelper cpDefinitionHelper;

	@Reference
	private CPDefinitionLocalService _cpDefinitionLocalService;

	@Reference
	private Language _language;
}
