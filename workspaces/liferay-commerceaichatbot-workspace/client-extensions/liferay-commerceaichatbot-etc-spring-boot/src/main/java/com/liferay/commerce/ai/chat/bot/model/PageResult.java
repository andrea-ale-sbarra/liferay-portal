/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivica Cardic
 */
public class PageResult<T> {

	public PageResult() {
	}

	public PageResult(int totalCount, int lastPage, List<T> items) {
		_totalCount = totalCount;
		_lastPage = lastPage;

		if (items != null) {
			_items = new ArrayList<>(items);
		}
	}

	public List<T> getItems() {
		return _items;
	}

	public int getLastPage() {
		return _lastPage;
	}

	public int getTotalCount() {
		return _totalCount;
	}

	public void setItems(List<T> items) {
		_items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
	}

	public void setLastPage(int lastPage) {
		_lastPage = lastPage;
	}

	public void setTotalCount(int totalCount) {
		_totalCount = totalCount;
	}

	private List<T> _items = new ArrayList<>();
	private int _lastPage;
	private int _totalCount;

}