package com.czh.bean;

/**
 * @Auhtor：陈志华
 * @createTime：2020-10-08 15:00
 * @Description：
 */
public class SubMenu {
    private Integer id;
    private String title;
    private String path;

    public SubMenu() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public SubMenu(String title, String path) {
        this.title = title;
        this.path = path;
    }

    @Override
    public String toString() {
        return "SubMenu{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
