package com.example.core.domain;

public enum GlobalErrorCode implements ErrorCode {
    INVALID_INPUT("Invalid input", "잘못된 입력입니다.", 400),
    NOT_FOUND("Not found", "대상을 찾을 수 없습니다.", 404),
    UNAUTHORIZED("Required Auth", "인증이 필요합니다.", 401),
    CONFLICT("Conflict", "요청이 현재 상태와 충돌합니다.", 409),
    INTERNAL_ERROR("Internal_error", "처리 중 오류가 발생했습니다.", 500);

    private final String code;
    private final String message;
    private final int status;

    GlobalErrorCode(String code, String message, int status) {
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
