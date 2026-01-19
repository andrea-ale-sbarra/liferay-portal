package com.liferay.commerce.ai.chat.bot.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

/**
 * @author Ivica Cardic
 */
public class SecurityUtils {

	public static String getUsername() {
		Jwt jwt = getJwt();

		if (jwt != null) {
			Map<String, Object> claims = jwt.getClaims();

			return (String) claims.get("username");
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
