package com.zqc.itineraryweb.mappers;

import com.zqc.itineraryweb.entity.Sms;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SmsMapper {

    @Insert("insert into tb_sms (phone_number, sms_code, biz_id, send_time, expire, create_time) " +
            "VALUES (#{phoneNumber}, #{smsCode}, #{bizId}, #{sendTime}, #{expire}, #{createTime})")
    void insertSmsData(Sms sms);

    @Select("select * from tb_sms where phone_number = #{phoneNumber} and biz_id = #{bizId}")
    Sms querySmsData(String phoneNumber, String bizId);

    @Select("select count(phone_number) from tb_sms where phone_number = #{phoneNumber}")
    int querySmsByPhoneNumber(String phoneNumber);

    @Update("update tb_sms set " +
            "phone_number = #{phoneNumber}, sms_code = #{smsCode}, biz_id = #{bizId}, send_time = #{sendTime}, expire = #{expire} " +
            "where phone_number = #{phoneNumber}")
    void updateSmsData(Sms sms);

    @Delete("delete from tb_sms where phone_number = #{phoneNumber}")
    void deleteByPhoneNumber(String phoneNumber);

}
