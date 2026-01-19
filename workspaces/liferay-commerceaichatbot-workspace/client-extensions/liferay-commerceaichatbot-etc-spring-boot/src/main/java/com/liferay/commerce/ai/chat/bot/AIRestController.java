/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.models.Gemini;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.BaseSessionService;
import com.google.adk.sessions.Session;
import com.google.adk.tools.FunctionTool;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import com.liferay.client.extension.util.spring.boot3.BaseRestController;
import com.liferay.commerce.ai.chat.bot.model.Settings;
import com.liferay.commerce.ai.chat.bot.service.SettingsService;
import com.liferay.commerce.ai.chat.bot.tools.CommerceTools;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StreamUtils;
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

	public AIRestController(
		CommerceTools commerceTools, SettingsService settingsService) {

		_commerceTools = commerceTools;
		_settingsService = settingsService;
	}

	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/chat")
	public ResponseEntity<String> chat(
			@AuthenticationPrincipal Jwt jwt, @RequestBody String json)
		throws Exception {

		Settings settings = _settingsService.getActiveSettings(jwt);

		if (settings == null) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		JSONObject jsonObject = new JSONObject(json);

		String token = jwt.getTokenValue();

		Runner runner = _runners.get(
			token,
			key -> {

				// Register CommerceTools methods as FunctionTools

				FunctionTool listChannelsAccountsTool = FunctionTool.create(
					_commerceTools, "listAvailableChannelsAndAccountsTool");
				FunctionTool findOrderTool = FunctionTool.create(
					_commerceTools, "findOrderTool");
				FunctionTool searchAccountOrdersByDateRangeTool =
					FunctionTool.create(
						_commerceTools, "searchAccountOrdersByDateRangeTool");
				FunctionTool searchOrdersTool = FunctionTool.create(
					_commerceTools, "searchOrdersTool");
				FunctionTool searchOrdersByDateRangeTool = FunctionTool.create(
					_commerceTools, "searchOrdersByDateRangeTool");
				FunctionTool searchOrdersByProductTool = FunctionTool.create(
					_commerceTools, "searchOrdersByProductTool");
				FunctionTool searchOrdersByStatusTool = FunctionTool.create(
					_commerceTools, "searchOrdersByStatusTool");
				FunctionTool searchOrdersByShippingAddressTool =
					FunctionTool.create(
						_commerceTools, "searchOrdersByShippingAddressTool");
				FunctionTool getCustomerOrdersByAccountTool =
					FunctionTool.create(
						_commerceTools, "getCustomerOrdersByAccountTool");
				FunctionTool getCustomerOrdersTool = FunctionTool.create(
					_commerceTools, "getCustomerOrdersTool");
				FunctionTool getOrderItemsTool = FunctionTool.create(
					_commerceTools, "getOrderItemsTool");
				FunctionTool getOrderShippingTool = FunctionTool.create(
					_commerceTools, "getOrderShippingTool");
				FunctionTool getAllAccountsOrderSummaryTool =
					FunctionTool.create(
						_commerceTools, "getAllAccountsOrderSummaryTool");
				FunctionTool getFaqInformationTool = FunctionTool.create(
					_commerceTools, "getFaqInformationTool");
				FunctionTool getCurrentDateTool = FunctionTool.create(
					_commerceTools, "getCurrentDateTool");

				LlmAgent rootAgent = LlmAgent.builder(
				).name(
					"commerce_assistant"
				).description(
					"A professional customer service representative for a " +
						"Liferay Commerce platform."
				).model(
					new Gemini(settings.modelName, settings.apiKey)
				).instruction(
					_getInstruction()
				).tools(
					listChannelsAccountsTool, findOrderTool,
					searchAccountOrdersByDateRangeTool, searchOrdersTool,
					searchOrdersByDateRangeTool, searchOrdersByProductTool,
					searchOrdersByStatusTool, searchOrdersByShippingAddressTool,
					getCurrentDateTool, getCustomerOrdersTool,
					getCustomerOrdersByAccountTool, getOrderItemsTool,
					getOrderShippingTool, getAllAccountsOrderSummaryTool,
					getFaqInformationTool
				).build();

				return new InMemoryRunner(rootAgent);
			});

		Session session = null;
		String sessionId = _sessionIds.get(jwt.getTokenValue(), s -> null);
		BaseSessionService sessionService = runner.sessionService();

		if (sessionId == null) {
			session = sessionService.createSession(
				runner.appName(), jwt.getTokenValue()
			).blockingGet();

			_sessionIds.put(jwt.getTokenValue(), session.id());
		}
		else {
			session = sessionService.getSession(
				runner.appName(), jwt.getTokenValue(), sessionId,
				Optional.empty()
			).blockingGet();
		}

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

			String text = content.text();

			if (text != null) {
				output.add(text);
			}
		}

		StringBuilder sb = new StringBuilder();

		for (String s : output) {
			sb.append(s);
		}

		jsonObject.put("output", sb.toString());

		return new ResponseEntity<>(jsonObject.toString(), HttpStatus.OK);
	}

	private String _getInstruction() {
		if (_instruction != null) {
			return _instruction;
		}

		try (InputStream inputStream = _instructionResource.getInputStream()) {
			_instruction = StreamUtils.copyToString(
				inputStream, StandardCharsets.UTF_8);

			return _instruction;
		}
		catch (IOException ioException) {
			throw new RuntimeException(ioException);
		}
	}

	private final CommerceTools _commerceTools;
	private String _instruction;

	@Value("classpath:instruction.txt")
	private Resource _instructionResource;

	private final Cache<String, Runner> _runners = Caffeine.newBuilder(
	).expireAfterAccess(
		30, TimeUnit.MINUTES
	).build();
	private final Cache<String, String> _sessionIds = Caffeine.newBuilder(
	).expireAfterAccess(
		30, TimeUnit.MINUTES
	).build();
	private final SettingsService _settingsService;

}