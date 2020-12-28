package com.czh.controller;

import com.czh.bean.User;
import com.czh.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Auhtor：陈志华
 * @createTime：2020-09-27 00:06
 * @Description：
 */
@RestController
public class TestController {
    @Autowired
    private UserDao userDao;

    @RequestMapping("/test")
    public String test(){
        List<User> users = userDao.getAllUser();
        for (User u:users) {
            System.out.println(u);
        }

//        mybatis的map的list集合
        List<Map<String, Object>> userMap = userDao.getAllUserMap();
        for (Map<String, Object> map:userMap) {
            String role = (String) map.get("role");
            System.out.println(role);
        }
        return "success";
    }

    @RequestMapping("/test1")
    public String test1(Integer id){
        //一条记录存放在map
        Map<String, Object> map = userDao.getUserById(id);
        System.out.println(map);
        System.out.println(map.get("role"));
        return "success";
    }

    @RequestMapping("/test2")
    public String test2(){
        //多条记录存放在map，需要指定key,这里一般把主键作为key
        Map<Integer, User> map = userDao.getUserMap();
        System.out.println(map);
        Set<Integer> keySet = map.keySet();
//        for (Integer num:keySet) {
//            System.out.println(map.get(num));
//        }

        Iterator<Integer> iterator = keySet.iterator();
        while (iterator.hasNext()){
            Integer next = iterator.next();
            System.out.println(map.get(next));
        }

        return "success";
    }
}
