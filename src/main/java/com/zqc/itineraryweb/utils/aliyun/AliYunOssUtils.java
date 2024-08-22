package com.zqc.itineraryweb.utils.aliyun;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.event.ProgressEvent;
import com.aliyun.oss.event.ProgressEventType;
import com.aliyun.oss.event.ProgressListener;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.PutObjectRequest;
import com.zqc.itineraryweb.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.Date;

@Component
public class AliYunOssUtils {

    private static final Logger logger = LoggerFactory.getLogger(AliYunOssUtils.class);

    private static final String DOMAIN_HOST = "oss.zyuxr.top";

    private static final String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";

    private static final String region = "cn-shenzhen";

    private static final String bucketName = "web-userzhou";

    private static String accessKey;

    private static String accessKeySecret;

    @Value("${aliyun.oss.accessKey}")
    public void setSecretKey(String accessKey) {
        AliYunOssUtils.accessKey = accessKey;
    }

    @Value("${aliyun.oss.accessKeySecret}")
    public void setAccessKeySecret(String accessKeySecret) {
        AliYunOssUtils.accessKeySecret = accessKeySecret;
    }

    /**
     * 上传文件并存储到阿里云OSS
     *
     * @param file          服务端接收到的文件
     * @param bucketDirName 文件路径，路径中不能包含Bucket名称
     * @return 返回存储到阿里云OSS中后文件的url地址
     */
    public static Result<String> upload(
            MultipartFile file,
            String bucketDirName,
            String fileName
    ) {
        if (file == null) {
            return Result.error("上传失败，请检查文件是否为空");
        }

        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKey, accessKeySecret);

        // 创建OSSClient实例
        ClientBuilderConfiguration configuration = new ClientBuilderConfiguration();
        configuration.setSignatureVersion(SignVersion.V4);
        configuration.setSupportCname(true);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(configuration)
                .region(region)
                .build();

        try {
            if (!ossClient.doesBucketExist(bucketName)) {
                // 创建Bucket
                ossClient.createBucket(bucketName);
            }

            String fileExtension = getFileExtension(file);
            String objectName = bucketDirName + "/" + fileName + fileExtension;

            // 创建文件流
            InputStream inputStream = file.getInputStream();

            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            ossClient.putObject(putObjectRequest.withProgressListener(new PutObjectProgressListener()));

            Date expiration = new Date(new Date().getTime() + 1800 * 1000L);
            // 生成签名URL
            GeneratePresignedUrlRequest request =
                    new GeneratePresignedUrlRequest(bucketName, objectName, HttpMethod.GET);
            request.setExpiration(expiration);

            // 通过HTTP GET请求生成签名URL
            URL signedUrl = ossClient.generatePresignedUrl(request);

            // 使用URL类解析并重建URL
            String protocol = signedUrl.getProtocol();
            String queryFile = signedUrl.getFile();

            // 构建新的URL
            URL modifiedUrl = new URL(protocol, DOMAIN_HOST, queryFile);

            // 将URL转换为字符串并返回
            return Result.success(modifiedUrl.toString());
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
            ossClient.shutdown();
        }
        return Result.error("上传异常");
    }


    static class PutObjectProgressListener implements ProgressListener {
        private long bytesWritten = 0;
        private long totalBytes = -1;
        private boolean succeed = false;

        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            long bytes = progressEvent.getBytes();
            ProgressEventType eventType = progressEvent.getEventType();
            switch (eventType) {
                case TRANSFER_STARTED_EVENT:
                    logger.info("Start to upload......");
                    break;
                case REQUEST_CONTENT_LENGTH_EVENT:
                    this.totalBytes = bytes;
                    logger.info("Total bytes: {}", this.totalBytes);
                    break;
                case REQUEST_BYTE_TRANSFER_EVENT:
                    this.bytesWritten += bytes;
                    if (this.totalBytes != -1) {
                        int percent = (int) (this.bytesWritten * 100.0 / this.totalBytes);
                        logger.info("{} bytes have been written at this time, upload progress: {}% ({}, {})", bytes, percent, this.bytesWritten, this.totalBytes);
                    }
//                    logger.info("{} bytes have been written at this time, upload ratio: ({})", bytes, this.bytesWritten);
                    break;
                case TRANSFER_COMPLETED_EVENT:
                    this.succeed = true;
                    logger.info("Upload completed, {} bytes have been written in total", this.bytesWritten);
                    break;
                case TRANSFER_FAILED_EVENT:
                    logger.info("Failed to upload, {} bytes have been transferred", this.bytesWritten);
                    break;
                default:
                    break;
            }
        }

    }

    public static String getFileExtension(MultipartFile file) {
        // 获取文件的原始文件名
        String originalFilename = file.getOriginalFilename();

        // 检查文件名是否有效，并找到最后一个点的位置
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 如果文件名无效或没有后缀，返回空字符串
        return "";
    }

}
