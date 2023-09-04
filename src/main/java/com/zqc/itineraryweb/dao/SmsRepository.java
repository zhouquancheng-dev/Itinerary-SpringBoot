package com.zqc.itineraryweb.dao;

import com.zqc.itineraryweb.entity.Sms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SmsRepository extends JpaRepository<Sms, Integer> {

    /**
     * 检查 Sms 表中是否存在此[phoneNumber]号码
     *
     * @param phoneNumber 手机号
     * @return 存在返回true，否则false
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 通过手机号和id查找短信信息
     *
     * @param phoneNumber 手机号
     * @param bizId 发送回执id
     * @return 返回Sms实体对象
     */
    Sms findSmsByPhoneNumberAndBizId(String phoneNumber, String bizId);

    /**
     * 通过手机号删除短信
     *
     * @param phoneNumber 手机号
     */
    @Modifying
    @Transactional
    @Query("""
            delete from Sms s where s.phoneNumber = :phoneNumber
            """)
    void deleteSmsByPhoneNumber(String phoneNumber);

    /**
     * 通过手机号更新短信信息
     *
     * @param sms Sms实体对象
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE Sms s SET
            s.phoneNumber = :#{#sms.phoneNumber},
            s.smsCode = :#{#sms.smsCode},
            s.bizId = :#{#sms.bizId},
            s.sendTime = :#{#sms.sendTime},
            s.token = :#{#sms.token},
            s.createTime = :#{#sms.createTime}
            WHERE s.phoneNumber = :#{#sms.phoneNumber}
            """)
    void updateSmsByPhoneNumber(@Param("sms") Sms sms);

}
