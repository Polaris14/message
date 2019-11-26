package com.tch.message.mapper;

import com.tch.message.model.Group;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GroupMapper {

    @Select("select * from `group` where id = #{gid}")
    Group findGroupById(Integer gid);
}
