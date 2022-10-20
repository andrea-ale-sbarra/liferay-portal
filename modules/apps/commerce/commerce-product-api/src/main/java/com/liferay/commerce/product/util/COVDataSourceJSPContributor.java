package com.liferay.commerce.product.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface COVDataSourceJSPContributor {
	public void render(
		long commerceOptionRelId, HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse)
		throws Exception;
}
