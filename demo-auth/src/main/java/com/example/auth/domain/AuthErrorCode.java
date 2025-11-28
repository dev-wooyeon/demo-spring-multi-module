package com.example.auth.domain;

import com.example.core.domain.ErrorCode;

public enum AuthErrorCode implements ErrorCode {
    INVALID_CREDENTIAL("AUTH-401", "이메일 또는 패스워드가 올바르지 않습니다.", 401),
    INVALID_TOKEN("AUTH-498", "토큰이 손상되었거나 위조되었습니다.", 498),
    TOKEN_EXPIRED("AUTH-499", "토큰이 만료되었습니다.", 499),
    REFRESH_NOT_FOUND("AUTH-404", "리프레시 토큰을 찾을 수 없습니다.", 404);

    private final String code;
    private final String message;
    private final int status;

    AuthErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public int status() {
        return status;
    }
}
