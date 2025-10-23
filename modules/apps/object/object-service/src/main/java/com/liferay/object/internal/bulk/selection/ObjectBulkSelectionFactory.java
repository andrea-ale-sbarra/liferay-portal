/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.bulk.selection;

import com.liferay.bulk.selection.BulkSelection;
import com.liferay.bulk.selection.BulkSelectionFactory;
import com.liferay.bulk.selection.EmptyBulkSelection;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.portal.kernel.util.MapUtil;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Andrea Sbarra
 */
@Component(
	property = "model.class.name=java.lang.Object",
	service = BulkSelectionFactory.class
)
public class ObjectBulkSelectionFactory
	implements BulkSelectionFactory<Object> {

	@Override
	public BulkSelection<Object> create(Map<String, String[]> parameterMap) {
		boolean selectAll = MapUtil.getBoolean(parameterMap, "selectAll");

		if (selectAll) {
			return null;
			/*

			return new SearchObjectEntryBulkSelection(
				BulkSelectionFactoryUtil.getRepositoryId(parameterMap),
				BulkSelectionFactoryUtil.getFolderId(parameterMap),
				parameterMap, _repositoryProvider, _dlAppService,
				_assetEntryLocalService, _dlAssetHelper);
			 */
		}

		if (!parameterMap.containsKey("rowIds")) {
			return new EmptyBulkSelection<>();
		}

		String[] rowIds = parameterMap.get("rowIds");

		return _getObjectBulkSelection(rowIds, parameterMap);
	}

	private BulkSelection<Object> _getObjectBulkSelection(
		String[] values, Map<String, String[]> parameterMap) {

		return new ObjectBulkSelection(
			values, parameterMap, _objectEntryLocalService,
			_objectFolderLocalService);
	}

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectFolderLocalService _objectFolderLocalService;

}