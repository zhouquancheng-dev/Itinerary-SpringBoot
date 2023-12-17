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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliYunSMSClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AliYunSMSClient.class);

    private static final String smsSignName = "驴游";

    private static final String smsTemplateCode = "SMS_462056166";

    private static final String smsEndpoint = "dysmsapi.aliyuncs.com";

    private static String secretKey;

    private static String accessKeySecret;

    @Value("${ali.sms.accessKey}")
    public void setSecretKey(String secretKey) {
        AliYunSMSClient.secretKey = secretKey;
    }

    @Value("${ali.sms.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        AliYunSMSClient.accessKeySecret = accessKeySecret;
    }

    /**
     * 创建短信客户端
     */
    private static Client createSmsClient() {
        Config config = new Config()
                .setAccessKeyId(secretKey)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint(smsEndpoint);
        try {
            return new Client(config);
        } catch (Exception e) {
            LOGGER.error("创建短信客户端异常: {}", e.getMessage());
            return null;
        }
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
            LOGGER.error("短信客户端未成功创建");
            return null;
        }

        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(smsSignName)
                .setTemplateCode(smsTemplateCode)
                .setPhoneNumbers(phoneNumber)
                .setTemplateParam("{\"code\":\"" + randomCode + "\"}");

        RuntimeOptions runtimeOptions = new RuntimeOptions();

        try {
            SendSmsResponse sendSmsResponse = sendSmsCodeClient.sendSmsWithOptions(sendSmsRequest, runtimeOptions);
//            LOGGER.info("阿里云发送验证码响应: {}", new Gson().toJson(sendSmsResponse.body));
            if ("OK".equals(sendSmsResponse.body.code)) {
                return sendSmsResponse;
            }
        } catch (ValidateException ve) {
            LOGGER.error("整体错误信息: {}", ve.getMessage());
        } catch (TeaUnretryableException tue) {
            LOGGER.error("错误信息: {}", tue.getMessage());
            LOGGER.error("请求记录: {}", tue.getLastRequest());
        } catch (TeaException te) {
            LOGGER.error("错误码: {}", te.getCode());
            LOGGER.error("错误信息以及本次请求的RequestId: {}", te.getMessage());
            LOGGER.error("具体错误内容: {}", te.getData());
        } catch (Exception e) {
            LOGGER.error("Exception: {}", e.getMessage());
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
            LOGGER.error("短信客户端未成功创建");
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
//            LOGGER.info("阿里云查询短信详情响应: {}", new Gson().toJson(querySendDetailsResponse.body));
            if ("OK".equals(querySendDetailsResponse.body.code)) {
                return querySendDetailsResponse;
            }
        } catch (ValidateException ve) {
            LOGGER.error("整体错误信息: {}", ve.getMessage());
        } catch (TeaUnretryableException tue) {
            LOGGER.error("错误信息: {}", tue.getMessage());
            LOGGER.error("请求记录: {}", tue.getLastRequest());
        } catch (TeaException te) {
            LOGGER.error("错误码: {}", te.getCode());
            LOGGER.error("错误信息以及本次请求的RequestId: {}", te.getMessage());
            LOGGER.error("具体错误内容: {}", te.getData());
        } catch (Exception e) {
            LOGGER.error("Exception: {}", e.getMessage());
        }

        // 如果发生异常，则返回null
        return null;
    }
}