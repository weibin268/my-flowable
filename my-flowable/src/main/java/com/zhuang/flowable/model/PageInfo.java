package com.zhuang.flowable.model;

import lombok.Data;

import java.util.List;

@Data
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
        int totalPages = new Double(Math.ceil((double) totalRows / (double) pageSize)).intValue();
        this.totalPages = totalPages;

        this.pageStartRow = pageSize * (pageNo - 1) + 1;
        this.pageEndRow = ((pageStartRow + pageSize) > totalRows) ? totalRows : (pageStartRow + pageSize);
        boolean hasPreviousPage = pageNo > 1 ? true : false;
        this.hasPreviousPage = hasPreviousPage;
        boolean hasNextPage = totalPages > pageNo ? true : false;
        this.hasNextPage = hasNextPage;
        this.list = list;

    }
}
