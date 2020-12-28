package com.czh.controller;

import com.alibaba.fastjson.JSONObject;
import com.czh.bean.MainMenu;
import com.czh.dao.MenuDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auhtor：陈志华
 * @createTime：2020-10-08 15:31
 * @Description：
 */
@RestController
public class MenuController {

    @Autowired(required = false)
    private MenuDao menuDao;

    @RequestMapping("/getAllMenus")
    public String getAllMenus(){
        List<MainMenu> menus = menuDao.getAllMenu();
        Map<String,Object> menuMap=new HashMap<String,Object>();
        if(menus!=null){
            menuMap.put("status", 200);
            menuMap.put("menus",menus);
        }else {
            menuMap.put("status", 500);
            menuMap.put("menus",null);
        }
        String jsonString = JSONObject.toJSONString(menuMap);
        return jsonString;
    }
}
