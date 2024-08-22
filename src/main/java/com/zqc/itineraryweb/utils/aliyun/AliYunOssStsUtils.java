package com.zqc.itineraryweb.utils.aliyun;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.zqc.itineraryweb.entity.Result;
import com.zqc.itineraryweb.entity.oss.StsResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliYunOssStsUtils {

    private static final Logger logger = LoggerFactory.getLogger(AliYunOssStsUtils.class);

    private static final String endpoint = "sts.cn-shenzhen.aliyuncs.com";

    private static final String roleSessionName = "RamOss";

    private static final Long durationSeconds = 3600L;

    private static String roleArn;

    private static String accessKey;

    private static String accessKeySecret;

    @Value("${aliyun.sts.roleArn}")
    public void setRoleArn(String roleArn) {
        AliYunOssStsUtils.roleArn = roleArn;
    }

    @Value("${aliyun.sts.accessKey}")
    public void setSecretKey(String accessKey) {
        AliYunOssStsUtils.accessKey = accessKey;
    }

    @Value("${aliyun.sts.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        AliYunOssStsUtils.accessKeySecret = accessKeySecret;
    }

    public static Result<StsResponse> getStsToken() {
        try {
            // 发起STS请求所在的地域。建议保留默认值，默认值为空字符串（""）。
            String regionId = "";
            DefaultProfile.addEndpoint(regionId, "Sts", endpoint);
            IClientProfile profile = DefaultProfile.getProfile(regionId, accessKey, accessKeySecret);
            DefaultAcsClient client = new DefaultAcsClient(profile);

            final AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(roleSessionName);
            request.setPolicy(null);
            request.setDurationSeconds(durationSeconds);

            final AssumeRoleResponse response = client.getAcsResponse(request);

            StsResponse stsResponse = getStsResponse(response);

            logger.info("Successfully obtained STS token, Request Id: {}", response.getRequestId());

            return Result.success(stsResponse);
        } catch (ClientException e) {
            logger.error("Failed");
            logger.error("Error code: {}", e.getErrCode());
            logger.error("Error message: {}", e.getErrMsg());
            logger.error("Error RequestId: {}", e.getRequestId());
            return Result.error("获取STS临时令牌失败: " + e.getErrMsg());
        }
    }

    private static @NotNull StsResponse getStsResponse(AssumeRoleResponse response) {
        StsResponse stsResponse = new StsResponse();
        stsResponse.setRequestId(response.getRequestId());

        StsResponse.AssumedRoleUser assumedRoleUser = new StsResponse.AssumedRoleUser();
        assumedRoleUser.setAssumedRoleId(response.getAssumedRoleUser().getAssumedRoleId());
        assumedRoleUser.setArn(response.getAssumedRoleUser().getArn());

        stsResponse.setAssumedRoleUser(assumedRoleUser);

        StsResponse.Credentials credentials = new StsResponse.Credentials();
        credentials.setSecurityToken(response.getCredentials().getSecurityToken());
        credentials.setExpiration(response.getCredentials().getExpiration());
        credentials.setAccessKeySecret(response.getCredentials().getAccessKeySecret());
        credentials.setAccessKeyId(response.getCredentials().getAccessKeyId());

        stsResponse.setCredentials(credentials);

        return stsResponse;
    }

}
