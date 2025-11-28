package com.example.member.application;

public record MemberRegisterCommand(String email, String name, String rawPassword) {
}
