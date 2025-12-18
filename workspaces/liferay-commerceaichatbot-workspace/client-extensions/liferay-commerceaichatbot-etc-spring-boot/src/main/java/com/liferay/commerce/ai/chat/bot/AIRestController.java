/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
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

import java.util.ArrayList;
import java.util.List;

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
		FunctionTool searchAccountOrdersByDateRangeTool = FunctionTool.create(
			_commerceTools, "searchAccountOrdersByDateRangeTool");
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
		FunctionTool getCustomerOrdersByAccountTool = FunctionTool.create(
			_commerceTools, "getCustomerOrdersByAccountTool");
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
		FunctionTool getCurrentDateTool = FunctionTool.create(
			_commerceTools, "getCurrentDateTool");

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
			listChannelsAccountsTool, findOrderTool,
			searchAccountOrdersByDateRangeTool, searchOrdersTool,
			searchOrdersByDateRangeTool, searchOrdersByProductTool,
			searchOrdersByStatusTool, searchOrdersByShippingAddressTool,
			getCurrentDateTool, getCustomerOrdersTool,
			getCustomerOrdersByAccountTool, getTestEmailsTool,
			getOrderItemsTool, getOrderShippingTool,
			getAllAccountsOrderSummaryTool, getFaqInformationTool
		).build();

		InMemoryRunner runner = new InMemoryRunner(rootAgent);

		Session session = runner.sessionService(
		).createSession(
			runner.appName(), jwt.getTokenValue()
		).blockingGet();

		Content userMsg = Content.fromParts(
			Part.fromText(jsonObject.getString("question")));

		List<Event> events = runner.runAsync(
			session.userId(), session.id(), userMsg
		).toList(
		).blockingGet();

		jsonObject = new JSONObject();

		List<String> output = new ArrayList<>();

		for (Event event : events) {
			Content content = event.content(
			).orElseThrow();

			output.add(content.text());
		}

		StringBuilder sb = new StringBuilder();

		for (String s : output) {
			sb.append(s);
		}

		jsonObject.put("output", sb.toString());

		return new ResponseEntity<>(jsonObject.toString(), HttpStatus.OK);
	}

	private static final String _INSTRUCTION = new StringBuilder(
	).append(
		"You are a professional userAccount service representative for a "
	).append(
		"Liferay Commerce platform.\s Your primary role is to help customers "
	).append(
		"find information about their orders.\s\n\n **Key "
	).append(
		"Responsibilities:**\n - Help customers locate their orders using "
	).append(
		"order numbers, email addresses, or names\n- Provide detailed order "
	).append(
		"information including status, items, and pricing\n- Answer questions "
	).append(
		"about order status and delivery\n- Assist with customer account "
	).append(
		"inquiries\n\n **Available Tools:**\n- handle_customer_inquiry: "
	).append(
		"Handles all order-related inquiries with a single unified tool\n- "
	).append(
		"Google Search: For general customer service information and "
	).append(
		"troubleshooting\n\n**Best Practices:**\n- Always greet customers"
	).append(
		"warmly and professionally\n- Ask clarifying questions if you need "
	).append(
		"more information\n- Provide clear, organized responses with order "
	).append(
		"details\n- Be empathetic and helpful, especially when orders can not "
	).append(
		"be found\n- Offer to help with additional questions or concerns\n\n"
	).append(
		"**Example Interactions:**\n- Customer: 'I need to check my order "
	).append(
		"status'\n- You: 'I am happy to help you check your order status. "
	).append(
		"Could you\n please provide your order number or the email address "
	).append(
		"you used when\n  placing the order?'\n\nStart by greeting the "
	).append(
		"customer and asking how you can help them today.\n\n"
	).append(
		"When asked for orders if number of orders is not specified, display "
	).append(
		"at most 5 orders by default."
	).append(
		"When asked about orders and no date or year is provided use the " +
			"getCurrentDateTool to calculate the required date or year."
	).toString();

	@Autowired
	private CommerceTools _commerceTools;

	@Autowired
	private SettingsService _settingsService;

}