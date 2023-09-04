package com.zqc.itineraryweb.utils;

import org.springframework.stereotype.Component;

@Component
public class MaskUtils {

    /**
     * 手机号脱敏
     * @param phoneNumber 手机号
     * @return 返回掩码后的字符串
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() >= 7) {
            int startIndex = 3; // 开始脱敏的位置
            int endIndex = phoneNumber.length() - 4; // 结束脱敏的位置
            StringBuilder maskedNumber = new StringBuilder(phoneNumber);
            for (int i = startIndex; i < endIndex; i++) {
                maskedNumber.setCharAt(i, '*');
            }
            return maskedNumber.toString();
        } else {
            // 如果电话号码长度小于7，则不脱敏，直接返回
            return phoneNumber;
        }
    }

}
