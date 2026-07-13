/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.pim.site.initializer.internal.servlet;

import com.liferay.depot.constants.DepotConstants;
import com.liferay.depot.model.DepotEntry;
import com.liferay.depot.service.DepotEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Portal;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Serializable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Development-only utility. Maps every {@code PIMBaseSku} object entry to the
 * Commerce catalog {@code Product} schema and writes the result as a
 * batch-engine-importable JSON file. It is invoked as an "async" action from
 * the PIM connectors data set row. The output path is a hard-coded developer
 * workstation directory, so this is not production code.
 *
 * @author Andrea Sbarra
 */
@Component(
	property = {
		"osgi.http.whiteboard.servlet.name=com.liferay.site.pim.site.initializer.internal.servlet.ExportPIMBaseSkuMappingServlet",
		"osgi.http.whiteboard.servlet.pattern=/pim/map-to-commerce/*",
		"servlet.init.httpMethods=GET"
	},
	service = Servlet.class
)
public class ExportPIMBaseSkuMappingServlet extends HttpServlet {

	@Override
	protected void doGet(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
		throws IOException, ServletException {

		httpServletResponse.setContentType(ContentTypes.APPLICATION_JSON);

		long companyId = _portal.getCompanyId(httpServletRequest);

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				fetchObjectDefinitionByExternalReferenceCode(
					_PIM_BASE_SKU_EXTERNAL_REFERENCE_CODE, companyId);

		if (objectDefinition == null) {
			httpServletResponse.setStatus(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

			httpServletResponse.getWriter(
			).write(
				JSONUtil.put(
					"error", "Unable to find the PIMBaseSku object definition"
				).toString()
			);

			return;
		}

		JSONArray productsJSONArray = _jsonFactory.createJSONArray();

		try {
			for (DepotEntry depotEntry :
					_depotEntryLocalService.getDepotEntries(
						companyId, DepotConstants.TYPE_SPACE)) {

				List<ObjectEntry> objectEntries =
					_objectEntryLocalService.getObjectEntries(
						depotEntry.getGroupId(),
						objectDefinition.getObjectDefinitionId(),
						QueryUtil.ALL_POS, QueryUtil.ALL_POS);

				for (ObjectEntry objectEntry : objectEntries) {
					productsJSONArray.put(
						_toProductJSONObject(
							_objectEntryLocalService.getValues(objectEntry)));
				}
			}
		}
		catch (PortalException portalException) {
			throw new ServletException(portalException);
		}

		Path filePath = _writeFile(productsJSONArray);

		httpServletResponse.getWriter(
		).write(
			JSONUtil.put(
				"count", productsJSONArray.length()
			).put(
				"file", filePath.toString()
			).toString()
		);
	}

	private JSONObject _toProductJSONObject(Map<String, Serializable> values) {
		String code = GetterUtil.getString(values.get("code"));

		return JSONUtil.put(
			"catalogId", _MASTER_CATALOG_ID_PLACEHOLDER
		).put(
			"description",
			JSONUtil.put(
				"en_US", GetterUtil.getString(values.get("description")))
		).put(
			"externalReferenceCode", code
		).put(
			"name",
			JSONUtil.put("en_US", GetterUtil.getString(values.get("name")))
		).put(
			"productType", "simple"
		).put(
			"skus",
			JSONUtil.put(
				JSONUtil.put(
					"published", true
				).put(
					"purchasable", true
				).put(
					"sku", code
				))
		);
	}

	private Path _writeFile(JSONArray productsJSONArray) throws IOException {
		Path directoryPath = Paths.get(_OUTPUT_DIRECTORY);

		Files.createDirectories(directoryPath);

		Path filePath = directoryPath.resolve(_OUTPUT_FILE_NAME);

		Files.write(
			filePath,
			productsJSONArray.toString().getBytes(StandardCharsets.UTF_8));

		if (_log.isInfoEnabled()) {
			_log.info(
				"Wrote " + productsJSONArray.length() +
					" mapped products to " + filePath);
		}

		return filePath;
	}

	private static final String _MASTER_CATALOG_ID_PLACEHOLDER =
		"@MASTER_CATALOG_ID@";

	private static final String _OUTPUT_DIRECTORY =
		"/home/andrea/work/src/pim_util";

	private static final String _OUTPUT_FILE_NAME =
		"pim-commerce-products.json";

	private static final String _PIM_BASE_SKU_EXTERNAL_REFERENCE_CODE =
		"L_PIM_BASE_SKU";

	private static final Log _log = LogFactoryUtil.getLog(
		ExportPIMBaseSkuMappingServlet.class);

	@Reference
	private DepotEntryLocalService _depotEntryLocalService;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private Portal _portal;

}
