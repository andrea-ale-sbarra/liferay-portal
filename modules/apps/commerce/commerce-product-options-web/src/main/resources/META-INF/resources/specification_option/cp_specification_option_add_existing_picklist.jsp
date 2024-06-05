<%--
/**
* SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
* SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
*/
--%>

<%@ include file="/init.jsp" %>

<%
long cpSpecificationOptionId = ParamUtil.getLong(request, "cpSpecificationOptionId");
String cpSpecificationOptionTitle = ParamUtil.getString(request, "cpSpecificationOptionTitle");
long listTypeDefinitionId = ParamUtil.getLong(request, "listTypeDefinitionId");
%>

<commerce-ui:modal-content
	submitButtonLabel='<%= LanguageUtil.get(request, "save") %>'
	title='<%= LanguageUtil.get(request, "add-an-existing-picklist") %>'
>
	<aui:form method="post" name="fm" onSubmit='<%= "event.preventDefault(); " + liferayPortletResponse.getNamespace() + "storeToParentForm(this.form);" %>' useNamespace="<%= false %>">
		<div class="px-4">
			<react:component module="{ListTypeDefinitionsAutocomplete} from commerce-product-options-web" />
		</div>
	</aui:form>

	<liferay-frontend:component
		context='<%=
			HashMapBuilder.<String, Object>put(
				"cmd", ParamUtil.getString(request, Constants.CMD)
			).put(
				"namespace", liferayPortletResponse.getNamespace()
			).put(
				"cpSpecificationOptionId", cpSpecificationOptionId
			).put(
				"cpSpecificationOptionTitle", cpSpecificationOptionTitle
			).put(
				"listTypeDefinitionId", listTypeDefinitionId
			).build()
		%>'
		module="{assignSpecificationOptionPicklist} from commerce-product-options-web"
	/>
</commerce-ui:modal-content>

