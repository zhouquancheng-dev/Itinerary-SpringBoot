package com.zqc.itineraryweb.config;

import com.zqc.itineraryweb.utils.RSAKeyPairGeneratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class RSAKeyConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAKeyConfig.class);

    /**
     * 创建并返回RSA密钥对。
     *
     * @return RSA密钥对
     */
    @Bean
    public KeyPair rsaKeyPair() {
        try {
            String readPrivateKey = readPrivateKey();
            String readPublicKey = readPublicKey();

            // 使用已经编码的私钥和公钥字符串创建KeyPair
            PrivateKey privateKey = RSAKeyPairGeneratorUtils.decodePrivateKey(readPrivateKey);
            PublicKey publicKey = RSAKeyPairGeneratorUtils.decodePublicKey(readPublicKey);

            return new KeyPair(publicKey, privateKey);
        } catch (Exception e) {
            // 处理异常
            LOGGER.error("RSA密钥对加载失败: {}", e.getMessage(), e);
            throw new RuntimeException("RSA密钥对加载失败", e);
        }
    }

    /**
     * 创建并返回RSA私钥。
     *
     * @return RSA私钥
     */
    @Bean
    public PrivateKey rsaPrivateKey() {
        try {
            String readPrivateKey = readPrivateKey();

            // 使用已经编码的私钥字符串创建RSAPrivateKey
            return RSAKeyPairGeneratorUtils.decodePrivateKey(readPrivateKey);
        } catch (Exception e) {
            // 处理异常
            LOGGER.error("RSA私钥解析失败: {}", e.getMessage());
            throw new RuntimeException("RSA私钥解析失败", e);
        }
    }

    /**
     * 创建并返回RSA公钥。
     *
     * @return RSA公钥
     */
    @Bean
    public PublicKey rsaPublicKey() {
        try {
            String readPublicKey = readPublicKey();

            // 使用已经编码的公钥字符串创建RSAPublicKey
            return RSAKeyPairGeneratorUtils.decodePublicKey(readPublicKey);
        } catch (Exception e) {
            // 处理异常
            LOGGER.error("RSA公钥解析失败: {}", e.getMessage());
            throw new RuntimeException("RSA公钥解析失败", e);
        }
    }

    /**
     * 从类路径中的文件"keys/private_key.txt"读取RSA私钥。
     *
     * @return RSA私钥的Base64编码字符串
     */
    public static String readPrivateKey() {
        try {
            ClassPathResource resource = new ClassPathResource("keys/private_key.txt");
            InputStream inputStream = resource.getInputStream();

            byte[] privateKeyBytes = FileCopyUtils.copyToByteArray(inputStream);
            String privateKeyBase64 = new String(privateKeyBytes, StandardCharsets.UTF_8);

            if (StringUtils.hasText(privateKeyBase64)) {
                return privateKeyBase64;
            } else {
                LOGGER.error("私钥内容为空");
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("读取私钥时发生IO异常: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从类路径中的文件"keys/public_key.txt"读取RSA公钥。
     *
     * @return RSA公钥的Base64编码字符串
     */
    public static String readPublicKey() {
        try {
            ClassPathResource resource = new ClassPathResource("keys/public_key.txt");
            InputStream inputStream = resource.getInputStream();

            byte[] publicKeyBytes = FileCopyUtils.copyToByteArray(inputStream);
            String publicKeyBase64 = new String(publicKeyBytes, StandardCharsets.UTF_8);

            if (StringUtils.hasText(publicKeyBase64)) {
                return publicKeyBase64;
            } else {
                LOGGER.error("公钥内容为空");
                return null;
            }
        } catch (IOException e) {
            LOGGER.error("读取公钥时发生IO异常: {}", e.getMessage(), e);
            return null;
        }
    }
}