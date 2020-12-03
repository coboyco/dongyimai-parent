package com.offcn.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果凤爪怒鬼对象
 */
public class PageResult implements Serializable {

    private long total; //总的记录数
    private List rows;  //当前页的结果

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }

    public PageResult() {
    }

    public PageResult(long total, List rows) {
        this.total = total;
        this.rows = rows;
    }
}
