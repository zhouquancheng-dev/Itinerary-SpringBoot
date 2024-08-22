package com.zqc.itineraryweb.entity.captcha;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GtCaptcha {
    private String status;
    private String result;
    private String reason;
    @SerializedName("captcha_args")
    private CaptchaArgs captchaArgs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CaptchaArgs {
        @SerializedName("used_type")
        private String usedType;
        @SerializedName("user_ip")
        private String userIp;
        @SerializedName("lot_number")
        private String lotNumber;
        private String scene;
        private String referer;
    }
}