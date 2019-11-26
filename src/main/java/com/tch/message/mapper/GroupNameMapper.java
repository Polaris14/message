package com.tch.message.mapper;

import org.apache.ibatis.annotations.Select;

public interface GroupNameMapper {
    @Select("select groupname from groupname where id = #{groupName}")
    String findGroupNameById(Integer groupName);
}
