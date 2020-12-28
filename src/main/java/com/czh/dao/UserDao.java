package com.czh.dao;

import com.czh.bean.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Auhtor：陈志华
 * @createTime：2020-09-26 17:57
 * @Description：
 */
@Repository
public interface UserDao {

    public User getUserByNameAndPwd(@Param("username") String username, @Param("password") String password);

    public List<User> getAllUser();

    public List<Map<String,Object>> getAllUserMap();

    public Map<String,Object> getUserById(@Param("id") Integer id);

    @MapKey("id")
    public Map<Integer,User> getUserMap();

    public List<User> getAllUserByPage(@Param("username") String username,@Param("pageStart") int pageStart,@Param("pageSize") int pageSize);

    public Integer getUsersCounts(@Param("username") String username);

}
