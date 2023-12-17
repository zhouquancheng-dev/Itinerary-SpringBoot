package com.zqc.itineraryweb.utils;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ValidationUtils {

    /**
     * 正则验证
     *
     * @param input 待验证的输入字符串
     * @param regex 正则表达式
     * @return 符合正则返回true，否则返回false
     */
    private static boolean isValidPattern(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    /**
     * 用户名正则验证
     *
     * @param username 用户名
     * @return 符合正则返回true，否则返回false
     */
    public static boolean isValidUsername(String username) {
        // 国内合法11位手机号码
        String regex = "^1\\d{10}$";
        return isValidPattern(username, regex);
    }

    /**
     * 用户密码正则验证
     *
     * @param password 用户密码
     * @return 符合正则返回true，否则返回false
     */
    public static boolean isValidPassword(String password) {
        // 大写字母、小写字母、数字、特殊字符 (. , ; : ! @ # $ % ^ & < > ( ) * ? = + -)
        // 6位以上且包含任意3项及以上的类型
        String regex = "^(?=.*[A-Z])|(?=.*[a-z])|(?=.*\\d)|(?=.*[.,;:!@#$%^&<>()*?=+-])\\S{6,}$";
        return isValidPattern(password, regex);
    }

    /**
     * 用户手机号正则验证
     *
     * @param phoneNumber 手机号码
     * @return 符合正则返回true，否则返回false
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        // 手机号(mobile phone)中国(严谨), 根据工信部2019年最新公布的手机号段
        String regex = "^(?:(?:\\+|00)86)?1(?:3\\d|4[5-79]|5[0-35-9]|6[5-7]|7[0-8]|8\\d|9[1589])\\d{8}$";
        return isValidPattern(phoneNumber, regex);
    }

    /**
     * 用户短信验证码正则验证
     *
     * @param smsCode 验证码
     * @return 符合正则返回true，否则返回false
     */
    public static boolean isValidSmsCode(String smsCode) {
        // 匹配数字
        String regex = "^\\d+$";
        return isValidPattern(smsCode, regex);
    }

}
