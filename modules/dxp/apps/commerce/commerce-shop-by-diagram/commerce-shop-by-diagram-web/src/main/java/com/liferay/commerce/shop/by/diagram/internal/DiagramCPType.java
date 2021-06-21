package com.liferay.commerce.shop.by.diagram.internal;
import com.liferay.commerce.product.type.CPType;
import com.liferay.commerce.product.type.simple.constants.SimpleCPTypeConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import org.osgi.service.component.annotations.Component;

import java.util.Locale;

@Component(
	immediate = true,
	property = {
		"commerce.product.type.display.order:Integer=5",
		"commerce.product.type.name=diagram"
	},
	service = CPType.class
)
public class DiagramCPType implements CPType {
	@Override
	public void deleteCPDefinition(long cpDefinitionId) throws PortalException {
	}

	@Override
	public String getLabel(Locale locale) {
		return "diagram";
	}

	@Override
	public String getName() {
		return "diagram";
	}


}
