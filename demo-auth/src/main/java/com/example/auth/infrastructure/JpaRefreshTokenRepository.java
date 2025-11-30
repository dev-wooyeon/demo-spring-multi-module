package com.example.auth.infrastructure;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface JpaRefreshTokenRepository extends JpaRepository<JpaRefreshToken, String> {

    Optional<JpaRefreshToken> findByTokenAndExpiresAtAfter(String token, Instant now);

    @Modifying
    @Query("delete from JpaRefreshToken t where t.expiresAt <= :now")
    void deleteExpired(@Param("now") Instant now);
}
