package com.zqc.itineraryweb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

@Service
public class RSAKeyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RSAKeyService.class);
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public RSAKeyService(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * 使用公钥加密密码，并返回Base64编码的加密结果
     *
     * @param password 密码
     * @return Base64编码的加密结果字符串
     */
    public String encryptPassword(String password) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(passwordBytes);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("密码加密失败，无法找到算法: {}", e.getMessage());
            throw new RuntimeException("密码加密失败", e);
        } catch (NoSuchPaddingException e) {
            LOGGER.error("密码加密失败，无法找到填充方式: {}", e.getMessage());
            throw new RuntimeException("密码加密失败", e);
        } catch (InvalidKeyException e) {
            LOGGER.error("密码加密失败，无效的密钥: {}", e.getMessage());
            throw new RuntimeException("密码加密失败", e);
        } catch (IllegalBlockSizeException e) {
            LOGGER.error("密码加密失败，非法的块大小: {}", e.getMessage());
            throw new RuntimeException("密码加密失败", e);
        } catch (BadPaddingException e) {
            LOGGER.error("密码加密失败，填充错误: {}", e.getMessage());
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 使用私钥解密Base64编码的密码，并返回解密后的密码字符串
     *
     * @param encryptedPasswordBase64 Base64编码的加密密码字符串
     * @return 解密后的密码字符串
     */
    public String decryptPassword(String encryptedPasswordBase64) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPasswordBase64);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("密码解密失败，无法找到算法: {}", e.getMessage());
            throw new RuntimeException("密码解密失败", e);
        } catch (NoSuchPaddingException e) {
            LOGGER.error("密码解密失败，无法找到填充方式: {}", e.getMessage());
            throw new RuntimeException("密码解密失败", e);
        } catch (InvalidKeyException e) {
            LOGGER.error("密码解密失败，无效的密钥: {}", e.getMessage());
            throw new RuntimeException("密码解密失败", e);
        } catch (IllegalBlockSizeException e) {
            LOGGER.error("密码解密失败，非法的块大小: {}", e.getMessage());
            throw new RuntimeException("密码解密失败", e);
        } catch (BadPaddingException e) {
            LOGGER.error("密码解密失败，填充错误: {}", e.getMessage());
            throw new RuntimeException("密码解密失败", e);
        }
    }
}