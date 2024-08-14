package com.zqc.itineraryweb.controllers.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zqc.itineraryweb.entity.captcha.*;
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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/validate")
public class CaptchaController {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaController.class);

    @Value("${aliyun.captcha.key}")
    private String aliCaptchaKey;

    @Value("${gt.loginAction.captchaKey}")
    private String loginCaptchaKey;

    @Value("${gt.smsAction.captchaKey}")
    private String smsCaptchaKey;

    private final RestTemplate restTemplate;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    public CaptchaController(RestTemplate restTemplate, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    /**
     * 极验行为验证码服务端二次校验
     *
     * @param captchaId     验证 id
     * @param lotNumber     验证流水号
     * @param captchaOutput 验证输出信息
     * @param passToken     验证通过标识
     * @param genTime       验证通过时间戳
     * @return Boolean
     */
    @PostMapping(
            value = "/gtCaptcha",
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
            logger.info("responseBody: {}", new Gson().fromJson(responseBody, GtCaptcha.class));
            if (responseBody != null) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String result = jsonObject.get("result").getAsString();
                return "success".equals(result);
            } else {
                logger.error("Response body is null");
                return false;
            }
        } else {
            logger.error("Request failed with status code: {}", response.getStatusCode());
            return false;
        }
    }

    @PostMapping(
            value = "/aliCaptcha",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<?>> verifyAliCaptcha(
            @RequestBody AliCaptchaRequest request
    ) {
        String lotNumber = request.getLotNumber();
        String captchaOutput = request.getCaptchaOutput();
        String passToken = request.getPassToken();
        String genTime = request.getGenTime();
        String captchaId = request.getCaptchaId();

        // 生成签名使用标准的hmac算法，使用用户当前完成验证的流水号lot_number作为原始消息message，使用客户验证私钥作为key
        // 采用sha256散列算法将message和key进行单向散列生成最终的签名
        String signToken = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, aliCaptchaKey).hmacHex(lotNumber);

        String url = "https://captcha.alicaptcha.com/validate";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("lot_number", lotNumber);
        formData.add("captcha_output", captchaOutput);
        formData.add("pass_token", passToken);
        formData.add("gen_time", genTime);
        formData.add("captcha_id", captchaId);
        formData.add("sign_token", signToken);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        AliCaptchaResponse successResponse = objectMapper.readValue(responseBody, AliCaptchaResponse.class);
                        return ResponseEntity.ok(successResponse);
                    } catch (Exception e) {
                        logger.error("Parsing failure", e);
                        return ResponseEntity.ok("验证失败");
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Request Error", e);
                    return Mono.just(ResponseEntity.ok("请求异常"));
                });
    }
}