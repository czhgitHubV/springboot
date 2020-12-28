package com.czh.bean;

import java.util.List;

/**
 * @Auhtor：陈志华
 * @createTime：2020-10-08 14:27
 * @Description：
 */
public class MainMenu {
    private Integer id;
    private String title;
    private String path;
    List<SubMenu> subMenuList;

    public MainMenu() {
    }

    public MainMenu(String title, String path, List<SubMenu> subMenuList) {
        this.title = title;
        this.path = path;
        this.subMenuList = subMenuList;
    }

    @Override
    public String toString() {
        return "MainMenu{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", subMenuList=" + subMenuList +
                '}';
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

    public List<SubMenu> getSubMenuList() {
        return subMenuList;
    }

    public void setSubMenuList(List<SubMenu> subMenuList) {
        this.subMenuList = subMenuList;
    }
}
