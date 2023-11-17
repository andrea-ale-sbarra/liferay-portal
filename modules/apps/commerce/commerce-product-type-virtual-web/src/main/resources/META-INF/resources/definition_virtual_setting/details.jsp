<%@ include file="/init.jsp" %>

<%--
/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%
CPDefinitionVirtualSettingDisplayContext cpDefinitionVirtualSettingDisplayContext = (CPDefinitionVirtualSettingDisplayContext)request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT);

CPDefinitionVirtualSetting cpDefinitionVirtualSetting = cpDefinitionVirtualSettingDisplayContext.getCPDefinitionVirtualSetting();
%>

<c:if test="<%= cpDefinitionVirtualSetting != null %>">

	<%
	long cpDefinitionId = -1;

	if (cpDefinitionVirtualSetting != null) {
		cpDefinitionId = cpDefinitionVirtualSetting.getClassPK();
	}

	Map<String, String> contextParams = HashMapBuilder.<String, String>put(
		"cpDefinitionId", String.valueOf(cpDefinitionId)
	).build();
	%>

	<%@ include file="/details.jspf" %>
</c:if>