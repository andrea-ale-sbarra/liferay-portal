/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.commerce.product.option;

import java.util.Objects;

/**
 * @author Andrea Sbarra
 */
public interface CommerceOption {

	public String getOptionKey();

	public String getPriceType();

	String getFormFieldTypeName();

	String getName();

	boolean isSkuContributor();

	public boolean matches(CommerceOption commerceOptionValue);

	public static class Builder {

		public CommerceOption build() {
			return new CommerceOption() {

				@Override
				public String getOptionKey() {
					return _optionKey;
				}

				@Override
				public String getPriceType() {
					return _priceType;
				}

				@Override
				public String getFormFieldTypeName() {
					return _formFieldTypeName;
				}

				@Override
				public String getName() {
					return _name;
				}


				@Override
				public boolean isSkuContributor() {
					return _skuContributor;
				}

				@Override
				public boolean matches(
					CommerceOption commerceOptionValue) {

					if (commerceOptionValue == null) {
						return false;
					}

					if (Objects.equals(
							_optionKey, commerceOptionValue.getOptionKey())) {

						return true;
					}

					return false;
				}

				private final String _formFieldTypeName = Builder.this._formFieldTypeName;

				private final String _name = Builder.this._name;
				private final String _optionKey = Builder.this._optionKey;
				private final String _priceType = Builder.this._priceType;

				private final boolean _skuContributor = Builder.this._skuContributor;

			};
		}

		public Builder formFieldTypeName(String formFieldTypeName) {
			_formFieldTypeName = formFieldTypeName;

			return this;
		}

		public Builder name(String name) {
			_name = name;

			return this;
		}

		public Builder optionKey(String optionKey) {
			_optionKey = optionKey;

			return this;
		}

		public Builder skuContributor(boolean skuContributor) {
			_skuContributor = skuContributor;

			return this;
		}

		public Builder priceType(String priceType) {
			_priceType = priceType;

			return this;
		}

		private String _formFieldTypeName;
		private String _optionKey;
		private String _name;
		private String _priceType;
		private boolean _skuContributor;
	}
}