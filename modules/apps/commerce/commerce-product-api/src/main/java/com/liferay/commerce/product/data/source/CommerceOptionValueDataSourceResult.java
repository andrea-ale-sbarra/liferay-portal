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

package com.liferay.commerce.product.data.source;

import com.liferay.commerce.product.option.CommerceOptionValue;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Andrea Sbarra
 */
public class CommerceOptionValueDataSourceResult implements Serializable {

	public CommerceOptionValueDataSourceResult(
		List<CommerceOptionValue> commerceOptionValues, int length) {

		if (commerceOptionValues == null) {
			_commerceOptionValues = Collections.emptyList();
		}
		else {
			_commerceOptionValues = commerceOptionValues;
		}

		_length = length;
	}

	public List<CommerceOptionValue> getCommerceOptionValues() {
		return _commerceOptionValues;
	}

	public int getLength() {
		return _length;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(
			(2 * _commerceOptionValues.size()) + 4);

		sb.append("{data={");

		boolean first = true;

		for (CommerceOptionValue commerceOptionValue: _commerceOptionValues) {
			if (!first) {
				sb.append(StringPool.COMMA_AND_SPACE);
			}

			first = false;

			sb.append(commerceOptionValue);
		}

		sb.append(CharPool.CLOSE_BRACKET);

		sb.append(", length=");
		sb.append(_length);
		sb.append(CharPool.CLOSE_BRACKET);

		return sb.toString();
	}

	private final List<CommerceOptionValue> _commerceOptionValues;
	private final int _length;

}