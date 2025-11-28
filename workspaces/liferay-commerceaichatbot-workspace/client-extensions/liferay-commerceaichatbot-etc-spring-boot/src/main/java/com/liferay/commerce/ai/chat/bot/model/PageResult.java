package com.liferay.commerce.ai.chat.bot.model;

import java.util.ArrayList;
import java.util.List;

public class PageResult<T> {

	public PageResult() {
	}

	public PageResult(int totalCount, int lastPage, List<T> items) {
		this.totalCount = totalCount;
		this.lastPage = lastPage;

		if (items != null)
			this.items = new ArrayList<>(items);
	}

	public List<T> getItems() {
		return items;
	}

	public int getLastPage() {
		return lastPage;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setItems(List<T> items) {
		this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
	}

	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	private List<T> items = new ArrayList<>();
	private int lastPage;
	private int totalCount;

}