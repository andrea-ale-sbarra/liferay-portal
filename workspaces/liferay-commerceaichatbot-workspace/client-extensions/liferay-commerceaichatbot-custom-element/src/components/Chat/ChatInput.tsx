import ClayForm, { ClayInput } from '@clayui/form';
import ClayLayout from '@clayui/layout';
import ClayIcon from '@clayui/icon';
import ClayButton from '@clayui/button';
import { UseFormReturn } from 'react-hook-form';

import { Schema } from '../CommerceAIChatBot.tsx';
import { useRef } from 'react';

type Props = {
	form: UseFormReturn<Schema>;
	onSubmit: (data: Schema, onSuccess: () => void) => void;
	placeholder: string;
};

export default function ChatInput(props: Props) {
	const { handleSubmit, formState, register, watch } = props.form;
	const formRef = useRef<HTMLFormElement>(null);
	const inputRef = useRef<HTMLInputElement>(null);
	const text = watch('input');

	const handleKeyDown = (event: any) => {
		if (event.key === 'Enter') {
			if (!event.shiftKey && text.trim() !== '') {
				event.preventDefault();
				formRef.current?.requestSubmit();
			}
		}
	};

	return (
		<>
			<ClayLayout.ContentRow className="w-100">
				<ClayLayout.ContentCol expand>
					<ClayForm
						ref={formRef}
						className="d-flex w-100"
						onSubmit={() => handleSubmit((data) => props.onSubmit(data, () => {
							if (inputRef.current) {
								inputRef.current.value = ''
							}
						}))}
					>
						<ClayInput
							{...register('input')}
							value={text}
							onKeyDown={handleKeyDown}
							component="textarea"
							disabled={
								formState.isSubmitting || formState.isLoading
							}
							placeholder={
								props.placeholder ||
								'Ask the Assistant for help'
							}
						/>
					</ClayForm>
				</ClayLayout.ContentCol>
				<ClayLayout.ContentCol>
					<ClayLayout.ContentSection>
						<ClayButton.Group>
							<ClayButton
								disabled={
									formState.isSubmitting ||
									formState.isLoading ||
									!text?.trim()?.length
								}
								displayType="primary"
								aria-label="Submit button"
								onClick={(event) => handleSubmit((data) => props.onSubmit(data, () => {
									if (inputRef.current) {
										inputRef.current.value = ''
									}

								}))(event)}
							>
								<ClayIcon
									aria-label="Submit Prompt"
									symbol="order-arrow-right"
								/>
							</ClayButton>
						</ClayButton.Group>
					</ClayLayout.ContentSection>
				</ClayLayout.ContentCol>
			</ClayLayout.ContentRow>
		</>
	);
}
