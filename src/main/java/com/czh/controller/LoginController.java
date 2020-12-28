package com.czh.controller;

import com.alibaba.fastjson.JSONObject;
import com.czh.bean.User;
import com.czh.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auhtor：陈志华
 * @createTime：2020-09-24 12:18
 * @Description：
 */
@RestController
public class LoginController {

    @Autowired
    private UserDao userDao;

    @RequestMapping("/login")
    public String login(@RequestBody User user){
        String flag="error";
        Map<String,Object> map=new HashMap<String, Object>();
        User user1=userDao.getUserByNameAndPwd(user.getUsername(),
                user.getPassword());
        if(user1!=null){
            flag="ok";
        }
        flag="ok";
        map.put("flag", flag);
        map.put("data", JSONObject.toJSONString(user1));
        String jsonString = JSONObject.toJSONString(map);
        return jsonString;
    }

    public static void main(String[] args) {
        String str="12\n34\n56\n978";
        byte[] bytes = str.getBytes();
//        for (byte b:bytes) {
//            System.out.print(b+",");
//        }
        int countchar = countchar(bytes, 0, bytes.length-1, (byte) 10);
        System.out.println(str.substring(0,3));

//        Pattern p=Pattern.compile("\n");
//        String pattern = p.pattern();
//        System.out.println(pattern);
//        Matcher m=p.matcher(str);
//        if(m.find()){
//            String cr =m.group();
//            System.out.println(str);
//            System.out.println(cr);
//        }



    }

    private static int countchar(byte[] bytestr, int startnum, int endnum, byte targetbyte)//计算byte数组中含某个值的个数
    {
        int c = 0;
        for (int i = startnum; i <= endnum; i++)
        {
            if (bytestr[i] == targetbyte)//如果等于空格
            {
                c += 1;
            }
        }
        return c;
    }

}
