<%--
/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
ViewPIMConnectorsDisplayContext viewPIMConnectorsDisplayContext = (ViewPIMConnectorsDisplayContext)request.getAttribute(ViewPIMConnectorsDisplayContext.class.getName());
%>

<div class="cms-section custom-empty-state">
	<frontend-data-set:headless-display
		apiURL="<%= viewPIMConnectorsDisplayContext.getAPIURL() %>"
		creationMenu="<%= viewPIMConnectorsDisplayContext.getCreationMenu() %>"
		emptyState="<%= viewPIMConnectorsDisplayContext.getEmptyState() %>"
		fdsActionDropdownItems="<%= viewPIMConnectorsDisplayContext.getFDSActionDropdownItems() %>"
		formName="fm"
		id="<%= PIMFDSNames.PIM_CONNECTOR %>"
		itemsPerPage="<%= 20 %>"
		selectedItemsKey="embedded.id"
	/>
</div>