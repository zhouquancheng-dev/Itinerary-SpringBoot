package com.zqc.itineraryweb.dao;

import com.zqc.itineraryweb.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 根据用户名查询User信息
     *
     * @param username 用户名
     * @return User实体对象
     */
    User findUserByUsername(@Param("username") String username);

    /**
     * 检查 User 表中是否存在此[username]的用户
     *
     * @param username 用户名
     * @return 存在true，否则false
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 通过用户id和用户名查找用户
     *
     * @param userIdBytes 用户id，传入UUID对象字节格式
     * @param username    用户名
     * @return password字符串
     */
    @Query("""
             select u.password from User u
             where u.userId = :userIdBytes
             and u.username = :username
            """)
    String findUserByUserIdAndUsername(
            @Param("userIdBytes") byte[] userIdBytes,
            @Param("username") String username
    );

    /**
     * 按用户名查找Token
     *
     * @param username 用户名
     * @return int
     */
    @Query("""
            select count(u.token) from User u where u.username = :username
            """)
    int findTokenByUsername(@Param("username") String username);

    /**
     * 根据用户id和用户名更新Token、LastLoginAt的值
     *
     * @param newToken    新的Token值
     * @param lastLoginAt 新的日期时间
     * @param userIdBytes 用户id
     * @param username    用户名
     */
    @Modifying
    @Transactional
    @Query("""
            update User u set u.token = :newToken, u.lastLoginAt = :lastLoginAt
            where u.userId = :userIdBytes
            and u.username = :username
            """)
    void updateTokenAndLastLoginAtByUserIdAndUsername(
            @Param("newToken") String newToken,
            @Param("lastLoginAt") LocalDateTime lastLoginAt,
            @Param("userIdBytes") byte[] userIdBytes,
            @Param("username") String username
    );

    /**
     * 根据用户id和用户名更新Token的值
     *
     * @param newToken    新Token值
     * @param userIdBytes 用户id
     * @param username    用户名
     */
    @Modifying
    @Transactional
    @Query("""
            update User u set u.token = :newToken
            where u.userId = :userIdBytes
            and u.username = :username
            """)
    void updateTokenByUserIdAndUsername(
            @Param("newToken") String newToken,
            @Param("userIdBytes") byte[] userIdBytes,
            @Param("username") String username
    );

    /**
     * 根据用户名更新Token值
     *
     * @param newToken 新Token值
     * @param username 用户名
     */
    @Modifying
    @Transactional
    @Query("""
            update User u set u.token = :newToken
            where u.username = :username
            """)
    void updateTokenByUsername(
            @Param("newToken") String newToken,
            @Param("username") String username
    );

}
