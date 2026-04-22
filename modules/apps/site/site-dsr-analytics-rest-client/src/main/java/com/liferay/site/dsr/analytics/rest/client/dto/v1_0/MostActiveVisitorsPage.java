/**
 * SPDX-FileCopyrightText: (c) 2026 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.site.dsr.analytics.rest.client.dto.v1_0;

import com.liferay.site.dsr.analytics.rest.client.function.UnsafeSupplier;
import com.liferay.site.dsr.analytics.rest.client.serdes.v1_0.MostActiveVisitorsPageSerDes;

import jakarta.annotation.Generated;

import java.io.Serializable;

import java.util.Objects;

/**
 * @author Gianmarco Brunialti
 * @generated
 */
@Generated("")
public class MostActiveVisitorsPage implements Cloneable, Serializable {

	public static MostActiveVisitorsPage toDTO(String json) {
		return MostActiveVisitorsPageSerDes.toDTO(json);
	}

	public MostActiveVisitor[] getMostActiveVisitors() {
		return mostActiveVisitors;
	}

	public void setMostActiveVisitors(MostActiveVisitor[] mostActiveVisitors) {
		this.mostActiveVisitors = mostActiveVisitors;
	}

	public void setMostActiveVisitors(
		UnsafeSupplier<MostActiveVisitor[], Exception>
			mostActiveVisitorsUnsafeSupplier) {

		try {
			mostActiveVisitors = mostActiveVisitorsUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected MostActiveVisitor[] mostActiveVisitors;

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
	public MostActiveVisitorsPage clone() throws CloneNotSupportedException {
		return (MostActiveVisitorsPage)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof MostActiveVisitorsPage)) {
			return false;
		}

		MostActiveVisitorsPage mostActiveVisitorsPage =
			(MostActiveVisitorsPage)object;

		return Objects.equals(toString(), mostActiveVisitorsPage.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return MostActiveVisitorsPageSerDes.toJSON(this);
	}

}
// LIFERAY-REST-BUILDER-HASH:-1788792880