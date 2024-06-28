package com.zqc.itineraryweb.entity.captcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AliCaptchaRequest {
    @JsonProperty("lot_number") private String lotNumber;
    @JsonProperty("captcha_output") private String captchaOutput;
    @JsonProperty("pass_token") private String passToken;
    @JsonProperty("gen_time") private String genTime;
    @JsonProperty("captcha_id") private String captchaId;
}
