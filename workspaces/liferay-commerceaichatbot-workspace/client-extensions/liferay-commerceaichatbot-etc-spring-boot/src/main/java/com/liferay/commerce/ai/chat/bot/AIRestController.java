/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot;

import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.FunctionTool;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import com.liferay.client.extension.util.spring.boot3.BaseRestController;
import com.liferay.commerce.ai.chat.bot.model.Settings;
import com.liferay.commerce.ai.chat.bot.service.SettingsService;
import com.liferay.commerce.ai.chat.bot.tools.CommerceTools;
import com.liferay.portal.kernel.util.StringBundler;

import java.util.stream.Collectors;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ivica Cardic
 */
@RequestMapping("/commerce/ai")
@RestController
public class AIRestController extends BaseRestController {

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/chat")
	public ResponseEntity<String> chat(
			@AuthenticationPrincipal Jwt jwt, @RequestBody String json)
		throws Exception {

		Settings settings = _settingsService.getActiveSettings(jwt);

		if (settings == null) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		JSONObject jsonObject = new JSONObject(json);

		// Register CommerceTools methods as FunctionTools

		FunctionTool listChannelsAccountsTool = FunctionTool.create(
			_commerceTools, "listAvailableChannelsAndAccountsTool");
		FunctionTool findOrderTool = FunctionTool.create(
			_commerceTools, "findOrderTool");
		FunctionTool searchOrdersTool = FunctionTool.create(
			_commerceTools, "searchOrdersTool");
		FunctionTool searchOrdersByDateRangeTool = FunctionTool.create(
			_commerceTools, "searchOrdersByDateRangeTool");
		FunctionTool searchOrdersByProductTool = FunctionTool.create(
			_commerceTools, "searchOrdersByProductTool");
		FunctionTool searchOrdersByStatusTool = FunctionTool.create(
			_commerceTools, "searchOrdersByStatusTool");
		FunctionTool searchOrdersByShippingAddressTool = FunctionTool.create(
			_commerceTools, "searchOrdersByShippingAddressTool");
		FunctionTool getCustomerOrdersTool = FunctionTool.create(
			_commerceTools, "getCustomerOrdersTool");
		FunctionTool getTestEmailsTool = FunctionTool.create(
			_commerceTools, "getTestEmailsTool");
		FunctionTool getOrderItemsTool = FunctionTool.create(
			_commerceTools, "getOrderItemsTool");
		FunctionTool getOrderShippingTool = FunctionTool.create(
			_commerceTools, "getOrderShippingTool");
		FunctionTool getAllAccountsOrderSummaryTool = FunctionTool.create(
			_commerceTools, "getAllAccountsOrderSummaryTool");
		FunctionTool getFaqInformationTool = FunctionTool.create(
			_commerceTools, "getFaqInformationTool");

		LlmAgent rootAgent = LlmAgent.builder(
		).name(
			"commerce_assistant"
		).description(
			"A professional customer service representative for a Liferay " +
				"Commerce platform."
		).model(
			new Gemini(settings.modelName, settings.apiKey)
		).instruction(
			_INSTRUCTION
		).tools(
			listChannelsAccountsTool, findOrderTool, searchOrdersTool,
			searchOrdersByDateRangeTool, searchOrdersByProductTool,
			searchOrdersByStatusTool, searchOrdersByShippingAddressTool,
			getCustomerOrdersTool, getTestEmailsTool, getOrderItemsTool,
			getOrderShippingTool, getAllAccountsOrderSummaryTool,
			getFaqInformationTool
		).build();

		InMemoryRunner runner = new InMemoryRunner(rootAgent);

		Session session = runner.sessionService(
		).createSession(
			runner.appName(), jwt.getTokenValue()
		).blockingGet();

		Content userMsg = Content.fromParts(
			Part.fromText(jsonObject.getString("question")));

		String output = runner.runAsync(
			session.userId(), session.id(), userMsg
		).toList(
		).blockingGet(
		).stream(
		).map(
			event -> event.content(
			).orElseThrow(
			).text()
		).collect(
			Collectors.joining()
		);

		return new ResponseEntity<>(
			new JSONObject(
			).put(
				"output", output
			).toString(),
			HttpStatus.OK);
	}

	private static final String _INSTRUCTION = StringBundler.concat(
		"You are a professional userAccount service representative for a ",
		"Liferay Commerce platform.\s Your primary role is to help customers ",
		"find information about their orders.\s\n\n **Key ",
		"Responsibilities:**\n - Help customers locate their orders using ",
		"order numbers, email addresses, or names\n- Provide detailed order ",
		"information including status, items, and pricing\n- Answer questions ",
		"about order status and delivery\n- Assist with customer account ",
		"inquiries\n\n **Available Tools:**\n- handle_customer_inquiry: ",
		"Handles all order-related inquiries with a single unified tool\n- ",
		"Google Search: For general customer service information and ",
		"troubleshooting\n\n**Best Practices:**\n- Always greet customers",
		"warmly and professionally\n- Ask clarifying questions if you need ",
		"more information\n- Provide clear, organized responses with order ",
		"details\n- Be empathetic and helpful, especically when orders can ",
		"not be found\n- Offer to help with additional questions or concerns\n",
		"\n**Example Interactions:**\n- Customer: 'I need to check my order ",
		"status'\n- You: 'I am happy to help you check your order status. ",
		"Could you\n please provide your order number or the email address ",
		"you used when\n  placing the order?'\n\nStart by greeting the ",
		"customer and asking how you can help them today.");

	@Autowired
	private CommerceTools _commerceTools;

	@Autowired
	private SettingsService _settingsService;

}