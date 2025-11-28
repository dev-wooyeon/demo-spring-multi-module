# DDD Multi-Module Boilerplate (Spring Boot 4, Java 25)

멀티모듈/DDD 샘플 보일러플레이트입니다. 회원 도메인과 인증 도메인을 분리하고, SOLID/클린 코드 원칙을 따르는 기본 구조를 제공합니다.

## 모듈 구성
- `demo-core`: 공통 베이스 엔티티/에러/시간 인터페이스.
- `demo-member`: 회원 도메인, 패스워드 해싱, JPA 어댑터.
- `demo-auth`: 로그인/토큰 발급·재발급, 리프레시 저장소, HMAC 서명 토큰 프로바이더.
- `demo-api`: 실행 모듈. REST API, 시큐리티 설정, 예외 처리, H2 데모 설정.

공통 의존성(lombok/validation/test)은 루트에서 일괄 적용. 둘 이상 모듈만 사용하는 의존성은 각 모듈에만 선언(`spring-security-crypto`, `spring-boot-starter-data-jpa` 등).

## 실행
```bash
./gradlew :api:bootRun
```
기본 DB는 H2 메모리(`jdbc:h2:mem:demo`). 토큰 서명키/TTL은 `api/src/main/resources/application.yml`에서 조정.

## 주요 흐름
1) 회원 가입 `POST /api/members` -> 이메일 중복 검사 -> 패스워드 해싱 -> 저장  
2) 로그인 `POST /api/auth/login` -> 패스워드 검증 -> 액세스/리프레시 토큰 발급(HMAC)  
3) 토큰 재발급 `POST /api/auth/refresh` -> 리프레시 검증/저장소 확인 -> 새 토큰 교체  
4) 보호된 API 예시 `GET /api/members/{id}` -> `Authorization: Bearer <access>` 필요  

### 예시 요청
```bash
# 회원 가입
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","name":"Demo User","password":"passw0rd!"}'

# 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"passw0rd!"}'

# 보호 리소스 조회 (accessToken 치환)
curl -H "Authorization: Bearer <accessToken>" http://localhost:8080/api/members/1
```

## 구조 참고
- 설계/작업 계획 및 모듈 의존 그래프: `docs/boilerplate-plan.md` (mermaid 포함)
- DDD 포트/어댑터: `member`의 `MemberRepository` 포트 + `MemberRepositoryAdapter`, `auth`의 `TokenProvider` 포트 + `HmacTokenProvider`.
- 인증 필터: `TokenAuthenticationFilter`가 Bearer 액세스 토큰을 검증하고 `SecurityContext`에 주입.

## 다음 단계 아이디어
- JPA 감사(BaseEntity) 확장 및 이벤트 발행 추가
- 토큰 저장소를 Redis/JPA 구현으로 교체
- 테스트 추가: 서비스/컨트롤러 슬라이스 테스트, 토큰 프로바이더 단위 테스트
