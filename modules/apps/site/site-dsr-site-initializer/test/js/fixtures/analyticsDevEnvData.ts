/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

export const activityLogDevEnvData = {
	totalEvents: 8,
	userSessions: [
		{
			userSessionEvents: [
				{
					createDate: '2026-03-04T14:38:26Z',
					emailAddressHashed: 'John Doe',
					name: 'view',
				},
				{
					createDate: '2026-03-04T19:13:24Z',
					emailAddressHashed: 'John Doe',
					name: 'comment',
				},
				{
					createDate: '2026-03-04T23:08:11Z',
					emailAddressHashed: 'John Doe',
					name: 'upload',
				},
			],
		},
		{
			userSessionEvents: [
				{
					createDate: '2026-03-05T11:09:28Z',
					emailAddressHashed: 'Paul Gerome',
					name: 'comment',
				},
				{
					createDate: '2026-03-05T19:17:29Z',
					emailAddressHashed: 'Paul Gerome',
					name: 'comment',
				},
				{
					createDate: '2026-03-06T12:12:29Z',
					emailAddressHashed: 'Paul Gerome',
					name: 'comment',
				},
			],
		},
		{
			userSessionEvents: [
				{
					createDate: '2026-03-05T13:11:00Z',
					emailAddressHashed: 'Emily Blunt',
					name: 'comment',
				},
				{
					createDate: '2026-03-06T00:16:14Z',
					emailAddressHashed: 'Emily Blunt',
					name: 'comment',
				},
			],
		},
	],
};

export const engagementChartDevEnvData = {
	histogram: {
		asymmetricComparison: 0,
		histogramMetrics: [
			{
				key: '2026-02-20T00:00:00Z',
				value: 400,
				valueKey: 'numberOfVisits',
			},
			{
				key: '2026-02-21T00:00:00Z',
				value: 300,
				valueKey: 'numberOfVisits',
			},
			{
				key: '2026-02-22T00:00:00Z',
				value: 320,
				valueKey: 'numberOfVisits',
			},
			{
				key: '2026-02-23T00:00:00Z',
				value: 200,
				valueKey: 'numberOfVisits',
			},
			{
				key: '2026-02-24T00:00:00Z',
				value: 278,
				valueKey: 'numberOfVisits',
			},
			{
				key: '2026-02-25T00:00:00Z',
				value: 189,
				valueKey: 'numberOfVisits',
			},
			{
				key: '2026-02-26T00:00:00Z',
				value: 199,
				valueKey: 'numberOfVisits',
			},
		],
		total: 1886,
	},
};

export const frequencyChartDevEnvData = {
	totalCount: 650,
	visitFrequencyItems: [
		{count: 320, name: 'DAILY'},
		{count: 30, name: 'WEEKLY'},
		{count: 100, name: 'BIWEEKLY'},
		{count: 200, name: 'MONTHLY'},
	],
};

export const latestActivityDevEnvData = {
	eventEntries: [
		{
			createDate: '2026-03-26T14:30:00Z',
			emailAddressHashed: 'John Doe',
			name: 'Created a new document',
		},
	],
};

export const mostActiveVisitorsDevEnvData = {
	mostActiveVisitors: [
		{
			activitiesCount: 150,
			emailAddress: 'john.doe@liferay.com',
			firstName: 'John',
			id: '1',
			lastName: 'Doe',
		},
	],
	total: 1,
};

export const roomDocumentsStatisticsDevEnvData = {
	documentMetrics: [
		{
			assetId: '1',
			assetTitle: 'pdf_test',
			commentsMetric: {value: 0},
			downloadsMetric: {value: 324},
			impressionMadeMetric: {value: 89},
			lastViewedMetric: {value: 1740929400000},
			ratingsMetric: {value: 0},
			urls: ['pdf_test.pdf'],
			usersInvolvedMetric: {value: 4},
		},
		{
			assetId: '2',
			assetTitle: 'doc_test2',
			commentsMetric: {value: 0},
			downloadsMetric: {value: 342},
			impressionMadeMetric: {value: 34},
			lastViewedMetric: {value: 1741015800000},
			ratingsMetric: {value: 0},
			urls: ['doc_test2.docx'],
			usersInvolvedMetric: {value: 3},
		},
		{
			assetId: '3',
			assetTitle: 'pdf_test2',
			commentsMetric: {value: 0},
			downloadsMetric: {value: 45},
			impressionMadeMetric: {value: 34},
			lastViewedMetric: {value: 1743694200000},
			ratingsMetric: {value: 0},
			urls: ['pdf_test2.pdf'],
			usersInvolvedMetric: {value: 4},
		},
		{
			assetId: '4',
			assetTitle: 'document_test',
			commentsMetric: {value: 0},
			downloadsMetric: {value: 23},
			impressionMadeMetric: {value: 23},
			lastViewedMetric: {value: 1741015800000},
			ratingsMetric: {value: 0},
			urls: ['document_test.docx'],
			usersInvolvedMetric: {value: 2},
		},
		{
			assetId: '5',
			assetTitle: 'pdf_test3',
			commentsMetric: {value: 0},
			downloadsMetric: {value: 768},
			impressionMadeMetric: {value: 67},
			lastViewedMetric: {value: 1741534200000},
			ratingsMetric: {value: 0},
			urls: ['pdf_test3.pdf'],
			usersInvolvedMetric: {value: 4},
		},
		{
			assetId: '6',
			assetTitle: 'pdf_test4',
			commentsMetric: {value: 0},
			downloadsMetric: {value: 324},
			impressionMadeMetric: {value: 85},
			lastViewedMetric: {value: 1741188600000},
			ratingsMetric: {value: 0},
			urls: ['pdf_test4.pdf'],
			usersInvolvedMetric: {value: 3},
		},
	],
	total: 6,
};

export const roomStatisticsDevEnvData = {
	totalEvents: 100,
	userSessions: [
		{
			userSessionEvents: [
				{
					createDate: '2026-03-04T14:38:26Z',
					emailAddressHashed: 'visitor1@example.com',
					name: 'view',
				},
				{
					createDate: '2026-03-04T19:13:24Z',
					emailAddressHashed: 'visitor1@example.com',
					name: 'comment',
				},
			],
		},
		{
			userSessionEvents: [
				{
					createDate: '2026-03-05T11:09:28Z',
					emailAddressHashed: 'visitor2@example.com',
					name: 'view',
				},
			],
		},
	],
};
