/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import Card from '@clayui/card';
import {Text} from '@clayui/core';
import ClayIcon from '@clayui/icon';
import LoadingIndicator from '@clayui/loading-indicator';
import classNames from 'classnames';
import React, {useState} from 'react';
import ReactMarkdown from 'react-markdown';

import {useAppContext} from '../context/AppContext';
import {Liferay} from '../services/liferay';
import {Message as MessageType} from '../types';

type ModalContentProps = {
	fullscreen: boolean;
	isLoadingContent: boolean;
	messages: any[];
	onSelectAsset: (asset: any) => void;
};

const Message = ({
	children,
	role,
}: {
	children: any;
} & Omit<MessageType, 'text'>) => {
	const {myUserAccount} = useAppContext();

	if (role === 'system') {
		return children;
	}

	return (
		<div
			className={classNames('d-flex rounded p-4 align-items-center', {
				'ai-options-panel': role === 'assistant',
				'justify-content-end ': role === 'user',
			})}
		>
			{role === 'assistant' && (
				<div className="mr-5">
					<ClayIcon color="blue" symbol="stars" />
				</div>
			)}

			<div className="optionContainer">{children}</div>

			{role === 'user' && (
				<img
					className="ml-3 rounded-circle"
					height={32}
					src={myUserAccount?.image || '/image/user_portrait'}
					width={32}
				/>
			)}
		</div>
	);
};

export default function ModalContent({
	fullscreen,
	isLoadingContent,
	messages,
	onSelectAsset,
}: ModalContentProps) {
	return (
		<>
			<div
				className={classNames({
					disabled: isLoadingContent,
				})}
			>
				<Message role="assistant">
					<b>
						Hi {Liferay.ThemeDisplay.getUserName()}! How can I help you?
					</b>
				</Message>
			</div>

			{messages.map((message, index) => (
				<Message key={index} role={message.role}>
					{React.isValidElement(message.text) ? (
						message.text
					) : (
						<ReactMarkdown>{message.text}</ReactMarkdown>
					)}
				</Message>
			))}

			{isLoadingContent && (
				<Message role="assistant">
					<div className="align-items-center d-flex justify-content-center w-100">
						<Text color="secondary" italic>
							Waiting for the answer... be patient.
						</Text>

						<LoadingIndicator
							className="ml-2"
							displayType="secondary"
							shape="squares"
							size="sm"
							title="Waiting for the answer... be patient."
						/>
					</div>
				</Message>
			)}
		</>
	);
}
