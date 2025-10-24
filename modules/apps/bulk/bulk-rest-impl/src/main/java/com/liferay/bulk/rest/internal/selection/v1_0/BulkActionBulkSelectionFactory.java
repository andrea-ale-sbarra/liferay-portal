/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.bulk.rest.internal.selection.v1_0;

import com.liferay.bulk.rest.dto.v1_0.BulkAction;
import com.liferay.bulk.rest.dto.v1_0.BulkActionItem;
import com.liferay.bulk.rest.dto.v1_0.SelectionScope;
import com.liferay.bulk.selection.BulkSelection;
import com.liferay.bulk.selection.BulkSelectionFactory;
import com.liferay.bulk.selection.BulkSelectionFactoryRegistry;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.liferay.portal.kernel.util.ListUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(service = BulkActionBulkSelectionFactory.class)
public class BulkActionBulkSelectionFactory {

	public BulkSelection<Object> create(
		String search, Filter filter, BulkAction bulkAction) {

		BulkSelectionFactory<Object> bulkSelectionFactory =
			_bulkSelectionFactoryRegistry.getBulkSelectionFactory(
				Object.class.getName());

		return bulkSelectionFactory.create(
			_getParameterMap(
				search, filter, bulkAction.getBulkActionItems(),
				bulkAction.getSelectionScope()));
	}

	private Map<String, String[]> _getParameterMap(
		String search, Filter filter, BulkActionItem[] bulkActionItems,
		SelectionScope selectionScope) {

		if (selectionScope.getSelectAll()) {
			return HashMapBuilder.put(
				"filter", new String[] {filter.toString()}
			).put(
				"search", new String[] {search}
			).put(
				"selectAll",
				new String[] {Boolean.toString(selectionScope.getSelectAll())}
			).build();
		}

		return HashMapBuilder.put(
			"rowIds",
			() -> {
				if (ArrayUtil.isEmpty(bulkActionItems)) {
					return null;
				}

				List<String> rowIds = new ArrayList<>(bulkActionItems.length);

				for (BulkActionItem bulkActionItem : bulkActionItems) {
					rowIds.add(
						StringBundler.concat(
							bulkActionItem.getClassName(), StringPool.SPACE,
							bulkActionItem.getClassPK(), StringPool.COMMA));
				}

				return rowIds.toArray(new String[0]);
			}
		).build();
	}

	@Reference
	private BulkSelectionFactoryRegistry _bulkSelectionFactoryRegistry;

}