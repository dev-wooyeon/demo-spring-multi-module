package com.example.auth.application;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.token")
public class AuthProperties {

    /**
     * HMAC 서명용 비밀키. 운영에서는 환경변수/비밀관리자를 통해 주입.
     */
    private String hmacSecret = "change-me-in-prod";

    /**
     * 액세스 토큰 TTL.
     */
    private Duration accessTtl = Duration.ofMinutes(15);

    /**
     * 리프레시 토큰 TTL.
     */
    private Duration refreshTtl = Duration.ofDays(7);
}
