package com.czh.controller;

import com.alibaba.fastjson.JSONObject;
import com.czh.bean.QueryInfo;
import com.czh.bean.User;
import com.czh.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auhtor：陈志华
 * @createTime：2020-10-09 16:54
 * @Description：
 */
@RestController
public class UserController {

    @Autowired
    private UserDao userDao;

    @RequestMapping("/getAllUserByPage")
    public String getAllUserByPage(QueryInfo queryInfo){
        Integer counts = userDao.getUsersCounts("%" + queryInfo.getQuery() + "%");
        int pageSize=(queryInfo.getPageStart()-1)*queryInfo.getPageSize();
        List<User> userList = userDao.getAllUserByPage("%" + queryInfo.getQuery() + "%", pageSize, queryInfo.getPageSize());
        Map<String, Object> map = new HashMap<>();
        map.put("counts",counts);
        map.put("users",userList);
        String json = JSONObject.toJSONString(map);
        return json;

    }
}
