package com.zqc.itineraryweb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.security.*;
import java.util.Base64;

@SpringBootTest
public class RSAKeyPairGeneratorTest {

    @Test
    void generatorRSA() {
        try {
            // 使用RSA算法生成密钥对
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // 选择密钥位数，通常使用2048或更高的位数
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // 获取生成的私钥和公钥
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            // 将私钥和公钥转换为Base64字符串
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            // 输出Base64字符串到控制台
            System.out.println("私钥(Base64):");
            System.out.println(privateKeyBase64);
            System.out.println("\n公钥(Base64):");
            System.out.println(publicKeyBase64);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
