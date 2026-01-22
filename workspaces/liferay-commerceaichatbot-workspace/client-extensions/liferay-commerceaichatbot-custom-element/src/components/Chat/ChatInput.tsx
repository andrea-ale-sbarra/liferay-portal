import ClayButton from '@clayui/button';
import ClayForm, { ClayInput } from '@clayui/form';
import ClayIcon from '@clayui/icon';
import { useRef } from 'react';
import { UseFormReturn } from 'react-hook-form';

import { Schema } from '../CommerceAIChatBot.tsx';

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
			event.stopPropagation();
			if (!event.shiftKey && text.trim() !== '') {
				event.preventDefault();
				formRef.current?.requestSubmit();
			}
		}
	};

	return (
		<ClayForm
			ref={formRef}
			className="chat-input-container"
			onSubmit={handleSubmit((data) => props.onSubmit(data, () => {
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
			<ClayButton
				className="chat-input-submit"
				disabled={
					formState.isSubmitting ||
					formState.isLoading ||
					!text?.trim()?.length
				}
				displayType="primary"
				aria-label="Submit button"
				type="submit"
			>
				<ClayIcon
					aria-label="Submit Prompt"
					symbol="order-arrow-right"
				/>
			</ClayButton>
		</ClayForm>
	);
}
