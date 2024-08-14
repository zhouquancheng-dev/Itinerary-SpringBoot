package com.zqc.itineraryweb.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class AliYunOSSUtils {

    private static final Logger logger = LoggerFactory.getLogger(AliYunOSSUtils.class);

    private static final String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";

    private static final String bucketName = "myweb-userzhou";

    private static String accessKey;

    private static String accessKeySecret;

    @Value("${aliyun.oss.accessKey}")
    public void setSecretKey(String accessKey) {
        AliYunOSSUtils.accessKey = accessKey;
    }

    @Value("${aliyun.oss.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        AliYunOSSUtils.accessKeySecret = accessKeySecret;
    }

    /**
     * 上传文件并存储到阿里云OSS
     *
     * @param file     服务端接收到的文件
     * @param fileName 接收到的文件名
     * @return 返回存储到阿里云OSS中后文件的url地址
     */
    public static String upload(MultipartFile file, String fileName) {

        /*
         * 返回的url格式："https://bucketName.oss-cn-shenzhen.aliyuncs.com/fileName"
         * 例如："https://myweb-userzhou.oss-cn-shenzhen.aliyuncs.com/xxx.jpg"
         *      "https://myweb-userzhou.oss-cn-shenzhen.aliyuncs.com/服务端接收到的文件名"
         */
        String url = null;

        // 创建OSSClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();

            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, fileName, inputStream);

            // 设置该属性可以返回response，如果不设置，则返回的response为空
            putObjectRequest.setProcess("true");

            // 创建PutObject请求
            // ossClient.putObject() 上传文件
            PutObjectResult result = ossClient.putObject(putObjectRequest);
            int responseCode = result.getResponse().getStatusCode();

            // 以 ”//“ 进行分割
            String[] splits = endpoint.split("//");
            // 按照文件的url格式拼接起来
            url = splits[0] + "//" + bucketName + "." + splits[1] + "/" + fileName;

            // 如果上传成功，则返回200
            logger.info("OSS上传返回信息:{}", responseCode);
        } catch (OSSException oe) {
            logger.error("OSSException，这意味着您的请求已发送到 OSS，但由于某种原因被错误响应拒绝。");
            logger.error("OSSException Error Message 错误信息:{}", oe.getErrorMessage());
            logger.error("Error Code 错误代码:{}", oe.getErrorCode());
            logger.error("Request ID 请求ID:{}", oe.getRequestId());
            logger.error("Host ID 主机ID:{}", oe.getHostId());
        } catch (ClientException ce) {
            logger.error("ClientException，这意味着客户端在尝试与OSS通信时遇到严重的内部问题，例如无法访问网络。");
            logger.error("Error Message: {}", ce.getMessage());
        } catch (FileNotFoundException fe) {
            logger.error("FileNotFoundException Error", fe);
        } catch (IOException ioe) {
            logger.error("IOException Error", ioe);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return url;
    }

}
