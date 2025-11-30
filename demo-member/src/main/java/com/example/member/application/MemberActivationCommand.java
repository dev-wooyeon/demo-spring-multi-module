package com.example.member.application;

public record MemberActivationCommand(String email, String code) {
}
