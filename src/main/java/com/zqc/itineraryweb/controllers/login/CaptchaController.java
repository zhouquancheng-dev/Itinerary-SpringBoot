package com.zqc.itineraryweb.controllers.login;

import com.aliyun.captcha20230305.Client;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaRequest;
import com.aliyun.captcha20230305.models.VerifyIntelligentCaptchaResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "/validate")
public class CaptchaController {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);

    @Value("${ali.captcha.accessKey}")
    private String secretKey;

    @Value("${ali.captcha.accessKeySecret}")
    private String accessKeySecret;

    @Value("${gt.loginAction.captchaKey}")
    private String loginCaptchaKey;

    @Value("${gt.smsAction.captchaKey}")
    private String smsCaptchaKey;

    private final RestTemplate restTemplate;

    public CaptchaController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 极验验证码服务端二次校验
     *
     * @param captchaId     验证 id
     * @param lotNumber     验证流水号
     * @param captchaOutput 验证输出信息
     * @param passToken     验证通过标识
     * @param genTime       验证通过时间戳
     * @return Boolean
     */
    @PostMapping(
            value = "/gt-captcha",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public Boolean verifyCaptcha(
            @RequestParam("captcha_id") String captchaId,
            @RequestParam("lot_number") String lotNumber,
            @RequestParam("captcha_output") String captchaOutput,
            @RequestParam("pass_token") String passToken,
            @RequestParam("gen_time") String genTime,
            @RequestParam("expected_action") String action
    ) {
        String url = "https://gcaptcha4.geetest.com/validate";

        String captchaKey;
        if (action.equals("login")) {
            captchaKey = loginCaptchaKey;
        } else {
            captchaKey = smsCaptchaKey;
        }

        // 生成签名使用标准的hmac算法，使用用户当前完成验证的流水号lot_number作为原始消息message，使用客户验证私钥作为key
        // 采用sha256散列算法将message和key进行单向散列生成最终的签名
        String signToken = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, captchaKey).hmacHex(lotNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("lot_number", lotNumber);
        multiValueMap.add("captcha_output", captchaOutput);
        multiValueMap.add("pass_token", passToken);
        multiValueMap.add("gen_time", genTime);
        multiValueMap.add("captcha_id", captchaId);
        multiValueMap.add("sign_token", signToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(multiValueMap, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            String responseBody = response.getBody();
//            logger.info("responseBody: {}", new Gson().fromJson(responseBody, Captcha.class));
            if (responseBody != null) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String result = jsonObject.get("result").getAsString();
                return "success".equals(result);
            } else {
                logger.error("Response body is null");
                return false;
            }
        } else {
            logger.error("Request failed with status code: " + response.getStatusCode());
            return false;
        }
    }

    /**
     * 阿里云服务端验证码验证
     *
     * @param captchaVerifyParam 前端参数
     * @return Boolean
     */
    @PostMapping(value = "/ali-captcha")
    public Boolean verifyCaptcha(@RequestBody String captchaVerifyParam) {
        Config config = new Config();
        config.accessKeyId = secretKey;
        config.accessKeySecret = accessKeySecret;
        //设置请求地址
        config.endpoint = "captcha.cn-shanghai.aliyuncs.com";
        // 设置连接超时为5000毫秒
        config.connectTimeout = 5000;
        // 设置读超时为5000毫秒
        config.readTimeout = 5000;

        try {
            Client client = new Client(config);

            VerifyIntelligentCaptchaRequest request = new VerifyIntelligentCaptchaRequest();
            request.setCaptchaVerifyParam(captchaVerifyParam);

            RuntimeOptions runtimeOptions = new RuntimeOptions();
            VerifyIntelligentCaptchaResponse response =
                    client.verifyIntelligentCaptchaWithOptions(request, runtimeOptions);
//            logger.info(new Gson().toJson(response.body));
            return response.body.result.verifyResult;
        } catch (TeaException error) {
            logger.error(error.message);
            return false;
        } catch (Exception _error) {
            logger.error(_error.getMessage());
            return false;
        }
    }
}