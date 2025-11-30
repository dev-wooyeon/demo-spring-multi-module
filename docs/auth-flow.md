# 인증/인가 플로우 (Mermaid)

## 회원 가입 및 활성화
```mermaid
sequenceDiagram
    participant Client
    participant API as API (MemberController)
    participant MemberSvc as MemberCommandService
    participant Notifier as ActivationNotifier
    participant DB as DB (members)

    Client->>API: POST /api/members (email, name, password)
    API->>MemberSvc: register(command)
    MemberSvc->>DB: email 중복 검사 / 저장(status=PENDING, activationCode, expiresAt)
    MemberSvc->>Notifier: notify(email, code, expiresAt)
    API-->>Client: PENDING 상태 응답

    Client->>API: POST /api/members/activate (email, code)
    API->>MemberActivationService: activate(command)
    MemberActivationService->>DB: 회원 조회 + 코드/만료 검증 + status=ACTIVE
    API-->>Client: ACTIVE 상태 응답
```

## 로그인과 토큰 발급
```mermaid
sequenceDiagram
    participant Client
    participant API as API (AuthController)
    participant AuthSvc as AuthService
    participant MemberRepo as MemberRepository
    participant TokenProv as TokenProvider (HMAC)
    participant RefreshStore as JpaRefreshTokenStore

    Client->>API: POST /api/auth/login (email, password)
    API->>AuthSvc: login(command)
    AuthSvc->>MemberRepo: findByEmail
    AuthSvc->MemberRepo: status ACTIVE/LOCKED 검사
    AuthSvc->AuthSvc: passwordHasher.matches
    AuthSvc->TokenProv: create(access payload)
    AuthSvc->TokenProv: create(refresh payload)
    AuthSvc->RefreshStore: store(refreshToken, payload)
    AuthSvc->MemberRepo: touchLogin + save
    API-->>Client: accessToken, refreshToken, accessTokenExpiresAt
```

## 보호 API 접근 (액세스 토큰)
```mermaid
sequenceDiagram
    participant Client
    participant Filter as TokenAuthenticationFilter
    participant TokenProv as TokenProvider
    participant API as Protected Controller

    Client->>Filter: Authorization: Bearer <access>
    Filter->>TokenProv: verify(token)
    TokenProv-->>Filter: TokenPayload (type=ACCESS, roles, email)
    Filter->>Filter: ROLE_* 권한 구성
    Filter->>API: SecurityContext 전달
    API-->>Client: 200 OK (인가 성공)

    Note over Client,Filter: 토큰 미제공/만료/서명 오류 시 401
```

## 리프레시 토큰 재발급
```mermaid
sequenceDiagram
    participant Client
    participant API as API (AuthController)
    participant AuthSvc as AuthService
    participant TokenProv as TokenProvider
    participant RefreshStore as JpaRefreshTokenStore

    Client->>API: POST /api/auth/refresh (refreshToken)
    API->>AuthSvc: refresh(command)
    AuthSvc->TokenProv: verify(refreshToken)
    AuthSvc->RefreshStore: find(refreshToken)
    AuthSvc->AuthSvc: 새 access/refresh payload 생성
    AuthSvc->TokenProv: create(new access)
    AuthSvc->TokenProv: create(new refresh)
    AuthSvc->RefreshStore: store(new refresh) & remove(old)
    API-->>Client: 새 accessToken/refreshToken
```

## 역할/상태 기준 인가 정책
- 회원 상태: PENDING/LOCKED면 로그인·재발급 차단, ACTIVE만 허용.
- 역할→권한 매핑: TokenAuthenticationFilter가 `ROLE_<role>`로 주입. `@PreAuthorize("hasRole('USER')")` 등으로 컨트롤러 보호.
- 예외/응답: DomainException을 ApiExceptionHandler가 상태 코드로 매핑, 토큰 오류는 필터에서 401 처리.
