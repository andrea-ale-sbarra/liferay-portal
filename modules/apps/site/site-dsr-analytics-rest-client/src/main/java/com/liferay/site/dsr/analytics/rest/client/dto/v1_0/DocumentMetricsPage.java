/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.client.dto.v1_0;

import com.liferay.site.dsr.analytics.rest.client.function.UnsafeSupplier;
import com.liferay.site.dsr.analytics.rest.client.serdes.v1_0.DocumentMetricsPageSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Gianmarco Brunialti
 * @generated
 */
@Generated("")
public class DocumentMetricsPage implements Cloneable, Serializable {

	public static DocumentMetricsPage toDTO(String json) {
		return DocumentMetricsPageSerDes.toDTO(json);
	}

	public DocumentMetric[] getDocumentMetrics() {
		return documentMetrics;
	}

	public void setDocumentMetrics(DocumentMetric[] documentMetrics) {
		this.documentMetrics = documentMetrics;
	}

	public void setDocumentMetrics(
		UnsafeSupplier<DocumentMetric[], Exception>
			documentMetricsUnsafeSupplier) {

		try {
			documentMetrics = documentMetricsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected DocumentMetric[] documentMetrics;

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public void setTotal(UnsafeSupplier<Long, Exception> totalUnsafeSupplier) {
		try {
			total = totalUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long total;

	@Override
	public DocumentMetricsPage clone() throws CloneNotSupportedException {
		return (DocumentMetricsPage)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof DocumentMetricsPage)) {
			return false;
		}

		DocumentMetricsPage documentMetricsPage = (DocumentMetricsPage)object;

		return Objects.equals(toString(), documentMetricsPage.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return DocumentMetricsPageSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:1281313620