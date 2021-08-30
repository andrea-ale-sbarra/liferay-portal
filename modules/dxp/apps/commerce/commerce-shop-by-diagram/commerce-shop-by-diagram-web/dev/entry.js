/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import {render} from '@liferay/frontend-js-react-web';

import Diagram from '../src/main/resources/META-INF/resources/js/Diagram';

import '../src/main/resources/META-INF/resources/css/diagram.scss';

render(
	Diagram,
	{
		componentId:null,
		enablePanZoom:true,
		enableResetZoom:true,
		imageSettings:{
			height:"500px",
			width:"100%",
		},
		imageURL: "/documents/42431/0/login_portlet_splash.jpg/02131025-6e20-a6ec-e7ee-98cc092dcf50?version=1.0&t=1630308326668&download=true",
		isAdmin:false,
		locale:{
			ISO3Country:"USA",
			ISO3Language:"eng",
			country:"US",
			displayCountry:"United States",
			displayLanguage:"English",
			displayName:"English (United States)",
			displayScript:"",
			displayVariant:"",
			extensionKeys:[
				
			],
			language:"en",
			script:"",
			unicodeLocaleAttributes:[
				
			],
			unicodeLocaleKeys:[
				
			],
			variant:""
		},
		pinsEndpoint:"/o/headless-commerce-admin-catalog/v1.0/",
		portletId:"com_liferay_commerce_product_content_web_internal_portlet_CPContentPortlet_INSTANCE_minium",
		portletNamespace:"_com_liferay_commerce_product_content_web_internal_portlet_CPContentPortlet_INSTANCE_minium_",
		productId:"44603",
		spritemap:"http://localhost:9000/o/minium-theme/images/clay/icons.svg",
	},
	document.getElementById('shop-by-diagram')
);
