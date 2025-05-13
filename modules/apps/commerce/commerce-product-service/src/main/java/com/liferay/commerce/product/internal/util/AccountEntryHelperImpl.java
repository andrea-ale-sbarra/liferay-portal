package com.liferay.commerce.product.internal.util;

import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalService;
import com.liferay.commerce.product.util.AccountEntryHelper;
import com.liferay.expando.kernel.service.ExpandoValueLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;


@Component(service = AccountEntryHelper.class)
public class AccountEntryHelperImpl implements AccountEntryHelper {

	@Override
	public List<Long> getAccountExcludedProductsIds(long commerceAccountId)
		throws PortalException{

		return getAccountExcludedProductsIds(
			_accountEntryLocalService.fetchAccountEntry(commerceAccountId)
		);
	}

	@Override
	public List<Long> getAccountExcludedProductsIds(AccountEntry accountEntry)
		throws PortalException{

		if(accountEntry == null){
			return new ArrayList<>();
		}
		return _parseToLongArray(
			_getAccountCustomFieldValue(
				accountEntry.getCompanyId(),
				accountEntry.getPrimaryKey(),
				"excluded_products")
		);
	}

	@Override
	public long[] getAccountExcludedProductsIdsAsArray(AccountEntry accountEntry)
		throws PortalException{

		return toLongArray(
			getAccountExcludedProductsIds(accountEntry)
		);
	}

	@Override
	public List<Long> getAccountExcludedCategoriesIds(long accountId)
		throws PortalException{

		return getAccountExcludedCategoriesIds(
			_accountEntryLocalService.fetchAccountEntry(accountId)
		);
	}

	@Override
	public List<Long> getAccountExcludedCategoriesIds(AccountEntry accountEntry)
		throws PortalException{

		if(accountEntry == null){
			return new ArrayList<>();
		}
		return _parseToLongArray(
			_getAccountCustomFieldValue(
				accountEntry.getCompanyId(),
				accountEntry.getPrimaryKey(),
				"excluded_categories")
		);
	}

	@Override
	public long[] getAccountExcludedCategoriesIdsAsArray(
		AccountEntry accountEntry)
		throws PortalException{

		return toLongArray(
			getAccountExcludedCategoriesIds(accountEntry)
		);
	}

	private String _getAccountCustomFieldValue(long companyId, long accountId, String fieldName) throws PortalException{

		return _expandoValueLocalService.getData(companyId,
			"com.liferay.account.model.AccountEntry", "CUSTOM_FIELDS",
			fieldName, accountId,
			"");
	}

	private List<Long> _parseToLongArray(String value) throws PortalException{

		if(value == null || value.isEmpty()){
			return new ArrayList<>();
		}

		String[] parts = value.split(",");
		List<Long> numbers = new ArrayList<>();

		for (String part : parts) {

			try {
				long num = Long.parseLong(part.trim());
				numbers.add(num);
			} catch (NumberFormatException e) {
				throw new PortalException(String.format("Unable to parse value %s to long ", part));
			}
		}

		return numbers;
	}

	private long[] toLongArray(List<Long> list) throws PortalException{

		if(list == null || list.isEmpty()){
			return null;
		}

		int size = list.size();

		long[] result = new long[size];

		for (int i = 0; i < size; i++) {
			result[i] = list.get(i);
		}

		return result;
	}

	@Reference
	private AccountEntryLocalService _accountEntryLocalService;

	@Reference
	private ExpandoValueLocalService _expandoValueLocalService;
}
