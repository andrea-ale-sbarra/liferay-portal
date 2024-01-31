/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shop.by.diagram.web.internal.portlet.action;

import com.liferay.commerce.product.constants.CPPortletKeys;
import com.liferay.commerce.product.constants.CommerceCatalogConstants;
import com.liferay.commerce.product.exception.CPAttachmentFileEntryNameException;
import com.liferay.commerce.product.exception.CPAttachmentFileEntrySizeException;
import com.liferay.commerce.product.model.CommerceCatalog;
import com.liferay.commerce.product.service.CommerceCatalogLocalService;
import com.liferay.commerce.shop.by.diagram.configuration.CSDiagramSettingImageConfiguration;
import com.liferay.commerce.shop.by.diagram.constants.CSDiagramSettingsConstants;
import com.liferay.commerce.shop.by.diagram.web.internal.util.CSDiagramSettingUtil;
import com.liferay.item.selector.ItemSelectorUploadResponseHandler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepository;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.servlet.ServletResponseConstants;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.File;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.upload.UploadFileEntryHandler;
import com.liferay.upload.UploadHandler;
import com.liferay.upload.UploadResponseHandler;

import java.io.IOException;
import java.io.InputStream;

import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alessio Antonio Rendina
 */
@Component(
	configurationPid = "com.liferay.commerce.shop.by.diagram.configuration.CSDiagramSettingImageConfiguration",
	property = {
		"javax.portlet.name=" + CPPortletKeys.CP_DEFINITIONS,
		"mvc.command.name=/cp_definitions/upload_cs_diagram_setting_image"
	},
	service = MVCActionCommand.class
)
public class UploadCSDiagramSettingImageMVCActionCommand
	extends BaseMVCActionCommand {

	@Activate
	protected void activate(Map<String, Object> properties) {
		_csDiagramSettingImageConfiguration =
			ConfigurableUtil.createConfigurable(
				CSDiagramSettingImageConfiguration.class, properties);
	}

	@Override
	protected void doProcessAction(
			ActionRequest actionRequest, ActionResponse actionResponse)
		throws Exception {

		_uploadHandler.upload(
			_csDiagramSettingImageUploadFileEntryHandler,
			_csDiagramSettingImageUploadResponseHandler, actionRequest,
			actionResponse);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UploadCSDiagramSettingImageMVCActionCommand.class);

	@Reference
	private CommerceCatalogLocalService _commerceCatalogLocalService;

	private volatile CSDiagramSettingImageConfiguration
		_csDiagramSettingImageConfiguration;
	private final CSDiagramSettingImageUploadFileEntryHandler
		_csDiagramSettingImageUploadFileEntryHandler =
			new CSDiagramSettingImageUploadFileEntryHandler();
	private final CSDiagramSettingImageUploadResponseHandler
		_csDiagramSettingImageUploadResponseHandler =
			new CSDiagramSettingImageUploadResponseHandler();

	@Reference
	private File _file;

	@Reference
	private ItemSelectorUploadResponseHandler
		_itemSelectorUploadResponseHandler;

	@Reference
	private PortletFileRepository _portletFileRepository;

	@Reference
	private UploadHandler _uploadHandler;

	private class CSDiagramSettingImageUploadFileEntryHandler
		implements UploadFileEntryHandler {

		@Override
		public FileEntry upload(UploadPortletRequest uploadPortletRequest)
			throws IOException, PortalException {

			String fileName = uploadPortletRequest.getFileName(_parameterName);

			_validateFile(
				fileName, uploadPortletRequest.getSize(_parameterName));

			try (InputStream inputStream = uploadPortletRequest.getFileAsStream(
					_parameterName)) {

				String contentType = uploadPortletRequest.getContentType(
					_parameterName);

				if (StringUtil.containsIgnoreCase(contentType, "svg")) {
					return _addFileEntry(
						fileName, contentType,
						CSDiagramSettingUtil.cleanInputStream(inputStream),
						(ThemeDisplay)uploadPortletRequest.getAttribute(
							WebKeys.THEME_DISPLAY));
				}

				return _addFileEntry(
					fileName, contentType, inputStream,
					(ThemeDisplay)uploadPortletRequest.getAttribute(
						WebKeys.THEME_DISPLAY));
			}
		}

		private FileEntry _addFileEntry(
				String fileName, String contentType, InputStream inputStream,
				ThemeDisplay themeDisplay)
			throws PortalException {

			Folder folder = _commerceCatalogLocalService.addCatalogFolder(
				themeDisplay.getUserId(), themeDisplay.getRefererGroupId(),
				CSDiagramSettingsConstants.FOLDER_NAME);

			String uniqueFileName = _portletFileRepository.getUniqueFileName(
				themeDisplay.getRefererGroupId(), folder.getFolderId(),
				fileName);

			CommerceCatalog commerceCatalog =
				_commerceCatalogLocalService.fetchCommerceCatalogByGroupId(
					themeDisplay.getRefererGroupId());

			return _portletFileRepository.addPortletFileEntry(
				null, themeDisplay.getRefererGroupId(),
				themeDisplay.getUserId(), CommerceCatalog.class.getName(),
				commerceCatalog.getCommerceCatalogId(),
				CommerceCatalogConstants.SERVICE_NAME, folder.getFolderId(),
				inputStream, uniqueFileName, contentType, true);
		}

		private void _validateFile(String fileName, long size)
			throws PortalException {

			if ((_csDiagramSettingImageConfiguration.imageMaxSize() > 0) &&
				(size > _csDiagramSettingImageConfiguration.imageMaxSize())) {

				throw new CPAttachmentFileEntrySizeException();
			}

			String extension = _file.getExtension(fileName);

			String[] imageExtensions =
				_csDiagramSettingImageConfiguration.imageExtensions();

			for (String imageExtension : imageExtensions) {
				if (StringPool.STAR.equals(imageExtension) ||
					imageExtension.equals(StringPool.PERIOD + extension)) {

					return;
				}
			}

			throw new CPAttachmentFileEntryNameException(
				"Invalid image for file name " + fileName);
		}

		private final String _parameterName = "imageSelectorFileName";

	}

	private class CSDiagramSettingImageUploadResponseHandler
		implements UploadResponseHandler {

		@Override
		public JSONObject onFailure(
				PortletRequest portletRequest, PortalException portalException)
			throws PortalException {

			JSONObject jsonObject =
				_itemSelectorUploadResponseHandler.onFailure(
					portletRequest, portalException);

			if (portalException instanceof CPAttachmentFileEntryNameException ||
				portalException instanceof CPAttachmentFileEntrySizeException) {

				String errorMessage = StringPool.BLANK;
				int errorType = 0;

				if (portalException instanceof
						CPAttachmentFileEntryNameException) {

					errorMessage = StringUtil.merge(
						_csDiagramSettingImageConfiguration.imageExtensions());

					errorType =
						ServletResponseConstants.SC_FILE_EXTENSION_EXCEPTION;
				}
				else if (portalException instanceof
							CPAttachmentFileEntrySizeException) {

					errorType = ServletResponseConstants.SC_FILE_SIZE_EXCEPTION;
				}

				jsonObject.put(
					"error",
					JSONUtil.put(
						"errorType", errorType
					).put(
						"message", errorMessage
					));
			}
			else {
				if (_log.isDebugEnabled()) {
					_log.debug(portalException);
				}

				jsonObject.put(
					"error",
					JSONUtil.put(
						"errorType", portalException.getCause()
					).put(
						"message", portalException.getMessage()
					));
			}

			return jsonObject;
		}

		@Override
		public JSONObject onSuccess(
				UploadPortletRequest uploadPortletRequest, FileEntry fileEntry)
			throws PortalException {

			return _itemSelectorUploadResponseHandler.onSuccess(
				uploadPortletRequest, fileEntry);
		}

	}

}