package com.zqc.itineraryweb.utils;

import org.springframework.stereotype.Component;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class RSAKeyPairGeneratorUtils {

    /**
     * 解码 Base64 编码的私钥字符串并返回 PrivateKey 对象。
     *
     * @param privateKeyBase64 私钥的 Base64 编码字符串
     * @return PrivateKey 对象
     * @throws RuntimeException 如果解码失败或发生其他异常
     */
    public static PrivateKey decodePrivateKey(String privateKeyBase64) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("私钥解码失败，无效的密钥规范", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("私钥解码失败，无效的算法", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("私钥解码失败，无效的Base64字符串", e);
        }
    }

    /**
     * 解码 Base64 编码的公钥字符串并返回 PublicKey 对象。
     *
     * @param publicKeyBase64 公钥的 Base64 编码字符串
     * @return PublicKey 对象
     * @throws RuntimeException 如果解码失败或发生其他异常
     */
    public static PublicKey decodePublicKey(String publicKeyBase64) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("公钥解码失败，无效的密钥规范", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("公钥解码失败，无效的算法", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("公钥解码失败，无效的Base64字符串", e);
        }
    }
}