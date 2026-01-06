/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.ai.chat.bot.constants;

import java.util.Map;

/**
 * @author Danny Situ
 */
public class CommerceOrderConstants {

	public static final int ORDER_STATUS_AWAITING_PICKUP = 13;

	public static final int ORDER_STATUS_CANCELLED = 8;

	public static final int ORDER_STATUS_COMPLETED = 0;

	public static final int ORDER_STATUS_DECLINED = 16;

	public static final int ORDER_STATUS_DISPUTED = 18;

	public static final int ORDER_STATUS_IN_PROGRESS = 6;

	public static final int ORDER_STATUS_ON_HOLD = 20;

	public static final int ORDER_STATUS_OPEN = 2;

	public static final int ORDER_STATUS_PARTIALLY_REFUNDED = 19;

	public static final int ORDER_STATUS_PARTIALLY_SHIPPED = 14;

	public static final int ORDER_STATUS_PENDING = 1;

	public static final int ORDER_STATUS_PROCESSING = 10;

	public static final int ORDER_STATUS_QUOTE_PROCESSED = 22;

	public static final int ORDER_STATUS_QUOTE_REQUESTED = 21;

	public static final int ORDER_STATUS_REFUNDED = 17;

	public static final int ORDER_STATUS_SHIPPED = 15;

	public static final int ORDER_STATUS_SUBSCRIPTION = 9;

	public static Map<String, Integer> getOrderStatusMap() {
		return Map.ofEntries(
			Map.entry("awaiting pickup", ORDER_STATUS_AWAITING_PICKUP),
			Map.entry("cancelled", ORDER_STATUS_CANCELLED),
			Map.entry("completed", ORDER_STATUS_COMPLETED),
			Map.entry("declined", ORDER_STATUS_DECLINED),
			Map.entry("disputed", ORDER_STATUS_DISPUTED),
			Map.entry("in progress", ORDER_STATUS_IN_PROGRESS),
			Map.entry("on hold", ORDER_STATUS_ON_HOLD),
			Map.entry("open", ORDER_STATUS_OPEN),
			Map.entry("partially refunded", ORDER_STATUS_PARTIALLY_REFUNDED),
			Map.entry("partially shipped", ORDER_STATUS_PARTIALLY_SHIPPED),
			Map.entry("pending", ORDER_STATUS_PENDING),
			Map.entry("processing", ORDER_STATUS_PROCESSING),
			Map.entry("quote processed", ORDER_STATUS_QUOTE_PROCESSED),
			Map.entry("quote requested", ORDER_STATUS_QUOTE_REQUESTED),
			Map.entry("refunded", ORDER_STATUS_REFUNDED),
			Map.entry("shipped", ORDER_STATUS_SHIPPED),
			Map.entry("subscription", ORDER_STATUS_SUBSCRIPTION));
	}

}