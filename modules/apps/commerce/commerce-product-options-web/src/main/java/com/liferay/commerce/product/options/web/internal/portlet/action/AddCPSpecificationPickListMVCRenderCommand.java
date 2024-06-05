package com.liferay.commerce.product.options.web.internal.portlet.action;

import com.liferay.commerce.product.constants.CPPortletKeys;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCRenderCommand;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ParamUtil;
import org.osgi.service.component.annotations.Component;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

@Component(
	property = {
		"javax.portlet.name=" + CPPortletKeys.CP_SPECIFICATION_OPTIONS,
		"mvc.command.name=/cp_specification_options/add_cp_specification_option_picklist"
	}
)
public class AddCPSpecificationPickListMVCRenderCommand implements
	MVCRenderCommand {
	@Override
	public String render(
		RenderRequest renderRequest, RenderResponse renderResponse)
		throws PortletException {

		String cmd = ParamUtil.getString(renderRequest, Constants.CMD);

		return cmd.equals(Constants.ADD)
			? "/specification_option/cp_specification_option_create_picklist.jsp"
			: "/specification_option/cp_specification_option_add_existing_picklist.jsp";
	}
}
