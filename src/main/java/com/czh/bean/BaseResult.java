package com.czh.bean;

import java.util.List;

/**
 * @Author:chenzhihua
 * @Date: 2020/12/3 15:42
 * @Deacription:
 **/
public class BaseResult<T> {
    private T result;
    private String message;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BaseResult() {
    }

    public BaseResult(T result, String message) {
        this.result = result;
        this.message = message;
    }

    @Override
    public String toString() {
        return "BaseResult{" +
                "result=" + result +
                ", message='" + message + '\'' +
                '}';
    }
}
