/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.bulk.rest.internal.resource.v1_0;

import com.liferay.bulk.rest.dto.v1_0.BulkAction;
import com.liferay.bulk.rest.dto.v1_0.BulkActionItem;
import com.liferay.bulk.rest.dto.v1_0.BulkActionTask;
import com.liferay.bulk.rest.dto.v1_0.KeywordBulkAction;
import com.liferay.bulk.rest.internal.odata.entity.v1_0.BulkActionEntityModel;
import com.liferay.bulk.rest.internal.selection.v1_0.BulkActionBulkSelectionFactory;
import com.liferay.bulk.rest.resource.v1_0.BulkActionResource;
import com.liferay.bulk.selection.BulkSelection;
import com.liferay.bulk.selection.BulkSelectionAction;
import com.liferay.bulk.selection.BulkSelectionFactoryRegistry;
import com.liferay.bulk.selection.BulkSelectionInputParameters;
import com.liferay.bulk.selection.BulkSelectionRunner;
import com.liferay.object.constants.ObjectEntryFolderConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.feature.flag.FeatureFlagManagerUtil;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.search.filter.Filter;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.search.searcher.SearchRequestBuilderFactory;
import com.liferay.portal.search.searcher.Searcher;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import jakarta.ws.rs.core.MultivaluedMap;

import java.io.Serializable;

import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Alejandro Tardín
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/bulk-action.properties",
	scope = ServiceScope.PROTOTYPE, service = BulkActionResource.class
)
public class BulkActionResourceImpl extends BaseBulkActionResourceImpl {

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap)
		throws Exception {

		return _entityModel;
	}

	@Override
	public BulkActionTask postBulkAction(
			String blueprintExternalReferenceCode, Boolean emptySearch,
			String entryClassNames, String scope, String search, Filter filter,
			Pagination pagination, Sort[] sorts, BulkAction bulkAction)
		throws Exception {

		if (!FeatureFlagManagerUtil.isEnabled(
				contextCompany.getCompanyId(), "LPD-17564")) {

			throw new UnsupportedOperationException();
		}

		BulkActionBulkSelectionFactory bulkActionBulkSelectionFactory =
			_getBulkActionBulkSelectionFactory(
				blueprintExternalReferenceCode, emptySearch, entryClassNames,
				scope, search, filter, pagination, sorts, bulkAction);

		BulkSelection<Object> bulkSelection =
			bulkActionBulkSelectionFactory.create();

		if (bulkSelection.getSize() == 0) {
			return new BulkActionTask();
		}

		BulkAction.Type type = bulkAction.getType();

		BulkActionTask bulkActionTask = _addBulkActionTask(type);

		_bulkSelectionRunner.run(
			contextUser, bulkSelection, _getBulkSelectionAction(type),
			_getInputMap(bulkAction, bulkActionTask, type));

		return bulkActionTask;
	}

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;


	private BulkActionTask _addBulkActionTask(BulkAction.Type type) throws Exception {
		ObjectDefinition objectDefinition = _objectDefinitionLocalService.
			getObjectDefinitionByExternalReferenceCode(
				"L_CMS_BULK_ACTION_TASK", contextCompany.getCompanyId());

		ObjectEntry objectEntry = _objectEntryLocalService.addObjectEntry(
			0, contextUser.getUserId(),
			objectDefinition.getObjectDefinitionId(),
			ObjectEntryFolderConstants.PARENT_OBJECT_ENTRY_FOLDER_ID_DEFAULT,
			contextAcceptLanguage.getPreferredLanguageId(),
			HashMapBuilder.<String, Serializable>put(
				"actionName", type.toString()
			).put(
				"executionStatus", "initial"
			).put(
				"type", type.toString()
			).build(),
			new ServiceContext());

		Map<String, Serializable> values = objectEntry.getValues();

		return new BulkActionTask() {
			{
				setActionName(
					() -> GetterUtil.getString(values.get("actionName")));
				setAuthor(objectEntry::getUserName);
				setCreatedDate(objectEntry::getCreateDate);
				setExecuteStatus(
					() -> GetterUtil.getString(values.get("executionStatus")));
				setExternalReferenceCode(objectEntry::getExternalReferenceCode);
				setId(objectEntry::getObjectEntryId);
				setType(() -> GetterUtil.getString(values.get("type")));
			}
		};
	}

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Override
	public Page<BulkActionItem> postBulkActionItemPreviewPage(
			Boolean fetchChildren, String search, Filter filter,
			Pagination pagination, Sort[] sorts, BulkAction bulkAction)
		throws Exception {

		return super.postBulkActionItemPreviewPage(
			fetchChildren, search, filter, pagination, sorts, bulkAction);
	}

	private BulkActionBulkSelectionFactory _getBulkActionBulkSelectionFactory(
		String blueprintExternalReferenceCode, Boolean emptySearch,
		String entryClassNames, String scope, String search, Filter filter,
		Pagination pagination, Sort[] sorts, BulkAction bulkAction) {

		return new BulkActionBulkSelectionFactory.Builder(
		).bulkSelectionFactoryRegistry(
			_bulkSelectionFactoryRegistry
		).indexerRegistry(
			_indexerRegistry
		).localization(
			_localization
		).searcher(
			_searcher
		).searchRequestBuilderFactory(
			_searchRequestBuilderFactory
		).contextAcceptLanguage(
			contextAcceptLanguage
		).contextCompany(
			contextCompany
		).blueprintExternalReferenceCode(
			blueprintExternalReferenceCode
		).emptySearch(
			emptySearch
		).entryClassNames(
			entryClassNames
		).scope(
			scope
		).search(
			search
		).filter(
			filter
		).pagination(
			pagination
		).sorts(
			sorts
		).bulkAction(
			bulkAction
		).contextHttpServletRequest(
			contextHttpServletRequest
		).contextUser(
			contextUser
		).build();
	}

	private BulkSelectionAction<Object> _getBulkSelectionAction(
		BulkAction.Type type) {

		if (BulkAction.Type.DELETE_BULK_ACTION.equals(type)) {
			return _deleteObjectBulkSelectionAction;
		}
		else if (BulkAction.Type.KEYWORD_BULK_ACTION.equals(type)) {
			return _editObjectTagsBulkSelectionAction;
		}

		throw new UnsupportedOperationException();
	}

	private Map<String, Serializable> _getInputMap(
		BulkAction bulkAction, BulkActionTask bulkActionTask, BulkAction.Type type) {

		if (BulkAction.Type.DELETE_BULK_ACTION.equals(type)) {
			return HashMapBuilder.<String, Serializable>
				put("bulkActionTaskId", bulkActionTask.getId())
				.build();
		}
		else if (BulkAction.Type.KEYWORD_BULK_ACTION.equals(type)) {
			return HashMapBuilder.<String, Serializable>put(
				BulkSelectionInputParameters.ASSET_ENTRY_BULK_SELECTION, true
			).put("bulkActionTaskId", bulkActionTask.getId())
			.put(
				"append", true
			).put(
				"toAddTagNames",
				() -> {
					KeywordBulkAction keywordBulkAction =
						(KeywordBulkAction)bulkAction;

					return keywordBulkAction.getKeywordsToAdd();
				}
			).put(
				"toRemoveTagNames",
				() -> {
					KeywordBulkAction keywordBulkAction =
						(KeywordBulkAction)bulkAction;

					return keywordBulkAction.getKeywordsToRemove();
				}
			).build();
		}

		throw new UnsupportedOperationException();
	}

	private static final EntityModel _entityModel = new BulkActionEntityModel();

	@Reference
	private BulkSelectionFactoryRegistry _bulkSelectionFactoryRegistry;

	@Reference
	private BulkSelectionRunner _bulkSelectionRunner;

	@Reference(target = "(bulk.selection.action.key=delete.object)")
	private BulkSelectionAction<Object> _deleteObjectBulkSelectionAction;

	@Reference(target = "(bulk.selection.action.key=edit.object.tags)")
	private BulkSelectionAction<Object> _editObjectTagsBulkSelectionAction;

	@Reference
	private IndexerRegistry _indexerRegistry;

	@Reference
	private Localization _localization;

	@Reference
	private Searcher _searcher;

	@Reference
	private SearchRequestBuilderFactory _searchRequestBuilderFactory;

}