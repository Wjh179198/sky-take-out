package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Select("select * from user where openid = #{openId}")
    User getByOPenId (String openId);

    void insert(User user);

    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    @Select("select count(*) from user where create_time >= #{beginTime} and create_time <= #{endTime}")
    Integer getNewUserCount(LocalDateTime beginTime, LocalDateTime endTime);

    @Select("select count(*) from user where create_time <= #{endTime}")
    Integer getSumUser(LocalDateTime endTime);
}
