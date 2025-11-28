DDD 기반 멀티모듈 보일러플레이트 설계
=======================================

- [x] 루트/모듈 Gradle 정비 (Java 25, Spring Boot 4.0.0, 공통/모듈별 의존성 분리)
- [x] 모듈 생성: demo-core(공통), demo-member(회원), demo-auth(인증), demo-api(실행/인터페이스)
- [x] 도메인 코드: 회원 등록·조회, 인증/토큰 발급, 패스워드 해싱 구현
- [x] REST API: 회원 가입/조회, 로그인/토큰 재발급, 예외/검증 응답 일관화
- [x] 기본 테스트 or 샘플 검증 시나리오 추가
- [x] README/사용 가이드 및 실행 방법 기술

모듈 설계 (DDD + SOLID)
-----------------------

| 모듈 | 책임 | 의존 |
| --- | --- | --- |
| demo-core | 공통 도메인 규약, 예외, 시간/ID 인터페이스, 베이스 엔티티 | - |
| demo-member | 회원 도메인(엔티티/값객체/도메인 서비스), JPA 어댑터 | demo-core |
| demo-auth | 인증/토큰 도메인, 패스워드 검증, 토큰 프로바이더 | demo-core, demo-member |
| demo-api | Spring Boot 실행, REST 계층, 보안/인증 설정, 모듈 wiring | demo-member, demo-auth |

- 공통 의존성(모듈 전역): Lombok, validation, 테스트 공통 등은 루트 `subprojects`에서 일괄 지정.
- 2개 모듈만 쓰는 의존성은 개별 선언: `spring-boot-starter-security`(auth, api), `spring-security-crypto`(member, auth) 등.
- 각 도메인은 인터페이스(Port)와 구현(Adapter)로 나누고, 애플리케이션 서비스는 도메인 규칙을 묶는 유스케이스 단위로 제공.

의존 방향(모듈 & 레이어)
------------------------

```mermaid
flowchart LR
    subgraph API["demo-api (interface/app)"]
        C1[REST Controllers]
        C2[Security Config]
    end
    subgraph AUTH["demo-auth (domain+app)"]
        A1[AuthService\n(login, refresh)]
        A2[TokenProvider]
        A3[PasswordVerifier]
    end
    subgraph MEMBER["demo-member (domain+infra)"]
        M1[MemberAggregate]
        M2[MemberService]
        M3[JPA Adapter]
    end
    subgraph CORE["demo-core (shared)"]
        S1[DomainError/Exception]
        S2[BaseEntity/Audit]
        S3[TimeProvider]
    end

    C1 --> A1
    C2 --> A1
    A1 --> M2
    M2 --> M1
    M3 --> M2
    M1 --> CORE
    A2 --> CORE
    A3 --> CORE
    API --> AUTH
    AUTH --> MEMBER
    MEMBER --> CORE
```

도메인 유스케이스 스케치
-----------------------

- 회원 가입: 이메일 중복 검사 → 패스워드 해싱 → 도메인 이벤트(optional) → 저장.
- 회원 조회: ID/이메일 기준 조회 → 존재하지 않을 경우 도메인 예외 반환.
- 로그인: 이메일로 회원 조회 → 패스워드 매칭 → 토큰 발급(액세스/리프레시) → 리프레시 저장소(optional) 업데이트.
- 토큰 재발급: 리프레시 검증 → 새 액세스/리프레시 발행.

작업 순서 (실행 가능 항목부터)
-----------------------------

1. [x] Gradle 멀티모듈 설정 수정: `settings.gradle` 확장, 루트 `build.gradle` 정리.
2. [x] 모듈 스캐폴드: `core`, `member`, `auth`, `api`에 빌드 스크립트/소스 디렉터리 생성.
3. [x] 공통 코드 추가: `core`에 예외/베이스 엔티티/시간 인터페이스.
4. [x] 회원 도메인 구현: 엔티티, 값객체, 리포지터리 포트, 서비스, JPA 어댑터.
5. [x] 인증 도메인 구현: 인증 서비스, 패스워드 검증, 토큰 프로바이더(서명/HMAC), 세션/리프레시 관리.
6. [x] API 계층: Spring Boot 앱, 설정, REST 컨트롤러, DTO 매퍼, 예외 처리기.
7. [x] 샘플 테스트/검증 시나리오 추가 후 빌드 시도.
8. [x] README/사용 가이드 정리.

다음에 할 일
-----------
- [ ] 서비스/컨트롤러 슬라이스 테스트 추가 (member/auth/api)
- [ ] 토큰 프로바이더 단위 테스트(HMAC 서명/만료/역직렬화)
- [ ] Lombok 리팩터링(POJO 보일러플레이트 정리)
- [ ] 리프레시 토큰 저장소를 Redis/JPA 구현으로 확장
