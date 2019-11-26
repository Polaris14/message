package com.tch.message.mapper;

import com.tch.message.model.Friend;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FriendMapper {

    @Select("select * from friend where uid = #{id}")
    List<Friend> findFriendByUserID(Integer id);
}
