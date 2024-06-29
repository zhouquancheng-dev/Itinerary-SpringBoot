package com.zqc.itineraryweb.controllers.im;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping(value = "/neim")
public class IMController {

    private static final Logger logger = LoggerFactory.getLogger(IMController.class);

    @Value("${im.appSecret}")
    private String appSecret;

    private final ObjectMapper objectMapper;

    public IMController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/messageCc")
    @ResponseBody
    public JSONPObject mockClient(HttpServletRequest request) {
        JSONPObject result;

        try {
            byte[] body = readBody(request);
            if (body == null) {
                logger.warn("Request error: empty body!");
                result = new JSONPObject("code", createResponseNode(414));
                return result;
            }

            String contentType = request.getContentType();
            String appKey = request.getHeader("AppKey");
            String curTime = request.getHeader("CurTime");
            String md5 = request.getHeader("MD5");
            String checkSum = request.getHeader("CheckSum");

            logger.info("Request headers: ContentType = {}, AppKey = {}, CurTime = {}, MD5 = {}, CheckSum = {}",
                    contentType, appKey, curTime, md5, checkSum);

            String requestBody = new String(body, StandardCharsets.UTF_8);
            logger.info("Request body = {}", requestBody);

            String verifyMD5 = CheckSumBuilder.getMD5(requestBody);
            String verifyChecksum = CheckSumBuilder.getCheckSum(appSecret, verifyMD5, curTime);

            logger.debug("verifyMD5 = {}, verifyChecksum = {}", verifyMD5, verifyChecksum);

            // 比较md5、checkSum是否一致，以及后续业务处理
            result = new JSONPObject("code", createResponseNode(200));
        } catch (Exception ex) {
            logger.error("Error processing request: {}", ex.getMessage(), ex);
            result = new JSONPObject("code", createResponseNode(414));
        }

        return result;
    }

    private ObjectNode createResponseNode(int code) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("code", code);
        return node;
    }

    private byte[] readBody(HttpServletRequest request) throws IOException {
        int contentLength = request.getContentLength();
        if (contentLength > 0) {
            byte[] body = new byte[contentLength];
            IOUtils.readFully(request.getInputStream(), body);
            return body;
        }
        return null;
    }

    static class CheckSumBuilder {

        private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

        public static String getCheckSum(String appSecret, String nonce, String curTime) {
            return encode("sha1", appSecret + nonce + curTime);
        }

        public static String getMD5(String requestBody) {
            return encode("md5", requestBody);
        }

        private static String encode(String algorithm, String value) {
            if (value == null) {
                return null;
            }

            try {
                MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
                messageDigest.update(value.getBytes());
                return getFormattedText(messageDigest.digest());
            } catch (Exception e) {
                throw new RuntimeException("Encoding error", e);
            }
        }

        private static String getFormattedText(byte[] bytes) {
            StringBuilder buf = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                buf.append(HEX_DIGITS[(b >> 4) & 0x0f]);
                buf.append(HEX_DIGITS[b & 0x0f]);
            }
            return buf.toString();
        }
    }
}
