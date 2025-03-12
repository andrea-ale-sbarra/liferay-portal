<%--
/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>

<%
CPConfigurationListDisplayContext cpConfigurationListDisplayContext = (CPConfigurationListDisplayContext)request.getAttribute(WebKeys.PORTLET_DISPLAY_CONTEXT);
%>

<aui:form cssClass="container-fluid" method="post" name="fm" onSubmit='<%= "event.preventDefault(); " + liferayPortletResponse.getNamespace() + "submitForm();" %>'>
	<div class="lfr-form-content">
		<aui:model-context bean="<%= cpConfigurationListDisplayContext.getCPConfigurationList() %>" model="<%= CPConfigurationList.class %>" />

		<label class="control-label" for="productId"><liferay-ui:message key="product" /></label>

		<div id="autocomplete-root"></div>

		<aui:button cssClass="hide" id="saveButton" type="submit" />
	</div>
</aui:form>

<liferay-frontend:component
	context="<%= cpConfigurationListDisplayContext.getContext() %>"
	module="{CPConfigurationEntryAutocomplete} from commerce-product-definitions-web"
/>