package com.liferay.commerce.product.util;

import com.liferay.account.model.AccountEntry;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.List;

public interface AccountEntryHelper {
	public List<Long> getAccountExcludedProductsIds(long commerceAccountId)
		throws PortalException;

	public List<Long> getAccountExcludedProductsIds(AccountEntry accountEntry)
		throws PortalException;

	public long[] getAccountExcludedProductsIdsAsArray(
		AccountEntry accountEntry)
		throws PortalException;

	public List<Long> getAccountExcludedCategoriesIds(long commerceAccountId)
		throws PortalException;

	public List<Long> getAccountExcludedCategoriesIds(AccountEntry accountEntry)
		throws PortalException;

	public long[] getAccountExcludedCategoriesIdsAsArray(
		AccountEntry accountEntry)
		throws PortalException;
}


