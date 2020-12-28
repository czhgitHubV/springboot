package com.czh.bean;

/**
 * @Auhtor：陈志华
 * @createTime：2020-10-09 16:52
 * @Description：
 */
public class QueryInfo {

    private String query;
    private Integer pageStart=1;
    private Integer pageSize=1;

    public QueryInfo(String query, Integer pageStart, Integer pageSize) {
        this.query = query;
        this.pageStart = pageStart;
        this.pageSize = pageSize;
    }

    public QueryInfo() {
    }

    @Override
    public String toString() {
        return "QueryInfo{" +
                "query='" + query + '\'' +
                ", pageStart=" + pageStart +
                ", pageSize=" + pageSize +
                '}';
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getPageStart() {
        return pageStart;
    }

    public void setPageStart(Integer pageStart) {
        this.pageStart = pageStart;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
