/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URI;

import java.nio.charset.StandardCharsets;

import java.util.Base64;
import java.util.Map;

import javax.annotation.PostConstruct;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * @author Ivica Cardic
 */
@Component
public class HttpClient {

	public JSONObject delete(String path) throws Exception {
		return execute(new HttpDelete(_buildUri(path, null)));
	}

	public JSONObject execute(HttpRequestBase httpRequestBase)
		throws Exception {

		httpRequestBase.addHeader(
			HttpHeaders.USER_AGENT, "Liferay-Customer-Service-Agent/1.0");
		httpRequestBase.addHeader(HttpHeaders.ACCEPT, "application/json");
		httpRequestBase.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

		if (_basicAuthHeader != null) {
			httpRequestBase.addHeader(
				HttpHeaders.AUTHORIZATION, _basicAuthHeader);
		}
		else {
			SecurityContext securityContext =
				SecurityContextHolder.getContext();

			Authentication authentication = securityContext.getAuthentication();

			if ((authentication != null) &&
				(authentication.getPrincipal() instanceof Jwt)) {

				Jwt jwt = (Jwt)authentication.getPrincipal();

				httpRequestBase.addHeader(
					HttpHeaders.AUTHORIZATION, "Bearer " + jwt.getTokenValue());
			}
		}

		try (CloseableHttpResponse response = _closeableHttpClient.execute(
				httpRequestBase)) {

			int status = response.getStatusLine(
			).getStatusCode();

			try (BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(
						response.getEntity(
						).getContent(),
						StandardCharsets.UTF_8))) {

				StringBuilder sb = new StringBuilder();

				String line;

				while ((line = bufferedReader.readLine()) != null) {
					sb.append(line);
				}

				String body = sb.toString();

				if ((status >= 200) && (status < 300)) {
					if (StringUtils.isEmpty(body)) {
						return new JSONObject();
					}

					return new JSONObject(body);
				}

				if (_log.isWarnEnabled()) {
					_log.warn(
						new StringBuilder(
						).append(
							httpRequestBase.getMethod()
						).append(
							" "
						).append(
							httpRequestBase.getURI()
						).append(
							" -> "
						).append(
							status
						).append(
							"body: "
						).append(
							_truncate(body)
						).toString());
				}

				return null;
			}
		}
	}

	public JSONObject get(String path) throws Exception {
		return get(path, null);
	}

	public JSONObject get(String path, Map<String, String> params)
		throws Exception {

		return execute(new HttpGet(_buildUri(path, params)));
	}

	@PostConstruct
	public void init() {
		if (!StringUtils.isBlank(_userName) &&
			!StringUtils.isBlank(_password)) {

			_basicAuthHeader = _buildBasicAuthHeader(_userName, _password);
		}

		_closeableHttpClient = _buildHttpClient(_sslVerify, _timeoutMs);

		if (_log.isInfoEnabled()) {
			_log.info(
				new StringBuilder(
				).append(
					"Liferay CommerceClient initialized. baseUrl="
				).append(
					_baseUrl
				).append(
					"sslVerify="
				).append(
					_sslVerify
				).append(
					" timeoutMs="
				).append(
					_timeoutMs
				).toString());
		}
	}

	public JSONObject patch(String path, JSONObject jsonObject)
		throws Exception {

		HttpPatch patch = new HttpPatch(_buildUri(path, null));

		patch.setEntity(new StringEntity(jsonObject.toString()));

		return execute(patch);
	}

	public JSONObject post(String path, JSONObject jsonObject)
		throws Exception {

		HttpPost post = new HttpPost(_buildUri(path, null));

		post.setEntity(new StringEntity(jsonObject.toString()));

		return execute(post);
	}

	public JSONObject put(String path, JSONObject jsonObject) throws Exception {
		HttpPut put = new HttpPut(_buildUri(path, null));

		put.setEntity(new StringEntity(jsonObject.toString()));

		return execute(put);
	}

	private String _buildBasicAuthHeader(String user, String pass) {
		Base64.Encoder encoder = Base64.getEncoder();

		String credentials = user + ":" + pass;

		String token = encoder.encodeToString(
			credentials.getBytes(StandardCharsets.UTF_8));

		return "Basic " + token;
	}

	private CloseableHttpClient _buildHttpClient(
		boolean verifySsl, int timeoutMs) {

		RequestConfig requestConfig = RequestConfig.custom(
		).setConnectTimeout(
			timeoutMs
		).setConnectionRequestTimeout(
			timeoutMs
		).setSocketTimeout(
			timeoutMs
		).build();

		if (verifySsl) {
			return HttpClients.custom(
			).setDefaultRequestConfig(
				requestConfig
			).build();
		}

		try {
			SSLContext sslContext = SSLContexts.custom(
			).loadTrustMaterial(
				null, (chain, authType) -> true
			).build();

			SSLConnectionSocketFactory sslConnectionSocketFactory =
				new SSLConnectionSocketFactory(
					sslContext, NoopHostnameVerifier.INSTANCE);

			return HttpClients.custom(
			).setSSLSocketFactory(
				sslConnectionSocketFactory
			).setDefaultRequestConfig(
				requestConfig
			).build();
		}
		catch (Exception exception) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Failed to create insecure SSL context, falling back to " +
						"default client",
					exception);
			}

			return HttpClients.custom(
			).setDefaultRequestConfig(
				requestConfig
			).build();
		}
	}

	private URI _buildUri(String path, Map<String, String> params)
		throws Exception {

		String trimmedBase = _baseUrl;

		if (StringUtils.endsWith(_baseUrl, "/")) {
			trimmedBase = StringUtils.substring(
				_baseUrl, 0, StringUtils.length(_baseUrl) - 1);
		}

		String trimmedPath = path;

		if (!StringUtils.startsWith(path, "/")) {
			trimmedPath = "/" + path;
		}

		URIBuilder builder = new URIBuilder(trimmedBase + trimmedPath);

		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				if (entry.getValue() != null) {
					builder.addParameter(entry.getKey(), entry.getValue());
				}
			}
		}

		return builder.build();
	}

	private String _truncate(String s) {
		if (s == null) {
			return null;
		}

		if (StringUtils.length(s) > 500) {
			return StringUtils.substring(s, 0, 500) + "...";
		}

		return s;
	}

	private static final Log _log = LogFactory.getLog(HttpClient.class);

	@Value(
		"${liferay.base.url:${LIFERAY_BASE_URL:https://webserver-lct66degrees-uat.lfr.cloud/}}"
	)
	private String _baseUrl;

	private String _basicAuthHeader;
	private CloseableHttpClient _closeableHttpClient;

	@Value("${liferay.password:${LIFERAY_PASSWORD:''}}")
	private String _password;

	@Value("${liferay.ssl.verify:${LIFERAY_SSL_VERIFY:true}}")
	private boolean _sslVerify;

	@Value("${liferay.timeout.ms:${LIFERAY_TIMEOUT:30000}}")
	private int _timeoutMs;

	@Value("${liferay.username:${LIFERAY_USERNAME:''}}")
	private String _userName;

}