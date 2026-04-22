/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useIsMounted} from '@liferay/frontend-js-react-web';
import {useCallback, useEffect, useState} from 'react';

import {
	AnalyticsFilters,
	IAnalyticsUserFilter,
	TAnalyticsFilter,
	TDateRangeAnalyticsFilterValue,
	TRoomAnalyticsFilterValue,
} from '../../main_view/analytics/types';
import AnalyticsService from '../services/AnalyticsService';
import useAnalyticsFilters from './useAnalyticsFilters';
import useIsInViewport from './useIsInViewport';

function toRequestParams(
	filters: TAnalyticsFilter,
	variables: Record<string, unknown>
) {
	const roomFilterValue = filters[AnalyticsFilters.ROOM]
		.value as TRoomAnalyticsFilterValue;
	const dateRangeFilterValue = filters[AnalyticsFilters.DATE_RANGE]
		.value as TDateRangeAnalyticsFilterValue;
	const userFilter = filters[AnalyticsFilters.USER] as IAnalyticsUserFilter;

	return {
		...variables,
		channelId: roomFilterValue.channelId || variables.channelId,
		emailAddresses: userFilter.value,
		rangeEnd: dateRangeFilterValue.to,
		rangeStart: dateRangeFilterValue.from,
	};
}

export default function useAnalyticsQuery({
	element,
	query,
	settings = {checkViewportVisibility: true, useDevEnvData: true},
	variables,
}: {
	element: HTMLElement | null;
	query: {devEnvData: any; path: string};
	settings?: {
		checkViewportVisibility: boolean;
		useDevEnvData: boolean;
	};
	variables: Record<string, unknown>;
}) {
	const [isLoading, setIsLoading] = useState(true);
	const isMounted = useIsMounted();
	const isVisible = useIsInViewport(element);

	const [filters] = useAnalyticsFilters(null);
	const [response, setResponse] = useState(null);

	const sendRequest = useCallback(
		async (filters: TAnalyticsFilter) => {
			setIsLoading(true);

			if (settings.checkViewportVisibility && isVisible) {
				if (settings.useDevEnvData) {
					setResponse(query.devEnvData);

					setIsLoading(false);

					return;
				}

				try {
					const result = await AnalyticsService.get(
						query.path,
						toRequestParams(filters, variables)
					);

					setResponse(result as any);
				}
				catch (_ignore) {
					setResponse(null);
				}

				setIsLoading(false);
			}
		},
		[isVisible, query, setResponse, settings, variables]
	);

	useEffect(() => {
		if (isVisible && !response) {
			sendRequest(filters as TAnalyticsFilter);
		}
	}, [filters, isVisible, response, sendRequest]);

	useEffect(() => {
		const handleFiltersUpdate = () => {
			setIsLoading(true);
			setResponse(null);
		};

		if (isMounted()) {
			Liferay.on('dsr-filters-updated', handleFiltersUpdate);
		}

		return () => {
			if (isMounted()) {
				Liferay.detach('dsr-filters-updated', handleFiltersUpdate);
			}
		};
	}, [isMounted, sendRequest]);

	return {isLoading, response, sendRequest};
}
