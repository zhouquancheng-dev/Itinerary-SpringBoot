package com.zqc.itineraryweb.mappers;

import com.zqc.itineraryweb.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Insert("insert into tb_user_login (user_id, username, password, created_at, last_login_at) " +
            "VALUES (#{userId}, #{username}, #{password}, #{createdAt}, #{lastLoginAt})")
    void insertUser(User user);

    @Select("select * from tb_user_login where username = #{username}")
    User getByUser(String username);

    @Select("select count(token) from tb_user_login where username = #{username}")
    int getUserByToken(String username);

    @Select("select count(username) from tb_user_login where username = #{username}")
    int getHasByUsername(String username);

    @Update("update tb_user_login set token = #{token}, last_login_at = #{lastLoginAt} " +
            "where username = #{username} and password = #{password}")
    void updateByTokenAndLastLoginAt(User user);

    @Update("update tb_user_login set token = #{token} where username = #{username}")
    int clearUserToken(String username, String token);
}
