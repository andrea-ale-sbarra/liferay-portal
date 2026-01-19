/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.util;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * @author Ivica Cardic
 */
public class SecurityUtils {

	public static String getEmail() {
		Jwt jwt = getJwt();

		if (jwt != null) {
			Map<String, Object> claims = jwt.getClaims();

			return (String)claims.get("username");
		}

		throw new IllegalStateException("No JWT found");
	}

	public static Jwt getJwt() {
		SecurityContext securityContext = SecurityContextHolder.getContext();

		Authentication authentication = securityContext.getAuthentication();

		if ((authentication != null) &&
			(authentication.getPrincipal() instanceof Jwt jwt)) {

			return jwt;
		}

		return null;
	}

}