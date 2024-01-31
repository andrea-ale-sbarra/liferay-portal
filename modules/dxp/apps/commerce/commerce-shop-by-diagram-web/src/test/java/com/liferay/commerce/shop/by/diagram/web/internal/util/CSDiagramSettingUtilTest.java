/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.shop.by.diagram.web.internal.util;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Andrea Sbarra
 */
public class CSDiagramSettingUtilTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testCleanInputStream() throws IOException {
		String maliciousSVG = StringBundler.concat(
			"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"400\"",
			"height=\"110\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">",
			"<a xlink:href=\"javascript:alert(1)\">",
			"<rect width=\"300\" height=\"100\" style=\"fill:rgb(0,0,255);",
			"stroke-width:3;stroke:rgb(0,0,0)\" /></a></svg>");

		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(
					CSDiagramSettingUtil.cleanInputStream(
						new ByteArrayInputStream(
							maliciousSVG.getBytes(StandardCharsets.UTF_8))),
					StandardCharsets.UTF_8))) {

			String line;

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(
					line
				).append(
					StringPool.NEW_LINE
				);
			}
		}

		Assert.assertTrue(
			"Malicious code is still present",
			StringUtil.contains(stringBuilder.toString(), "alert"));
	}

}