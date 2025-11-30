package com.example.member.domain;

import com.example.core.domain.ErrorCode;

public enum MemberErrorCode implements ErrorCode {
    EMAIL_DUPLICATED("MEMBER-409", "이미 사용 중인 이메일입니다.", 409),
    MEMBER_NOT_FOUND("MEMBER-404", "회원을 찾을 수 없습니다.", 404),
    MEMBER_LOCKED("MEMBER-423", "잠긴 회원입니다.", 423),
    MEMBER_INACTIVE("MEMBER-403", "활성화되지 않은 회원입니다.", 403),
    INVALID_PASSWORD("MEMBER-401", "패스워드가 올바르지 않습니다.", 401),
    ACTIVATION_INVALID("MEMBER-422", "인증 코드가 올바르지 않습니다.", 422),
    ACTIVATION_EXPIRED("MEMBER-410", "인증 코드가 만료되었습니다.", 410),
    MEMBER_ALREADY_ACTIVE("MEMBER-409", "이미 활성화된 회원입니다.", 409);

    private final String code;
    private final String message;
    private final int status;

    MemberErrorCode(String code, String message, int status) {
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
