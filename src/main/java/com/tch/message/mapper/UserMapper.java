package com.tch.message.mapper;

import com.tch.message.model.User;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper {

    @Select("select * from user where id = #{userId}")
    User findUserById(Integer userId);

    @Update("update user set sign = #{sign} where id = #{id}")
    int updateSignById(String sign, Integer id);

    @Select("select * from user where username  = #{username} and password = #{password}")
    User findUserByUserNameAndPassword(String username, String password);
}
