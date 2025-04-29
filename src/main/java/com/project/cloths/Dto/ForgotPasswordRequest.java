package com.project.cloths.Dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private Header header;
    private Body body;

    @Data
    public static class Header {
        private String requestId;
    }

    @Data
    public static class Body {
        private String email;
    }
}