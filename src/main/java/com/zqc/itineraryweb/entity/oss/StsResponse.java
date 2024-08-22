package com.zqc.itineraryweb.entity.oss;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StsResponse {

    @JsonProperty("RequestId")
    private String requestId;

    @JsonProperty("AssumedRoleUser")
    private AssumedRoleUser assumedRoleUser;

    @JsonProperty("Credentials")
    private Credentials credentials;

    @Data
    public static class AssumedRoleUser {
        @JsonProperty("AssumedRoleId")
        private String assumedRoleId;
        @JsonProperty("Arn")
        private String arn;
    }

    @Data
    public static class Credentials {
        @JsonProperty("SecurityToken")
        private String securityToken;
        @JsonProperty("Expiration")
        private String expiration;
        @JsonProperty("AccessKeySecret")
        private String accessKeySecret;
        @JsonProperty("AccessKeyId")
        private String accessKeyId;
    }
}
