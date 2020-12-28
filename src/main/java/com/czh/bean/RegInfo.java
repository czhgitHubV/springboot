package com.czh.bean;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author:chenzhihua
 * @Date: 2020/12/3 16:08
 * @Deacription:
 **/
public class RegInfo {
    private Integer startNum;//开始位置
    private Integer endNum;//结束位置
    private Integer colorIndex;//颜色的下标
    private String nickName;//别名
    private String type;//类型
    private String contentText;//文本框的内容

    public Integer getStartNum() {
        return startNum;
    }

    public void setStartNum(Integer startNum) {
        this.startNum = startNum;
    }

    public Integer getEndNum() {
        return endNum;
    }

    public void setEndNum(Integer endNum) {
        this.endNum = endNum;
    }

    public Integer getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(Integer colorIndex) {
        this.colorIndex = colorIndex;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public RegInfo() {
    }

    public RegInfo(Integer startNum, Integer endNum, Integer colorIndex, String nickName, String type, String contentText) {
        this.startNum = startNum;
        this.endNum = endNum;
        this.colorIndex = colorIndex;
        this.nickName = nickName;
        this.type = type;
        this.contentText = contentText;
    }

    @Override
    public String toString() {
        return "RegInfo{" +
                "startNum=" + startNum +
                ", endNum=" + endNum +
                ", colorIndex=" + colorIndex +
                ", nickName='" + nickName + '\'' +
                ", type='" + type + '\'' +
                ", contentText='" + contentText + '\'' +
                '}';
    }
}
