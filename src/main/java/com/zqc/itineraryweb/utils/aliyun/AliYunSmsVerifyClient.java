package com.zqc.itineraryweb.utils.aliyun;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.*;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.Gson;
import com.zqc.itineraryweb.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 云通信号码认证服务 短信认证服务
 *
 * @author zqc
 * @date 2024/6/21 021 16:21
 **/
@Component
public class AliYunSmsVerifyClient {

    private static final Logger logger = LoggerFactory.getLogger(AliYunSmsVerifyClient.class);

    private static final String signName = "驴游";

    private static final String templateCode = "SMS_462056166";

    private static final String endpoint = "dypnsapi.aliyuncs.com";

    private static String accessKey;

    private static String accessKeySecret;

    @Value("${aliyun.smsVerify.accessKey}")
    public void setSecretKey(String accessKey) {
        AliYunSmsVerifyClient.accessKey = accessKey;
    }

    @Value("${aliyun.smsVerify.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        AliYunSmsVerifyClient.accessKeySecret = accessKeySecret;
    }

    private static Client client;

    /**
     * 创建短信客户端
     */
    private static synchronized Client createClient() {
        if (client == null) {
            Config config = new Config()
                    .setAccessKeyId(accessKey)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint(endpoint);
            try {
                client = new Client(config);
            } catch (Exception e) {
                logger.error("创建短信客户端异常: {}", e.getMessage());
            }
        }
        return client;
    }

    /**
     * 发送验证码
     *
     * @param phoneNumber  手机号
     * @param codeLength   验证码长度支持4～8位长度
     * @param validTime    验证码有效时长
     * @param sendInterval 时间间隔
     * @return Result<SendSmsVerifyCodeResponseBody>
     */
    public static Result<SendSmsVerifyCodeResponseBody> sendSmsVerifyCode(
            String phoneNumber,
            long codeLength,
            long validTime,
            long sendInterval
    ) {
        Client client = createClient();
        if (client == null) {
            logger.error("sendSmsVerifyCode 短信客户端成功失败");
            return Result.error("验证码发送失败");
        }

        SendSmsVerifyCodeRequest sendSmsVerifyCodeRequest = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phoneNumber)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setCodeLength(codeLength)
                .setValidTime(validTime)
                .setInterval(sendInterval)
                .setTemplateParam("{\"code\":\"##code##\"}");

        RuntimeOptions runtime = new RuntimeOptions();

        try {
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(sendSmsVerifyCodeRequest, runtime);
            logger.info("云通信认证-发送短信验证码响应: {}", new Gson().toJson(response.body));
            if ("OK".equals(response.body.code)) {
                return Result.success(response.body);
            } else {
                return Result.error("验证码发送失败: " + response.body.message);
            }
        } catch (TeaException te) {
            logger.error("sendSmsVerifyCode 错误码: {}", te.getCode());
            logger.error("sendSmsVerifyCode 错误信息以及本次请求的RequestId: {}", te.getMessage());
            logger.error("sendSmsVerifyCode 具体错误内容: {}", te.getData());
        } catch (Exception e) {
            logger.error("sendSmsVerifyCode Exception: {}", e.getMessage());
        }

        return Result.error("验证码发送失败");
    }

    /**
     * 核验验证码
     *
     * @param phoneNumber 手机号
     * @param verifyCode  验证码
     * @return Result<CheckSmsVerifyCodeResponseBody>
     */
    public static Result<CheckSmsVerifyCodeResponseBody> checkSmsVerifyCode(String phoneNumber, String verifyCode) {
        Client client = createClient();
        if (client == null) {
            logger.error("checkSmsVerifyCode 短信客户端成功失败");
            return Result.error("验证码核验失败");
        }

        CheckSmsVerifyCodeRequest checkSmsVerifyCodeRequest = new CheckSmsVerifyCodeRequest()
                .setPhoneNumber(phoneNumber)
                .setVerifyCode(verifyCode);

        RuntimeOptions runtime = new RuntimeOptions();

        try {
            CheckSmsVerifyCodeResponse response = client.checkSmsVerifyCodeWithOptions(checkSmsVerifyCodeRequest, runtime);
            logger.info("云通信认证-核验短信验证码响应: {}", new Gson().toJson(response.body));
            if ("OK".equals(response.body.code)) {
                return Result.success(response.body);
            } else {
                return Result.error("验证码核验失败: " + response.body.message);
            }
        } catch (TeaException te) {
            logger.error("checkSmsVerifyCode 错误码: {}", te.getCode());
            logger.error("checkSmsVerifyCode 错误信息以及本次请求的RequestId: {}", te.getMessage());
            logger.error("checkSmsVerifyCode 具体错误内容: {}", te.getData());
        } catch (Exception e) {
            logger.error("checkSmsVerifyCode Exception: {}", e.getMessage());
        }

        return Result.error("验证码核验失败");
    }

}