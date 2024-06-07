/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.commerce.constants;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;

/**
 * @author Crescenzo Rega
 */
public class CommerceReturnConstants {

	public static final String RETURN_STATUS_AUTHORIZED = "authorized";

	public static final String RETURN_STATUS_CANCELLED = "cancelled";

	public static final String RETURN_STATUS_COMPLETED = "completed";

	public static final String RETURN_STATUS_DRAFT = "draft";

	public static final String RETURN_STATUS_ITEM_ACCEPTED = "accepted";

	public static final String RETURN_STATUS_ITEM_AUTHORIZED = "authorized";

	public static final String RETURN_STATUS_ITEM_DEFINED = "defined";

	public static final String RETURN_STATUS_ITEM_NOT_ACCEPTED = "notAccepted";

	public static final String RETURN_STATUS_ITEM_NOT_AUTHORIZED =
		"notAuthorized";

	public static final String RETURN_STATUS_ITEM_PARTIALLY_ACCEPTED =
		"partiallyAccepted";

	public static final String RETURN_STATUS_ITEM_PARTIALLY_AUTHORIZED =
		"partiallyAuthorized";

	public static final String RETURN_STATUS_ITEM_TO_BE_ACCEPTED =
		"toBeAccepted";

	public static final String RETURN_STATUS_ITEM_TO_BE_AUTHORIZED =
		"toBeAuthorized";

	public static final String RETURN_STATUS_ITEM_TO_BE_DEFINED = "toBeDefined";

	public static final String RETURN_STATUS_ITEM_TO_BE_PROCESSED =
		"toBeProcessed";

	public static final String RETURN_STATUS_PENDING = "pending";

	public static final String RETURN_STATUS_PROCESSING = "processing";

	public static final String RETURN_STATUS_REJECTED = "rejected";

	public static String getReturnStatusLabelStyle(String returnStatus) {
		if (StringUtil.equals(returnStatus, RETURN_STATUS_AUTHORIZED) ||
			StringUtil.equals(returnStatus, RETURN_STATUS_DRAFT)) {

			return "secondary";
		}
		else if (StringUtil.equals(returnStatus, RETURN_STATUS_CANCELLED) ||
				 StringUtil.equals(returnStatus, RETURN_STATUS_REJECTED)) {

			return "danger";
		}
		else if (StringUtil.equals(returnStatus, RETURN_STATUS_COMPLETED)) {
			return "success";
		}
		else if (StringUtil.equals(returnStatus, RETURN_STATUS_PENDING)) {
			return "warning";
		}
		else if (StringUtil.equals(returnStatus, RETURN_STATUS_PROCESSING)) {
			return "info";
		}

		return StringPool.BLANK;
	}

}