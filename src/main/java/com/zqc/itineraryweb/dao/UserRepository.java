package com.zqc.itineraryweb.dao;

import com.zqc.itineraryweb.entity.user.User;
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
     * @return User实体对象
     */
    @Query("""
             select u from User u
             where u.userId = :userIdBytes
             and u.username = :username
            """)
    User findUserByUserIdAndUsername(
            @Param("userIdBytes") byte[] userIdBytes,
            @Param("username") String username
    );

    /**
     * 根据用户id和用户名更新LastLoginAt的值
     *
     * @param newLastLoginAt 新的日期时间
     * @param userIdBytes 用户id
     * @param username    用户名
     */
    @Modifying
    @Transactional
    @Query("""
            update User u
            set u.lastLoginAt = :newLastLoginAt
            where u.userId = :userIdBytes
            and u.username = :username
            """)
    int updateLastLoginAtByUserIdAndUsername(
            @Param("newLastLoginAt") LocalDateTime newLastLoginAt,
            @Param("userIdBytes") byte[] userIdBytes,
            @Param("username") String username
    );

    /**
     * 通过用户名更新 UserId 和 Password 的值
     *
     * @param newUserIdBytes 新用户id
     * @param newPassword 新密码
     * @param username 用户名
     */
    @Modifying
    @Transactional
    @Query("""
            update User u
            set u.userId = :newUserIdBytes, u.password = :newPassword
            where u.username = :username
            """)
    int updateUserIdAndPasswordByUsername(
            @Param("newUserIdBytes") byte[] newUserIdBytes,
            @Param("newPassword") String newPassword,
            @Param("username") String username
    );
}
