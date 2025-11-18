/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import ClayButton from '@clayui/button';
import {ClayRadio, ClayRadioGroup} from '@clayui/form';
import ClayModal from '@clayui/modal';
import {sub} from 'frontend-js-web';
import React, {useCallback, useEffect, useState} from 'react';

import {
	IBulkActionFDSData,
	IBulkActionTaskStarterDTO,
} from '../../../common/types/BulkActionTask';
import {displayErrorToast} from '../../../common/utils/toastUtil';
import AssetCategories from '../../info_panel/components/AssetCategories';
import AssetTags from '../../info_panel/components/AssetTags';
import {EntryCategorizationDTO} from '../../info_panel/services/ObjectEntryService';
import {triggerAssetBulkAction} from '../../props_transformer/actions/triggerAssetBulkAction';

export default function CategorizationModalContent({
	apiURL,
	closeModal,
	cmsGroupId,
	selectedData,
	type = 'KeywordBulkAction',
}: {
	apiURL?: string;
	closeModal: () => void;
	cmsGroupId: number;
	selectedData: IBulkActionFDSData;
	type: 'KeywordBulkAction' | 'TaxonomyCategoryBulkAction';
}) {
	const [categorizationDTO, setCategorizationDTO] =
		useState<EntryCategorizationDTO>({
			keywords: [],
			taxonomyCategoryBriefs: [],
			taxonomyCategoryIds: [],
		});
	const [submitDisabled, setSubmitDisabled] = useState<boolean>(false);
	const [selectedOperation, setSelectedOperation] = useState<string>('add');

	const Component =
		type === 'KeywordBulkAction' ? AssetTags : AssetCategories;

	const doBulkSubmit = useCallback(async () => {
		setSubmitDisabled(true);

		const taskStarterDTO: Partial<IBulkActionTaskStarterDTO<any>> = {
			apiURL,
			onCreateError: ({error}) => {
				setSubmitDisabled(false);

				displayErrorToast(error as string);
			},
			onCreateSuccess: (response) => {
				if (response.error) {
					setSubmitDisabled(false);

					displayErrorToast(response.error as string);

					return;
				}

				closeModal();
			},
			overrideDefaultErrorToast: true,
			overrideDefaultSuccessToast: true,
			selectedData,
			type,
		};

		if (
			type === 'TaxonomyCategoryBulkAction' &&
			categorizationDTO?.taxonomyCategoryIds?.length
		) {
			triggerAssetBulkAction({
				...taskStarterDTO,
				keyValues: {
					append: selectedOperation === 'add',
					toAddCategoryIds: categorizationDTO.toAddCategoryIds,
					toRemoveCategoryIds: categorizationDTO.toRemoveCategoryIds,
				},
			} as IBulkActionTaskStarterDTO<'TaxonomyCategoryBulkAction'>);
		}

		if (
			type === 'KeywordBulkAction' &&
			categorizationDTO?.keywords?.length
		) {
			triggerAssetBulkAction({
				...taskStarterDTO,
				keyValues: {
					append: selectedOperation === 'add',
					toAddTagNames: categorizationDTO.toAddTagNames,
					toRemoveTagNames: categorizationDTO.toRemoveTagNames,
				},
			} as IBulkActionTaskStarterDTO<'KeywordBulkAction'>);
		}
	}, [
		apiURL,
		categorizationDTO,
		closeModal,
		selectedData,
		setSubmitDisabled,
	]);

	const getCommonEntries = useCallback(() => {

		/**
		 * TODO
		 * create service for /common endpoint as landing GET
		 * for base keywords and categories (depending)
		 */
	}, []);

	useEffect(() => {
		getCommonEntries();
	}, [getCommonEntries]);

	const updateLocalObjectEntry = useCallback(
		({
			keywords,
			lastAddedBrief,
			taxonomyCategoryIds,
			toAddCategoryIds,
			toAddTagNames,
			toRemoveCategoryIds,
			toRemoveTagNames,
		}: EntryCategorizationDTO): void => {
			setCategorizationDTO(
				({
					keywords: currentKeywords,
					taxonomyCategoryBriefs = [],
					taxonomyCategoryIds: currentTaxonomyCategoryIds,
				}) => ({
					keywords: keywords || currentKeywords!,
					taxonomyCategoryBriefs: [
						...taxonomyCategoryBriefs,
						...(lastAddedBrief
							? [
									{
										embeddedTaxonomyCategory:
											lastAddedBrief,
									},
								]
							: []),
					],
					taxonomyCategoryIds:
						taxonomyCategoryIds || currentTaxonomyCategoryIds,
					toAddCategoryIds,
					toAddTagNames,
					toRemoveCategoryIds,
					toRemoveTagNames,
				})
			);
		},
		[setCategorizationDTO]
	);

	return (
		<>
			<ClayModal.Header
				closeButtonAriaLabel={Liferay.Language.get('close')}
			>
				{type === 'KeywordBulkAction'
					? Liferay.Language.get('edit-tags')
					: Liferay.Language.get('edit-categories')}
			</ClayModal.Header>

			<ClayModal.Body>
				<ClayRadioGroup
					name="add-replace"
					onChange={(value) => setSelectedOperation(value as string)}
					value={selectedOperation}
				>
					<ClayRadio
						checked={true}
						label={Liferay.Language.get('edit')}
						value="add"
					>
						<div className="form-text">
							{Liferay.Language.get(
								'add-new-categories-or-remove-common-categories'
							)}
						</div>
					</ClayRadio>

					<ClayRadio
						label={Liferay.Language.get('replace')}
						value="replace"
					>
						<div className="form-text">
							{Liferay.Language.get(
								'these-categories-replace-all-existing-categories'
							)}
						</div>
					</ClayRadio>
				</ClayRadioGroup>

				<Component
					cmsGroupId={cmsGroupId}
					collapsable={false}
					objectEntry={categorizationDTO}
					updateObjectEntry={updateLocalObjectEntry}
				/>
			</ClayModal.Body>

			<ClayModal.Footer
				last={
					<ClayButton.Group spaced>
						<ClayButton
							displayType="secondary"
							onClick={closeModal}
						>
							{Liferay.Language.get('cancel')}
						</ClayButton>

						<ClayButton
							disabled={
								!(
									categorizationDTO.keywords?.length ||
									categorizationDTO.taxonomyCategoryIds
										?.length
								) || submitDisabled
							}
							displayType="primary"
							onClick={doBulkSubmit}
							type="button"
						>
							{selectedData.selectAll
								? Liferay.Language.get('add-to-all-assets')
								: selectedData?.items?.length === 1
									? Liferay.Language.get('add-to-1-asset')
									: sub(
											Liferay.Language.get(
												'add-to-x-assets'
											),
											selectedData?.items?.length
										)}
						</ClayButton>
					</ClayButton.Group>
				}
			/>
		</>
	);
}
