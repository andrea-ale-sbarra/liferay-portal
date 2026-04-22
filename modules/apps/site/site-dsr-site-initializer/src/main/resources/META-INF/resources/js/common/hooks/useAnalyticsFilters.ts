/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {useCallback, useEffect, useRef, useState} from 'react';

import {TAnalyticsFilter} from '../../main_view/analytics/types';
import {toFilters, toStoredFilters} from '../../main_view/analytics/utils';
import AnalyticsService from '../services/AnalyticsService';

export default function useAnalyticsFilters(
	filtersJSONString: string | null,
	persisted: boolean = false
) {
	const [filters, setFilters] = useState<TAnalyticsFilter>(
		toFilters(filtersJSONString)
	);
	const skipFireRef = useRef(true);

	const setFilter = useCallback(
		(filter: TAnalyticsFilter) => {
			setFilters((filters: TAnalyticsFilter) => {
				const filterName = Object.keys(
					filter
				)[0] as keyof TAnalyticsFilter;

				const updatedFilterValue = JSON.stringify(filter[filterName]);
				const filterValue = JSON.stringify(filters[filterName]);

				if (filterValue !== updatedFilterValue) {
					skipFireRef.current = false;

					return {
						...filters,
						[filterName]: filter[filterName],
					};
				}

				return filters;
			});
		},
		[setFilters]
	);

	useEffect(() => {
		if (skipFireRef.current) {
			skipFireRef.current = true;

			return;
		}

		const shouldStore = persisted
			? () => AnalyticsService.storeFilters(toStoredFilters(filters))
			: () => Promise.resolve();

		shouldStore().then(() => {
			Liferay.fire('dsr-filters-updated', {filters});
		});
	}, [filters, persisted]);

	useEffect(() => {
		const handleFiltersUpdate = ({
			filters: incoming,
		}: {
			filters: TAnalyticsFilter;
		}) => {
			setFilters((current) => {
				if (JSON.stringify(current) === JSON.stringify(incoming)) {
					return current;
				}

				skipFireRef.current = true;

				return incoming;
			});
		};

		Liferay.on('dsr-filters-updated', handleFiltersUpdate);

		return () => {
			Liferay.detach('dsr-filters-updated', handleFiltersUpdate);
		};
	}, []);

	return [filters, setFilter] as const;
}
