package com.zqc.itineraryweb.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsRequest;
import com.aliyun.dysmsapi20170525.models.QuerySendDetailsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.tea.TeaUnretryableException;
import com.aliyun.tea.ValidateException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云 短信服务
 *
 * @author zqc
 * @date 2024/6/21 16:19
 **/
@Component
public class AliYunSmsClient {

    private static final Logger logger = LoggerFactory.getLogger(AliYunSmsClient.class);

    private static final String signName = "驴游";

    private static final String templateCode = "SMS_462056166";

    private static final String endpoint = "dysmsapi.aliyuncs.com";

    private static String accessKey;

    private static String accessKeySecret;

    @Value("${aliyun.sms.accessKey}")
    public void setSecretKey(String accessKey) {
        AliYunSmsClient.accessKey = accessKey;
    }

    @Value("${aliyun.sms.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        AliYunSmsClient.accessKeySecret = accessKeySecret;
    }

    private static Client client;

    /**
     * 创建短信客户端
     */
    private static Client createSmsClient() {
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
     * 发送短信验证码
     *
     * @param phoneNumber 手机号
     * @param randomCode  需要发送的随机验证码
     * @return SendSmsResponse
     */
    public static SendSmsResponse sendSmsCode(String phoneNumber, int randomCode) {
        Client sendSmsCodeClient = createSmsClient();
        if (sendSmsCodeClient == null) {
            logger.error("sendSmsCode 短信客户端未成功创建");
            return null;
        }

        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setPhoneNumbers(phoneNumber)
                .setTemplateParam("{\"code\":\"" + randomCode + "\"}");

        RuntimeOptions runtimeOptions = new RuntimeOptions();

        try {
            SendSmsResponse sendSmsResponse = sendSmsCodeClient.sendSmsWithOptions(sendSmsRequest, runtimeOptions);
            logger.info("短信服务-发送验证码响应: {}", new Gson().toJson(sendSmsResponse.body));
            if ("OK".equals(sendSmsResponse.body.code)) {
                return sendSmsResponse;
            }
        } catch (ValidateException ve) {
            logger.error("sendSmsCode 整体错误信息: {}", ve.getMessage());
        } catch (TeaUnretryableException tue) {
            logger.error("sendSmsCode 错误信息: {}", tue.getMessage());
            logger.error("sendSmsCode 请求记录: {}", tue.getLastRequest());
        } catch (TeaException te) {
            logger.error("sendSmsCode 错误码: {}", te.getCode());
            logger.error("sendSmsCode 错误信息以及本次请求的RequestId: {}", te.getMessage());
            logger.error("sendSmsCode 具体错误内容: {}", te.getData());
        } catch (Exception e) {
            logger.error("sendSmsCode Exception: {}", e.getMessage());
        }

        // 如果发生异常，则返回null
        return null;
    }

    /**
     * 查询短信发送详情
     *
     * @param phoneNumber 查询的手机号
     * @param bizId       短信发送回执ID
     * @param sendDate    短信发送日期，需要以 yyyyMMdd 字符串格式输入
     * @return QuerySendDetailsResponse
     */
    public static QuerySendDetailsResponse querySendDetails(String phoneNumber, String bizId, String sendDate) {
        Client smsDetailsClient = createSmsClient();
        if (smsDetailsClient == null) {
            logger.error("短信客户端未成功创建");
            return null;
        }

        QuerySendDetailsRequest querySendDetailsRequest = new QuerySendDetailsRequest()
                .setPhoneNumber(phoneNumber)
                .setBizId(bizId)
                .setSendDate(sendDate)
                .setPageSize(5L)
                .setCurrentPage(1L);

        RuntimeOptions runtimeOptions = new RuntimeOptions();

        try {
            QuerySendDetailsResponse querySendDetailsResponse =
                    smsDetailsClient.querySendDetailsWithOptions(querySendDetailsRequest, runtimeOptions);
            logger.info("短信服务-查询短信发送详情响应: {}", new Gson().toJson(querySendDetailsResponse.body));
            if ("OK".equals(querySendDetailsResponse.body.code)) {
                return querySendDetailsResponse;
            }
        } catch (ValidateException ve) {
            logger.error("整体错误信息: {}", ve.getMessage());
        } catch (TeaUnretryableException tue) {
            logger.error("错误信息: {}", tue.getMessage());
            logger.error("请求记录: {}", tue.getLastRequest());
        } catch (TeaException te) {
            logger.error("错误码: {}", te.getCode());
            logger.error("错误信息以及本次请求的RequestId: {}", te.getMessage());
            logger.error("具体错误内容: {}", te.getData());
        } catch (Exception e) {
            logger.error("Exception: {}", e.getMessage());
        }

        // 如果发生异常，则返回null
        return null;
    }
}