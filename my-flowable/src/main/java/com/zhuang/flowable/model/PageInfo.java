package com.zhuang.flowable.model;

import java.util.List;

public class PageInfo<T> {

	List<T> list;
	private int pageNo;
	private int pageSize;
	private int totalRows;
	private int totalPages;
	private int pageStartRow;
	private int pageEndRow;
	private boolean hasNextPage;
	private boolean hasPreviousPage;

	public PageInfo() {
	}

	public PageInfo(int pageNo, int pageSize, int totalRows, List<T> list) {

		this.totalRows = totalRows;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		int totalPages = new Double(Math.ceil((double)totalRows / (double)pageSize)).intValue();
		this.totalPages = totalPages;
		
		this.pageStartRow = pageSize * (pageNo - 1) + 1;
		this.pageEndRow=((pageStartRow+pageSize)>totalRows)?totalRows:(pageStartRow+pageSize);
		boolean hasPreviousPage = pageNo > 1 ? true : false;
		this.hasPreviousPage = hasPreviousPage;
		boolean hasNextPage = totalPages > pageNo ? true : false;
		this.hasNextPage = hasNextPage;
		this.list = list;

	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getPageStartRow() {
		return pageStartRow;
	}

	public void setPageStartRow(int pageStartRow) {
		this.pageStartRow = pageStartRow;
	}

	public int getPageEndRow() {
		return pageEndRow;
	}

	public void setPageEndRow(int pageEndRow) {
		this.pageEndRow = pageEndRow;
	}

	public boolean getHasNextPage() {
		return hasNextPage;
	}

	public void setHasNextPage(boolean hasNextPage) {
		this.hasNextPage = hasNextPage;
	}

	public boolean getHasPreviousPage() {
		return hasPreviousPage;
	}

	public void setHasPreviousPage(boolean hasPreviousPage) {
		this.hasPreviousPage = hasPreviousPage;
	}

	@Override
	public String toString() {
		return "PageInfo{" +
				"list=" + list +
				", pageNo=" + pageNo +
				", pageSize=" + pageSize +
				", totalRows=" + totalRows +
				", totalPages=" + totalPages +
				", pageStartRow=" + pageStartRow +
				", pageEndRow=" + pageEndRow +
				", hasNextPage=" + hasNextPage +
				", hasPreviousPage=" + hasPreviousPage +
				'}';
	}
}
