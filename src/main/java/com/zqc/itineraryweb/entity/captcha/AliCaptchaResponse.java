package com.zqc.itineraryweb.entity.captcha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AliCaptchaResponse {
    private String status;
    private String result;
    private String reason;
    @JsonProperty("captcha_args")
    private CaptchaArgs captchaArgs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CaptchaArgs {
        @JsonProperty("used_type")
        private String usedType;
        @JsonProperty("user_ip")
        private String userIp;
        @JsonProperty("lot_number")
        private String lotNumber;
        private String scene;
        private String referer;
    }
}