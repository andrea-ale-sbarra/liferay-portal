/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.ai.hub.internal.instance.lifecycle;

import com.liferay.ai.hub.internal.constants.NotificationConstants;
import com.liferay.notification.context.NotificationContext;
import com.liferay.notification.rest.dto.v1_0.NotificationTemplate;
import com.liferay.notification.rest.dto.v1_0.util.NotificationUtil;
import com.liferay.notification.service.NotificationTemplateLocalService;
import com.liferay.notification.type.NotificationTypeServiceTracker;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.exception.NoSuchUserException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.net.URL;
import java.net.URLConnection;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Danny Situ
 */
@Component(
	property = "service.ranking:Integer=" + Integer.MIN_VALUE,
	service = PortalInstanceLifecycleListener.class
)
public class AddAIHubNotificationsPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		try {
			long companyId = company.getCompanyId();

			_verifyNotificationTemplate(
				companyId,
				NotificationConstants.
					NOTIFICATION_TEMPLATE_EMAIL_EXTERNAL_REFERENCE_CODE,
				"dependencies/notification-template-email.json");
			_verifyNotificationTemplate(
				companyId,
				NotificationConstants.
					NOTIFICATION_TEMPLATE_USER_NOTIFICATION_EXTERNAL_REFERENCE_CODE,
				"dependencies/notification-template-user-notification.json");
		}
		catch (Exception exception) {
			if (_log.isDebugEnabled()) {
				_log.debug(exception);
			}
		}
	}

	private User _getAdminUser(long companyId) throws Exception {
		List<User> users = _userLocalService.getUsersByRoleName(
			companyId, RoleConstants.ADMINISTRATOR, 0, 1);

		if (users.isEmpty()) {
			throw new NoSuchUserException(
				StringBundler.concat(
					"No user exists in company ", companyId, " with role ",
					RoleConstants.ADMINISTRATOR));
		}

		return users.get(0);
	}

	private void _verifyNotificationTemplate(
			long companyId, String externalReferenceCode, String resourcePath)
		throws Exception {

		com.liferay.notification.model.NotificationTemplate
			serviceBuilderNotificationTemplate =
				_notificationTemplateLocalService.
					fetchNotificationTemplateByExternalReferenceCode(
						externalReferenceCode, companyId);

		if (serviceBuilderNotificationTemplate != null) {
			return;
		}

		Class<?> clazz = getClass();

		URL url = clazz.getResource(resourcePath);

		URLConnection urlConnection = url.openConnection();

		String json = StringUtil.read(urlConnection.getInputStream());

		if (Validator.isNull(json)) {
			return;
		}

		NotificationTemplate notificationTemplate = NotificationTemplate.toDTO(
			json);

		NotificationContext notificationContext =
			NotificationUtil.toNotificationContext(
				notificationTemplate, _objectFieldLocalService);

		notificationContext.setCompanyId(companyId);

		User user = _getAdminUser(companyId);

		notificationContext.setNotificationRecipient(
			NotificationUtil.toNotificationRecipient(user, 0L));
		notificationContext.setNotificationRecipientSettings(
			NotificationUtil.toNotificationRecipientSetting(
				0L,
				_notificationTypeServiceTracker.getNotificationType(
					notificationTemplate.getType()),
				notificationTemplate.getRecipients(), user));
		notificationContext.setNotificationTemplate(
			NotificationUtil.toNotificationTemplate(
				0L, notificationTemplate, _objectDefinitionLocalService, user));

		_notificationTemplateLocalService.addNotificationTemplate(
			notificationContext);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		AddAIHubNotificationsPortalInstanceLifecycleListener.class);

	@Reference
	private NotificationTemplateLocalService _notificationTemplateLocalService;

	@Reference
	private NotificationTypeServiceTracker _notificationTypeServiceTracker;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private UserLocalService _userLocalService;

}