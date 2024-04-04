/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {slugify} from 'commerce-frontend-js';
import {debounce} from 'frontend-js-web';

export default function editCpDefinitionOptionValueRelForm({
	BCP47LanguageId,
	namespace,
}) {
	const form = document.getElementById(
		`${namespace}cpDefinitionOptionValueRelfm`
	);

	const dateInput = form.querySelector(`#${namespace}date`);
	const durationInput = form.querySelector(`#${namespace}duration`);
	const durationTypeInput = form.querySelector(`#${namespace}durationType`);
	const labelInput = form.querySelector(
		`#${namespace}optionValueSelectDateLabel`
	);
	const timeInput = form.querySelector(`#${namespace}time`);
	const timeZoneInput = form.querySelector(`#${namespace}timeZone`);

	const optionValueSelectDateObj = new optionValueSelectDate();

	const handleOnLabelInput = function () {
		optionValueSelectDateObj.setDate(slugify.default(dateInput.value));
		optionValueSelectDateObj.setTime(slugify.default(timeInput.value));
		optionValueSelectDateObj.setTimezone(timeZoneInput.value);
		optionValueSelectDateObj.setDuration(durationInput.value);
		optionValueSelectDateObj.setDurationType(durationTypeInput.value);

		labelInput.value = optionValueSelectDateObj.getLabel(BCP47LanguageId);
	};

	dateInput.addEventListener('focus', debounce(handleOnLabelInput, 200));
	durationInput.addEventListener('input', debounce(handleOnLabelInput, 200));
	durationTypeInput.addEventListener(
		'input',
		debounce(handleOnLabelInput, 200)
	);
	timeInput.addEventListener('change', debounce(handleOnLabelInput, 200));
	timeZoneInput.addEventListener('change', debounce(handleOnLabelInput, 200));

	function optionValueSelectDate() {
		this.date = null;
		this.duration = null;
		this.durationType = null;
		this.time = null;
		this.timeZone = null;

		this.setDate = function (date) {
			this.date = date;
		};

		this.setDuration = function (duration) {
			this.duration = duration;
		};

		this.setDurationType = function (durationType) {
			this.durationType = durationType;
		};

		this.setTime = function (time) {
			this.time = time;
		};

		this.setTimezone = function (timeZone) {
			this.timeZone = timeZone;
		};

		this.getLabel = function (locale) {
			const dateSplit = this.date.split('-');
			const [hour, minute] = this.time.split('-');
			const date = new Date(
				dateSplit[2],
				dateSplit[0] - 1,
				dateSplit[1],
				hour,
				minute
			);
			const options = {
				day: 'numeric',
				hour: 'numeric',
				minute: 'numeric',
				month: 'numeric',
				year: 'numeric',
			};
			const formattedDate = date.toLocaleDateString(locale, options);

			if (this.duration && this.durationType) {
				return (
					formattedDate +
					' (' +
					this.timeZone +
					'), ' +
					this.duration +
					' ' +
					this.durationType
				);
			}

			return formattedDate + ' (' + this.timeZone + ')';
		};
	}
}
