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
        // 字母、数字、下划线、连字符，长度范围为 3 ~ 16 个字符
        String regex = "^[a-zA-Z0-9_-]{3,16}$";
        return isValidPattern(username, regex);
    }

    /**
     * 用户密码正则验证
     *
     * @param password 用户密码
     * @return 符合正则返回true，否则返回false
     */
    public static boolean isValidPassword(String password) {
        // 8 到 20 个字符，至少包含一个大写，一个小写，一个数字，一个特殊字符
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[-@#$%^&+?;,.=!])(?!.*\\s).{8,20}$";
        return isValidPattern(password, regex);
    }

    /**
     * 用户手机号正则验证
     *
     * @param phoneNumber 手机号码
     * @return 符合正则返回true，否则返回false
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        // 匹配11位数字
        String regex = "^[0-9]{11}$";
        return isValidPattern(phoneNumber, regex);
    }

    /**
     * 用户手机号正则验证
     *
     * @param smsCode 验证码
     * @return 符合正则返回true，否则返回false
     */
    public static boolean isValidSmsCode(String smsCode) {
        // 匹配数字
        String regex = "^[0-9]+$";
        return isValidPattern(smsCode, regex);
    }

}
