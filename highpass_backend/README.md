# Highpass Backend

한국 자격증 시험 준비 플랫폼 **Highpass**의 백엔드 서버입니다.

Spring Boot 기반 REST API + WebSocket STOMP 채팅 서버로, 포트 **8080**에서 실행됩니다.

---

## 기술 스택

| 구분 | 내용 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| DB (prod) | MySQL 8 + Flyway 마이그레이션 |
| DB (local) | MySQL (localhost:3310, HeidiSQL) |
| 보안 | Spring Security, JWT (jjwt 0.13), OAuth2 (Google · Kakao) |
| 실시간 | WebSocket STOMP (`/ws-stomp`) |
| 배포 | GCP Cloud Run + Cloud SQL (MySQL) |

---

## 로컬 실행

### 사전 조건

- Java 17+
- `highpass_backend/env.properties` 파일 (민감 정보 설정)

### env.properties 필수 키

```properties
JWT_SECRET_KEY=...
JWT_EXPIRATION=...
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
KAKAO_REST_API_KEY=...
PUBLIC_DATA_API_KEY=...
ADMIN_EMAIL=admin@highpass.local
ADMIN_PASSWORD=Admin1234!
ADMIN_NICKNAME=관리자
```

### 실행

```bash
./gradlew bootRun
```

---

## API 구조

| 경로 | 컨트롤러 | 설명 |
|---|---|---|
| `/api/auth/**` | AuthController, TokenController | 회원가입·로그인·토큰 재발급 |
| `/api/oauth2/**` | OAuth2Controller | Google·Kakao 소셜 로그인 후처리 |
| `/api/users/**` | UserController | 프로필 조회·수정 |
| `/api/user-certificates/**` | UserCertificateController | 사용자 자격증 관리 |
| `/api/boards/**` | FreeBoardController | 자유게시판 |
| `/api/study/**` | StudyBoardController | 스터디 모집 게시판 |
| `/api/likes/**` | BoardLikeController | 좋아요 |
| `/api/comments/**` | CommentController | 댓글 |
| `/api/calendar/**` | CalendarController, HolidayController | 캘린더·공휴일 |
| `/api/todos/**` | TodoListController | 할일 |
| `/api/certificates/**` | CertificateController | 자격증 정보·일정 (공공데이터 API) |
| `/api/notifications/**` | NotificationController | 알림 조회·설정 |
| `/api/reports/**` | ReportController | 신고 |
| `/api/admin/**` | AdminController | 관리자 기능 (`ADMIN` 역할 필수) |
| `/api/kakao/**` | KakaoTokenController | 카카오 캘린더 OAuth 토큰 |

### WebSocket STOMP

- 엔드포인트: `/ws-stomp`
- 클라이언트 발행: `/pub/chat/message`
- 메시지 구독: `/sub/chat/room/{roomId}`
- 알림 구독: `/sub/notifications/{userId}`

---

## 인증 구조

- 로그인 시 Access Token(15분)과 Refresh Token(14일)을 **HttpOnly 쿠키**로 발급
- Access Token에 `userId`, `email`, `role` 클레임 포함 → Spring Security 권한(`ROLE_USER` / `ROLE_ADMIN`) 자동 설정
- 모든 쓰기 API는 `@AuthenticationPrincipal CustomJwtPrincipal`로 사용자 식별 (path의 userId 신뢰 금지)
- `/api/admin/**`는 `ROLE_ADMIN` 권한 필수 (일반 유저 403)
- STOMP 연결은 `StompHandler`에서 JWT 검증

---

## DB 마이그레이션 (Flyway)

```
src/main/resources/db/migration/V{YYYYMMDD}_{NN}__{설명}.sql
```

- prod 환경에서만 Flyway 활성화 (`SPRING_FLYWAY_ENABLED=true`)
- 로컬은 `ddl-auto: update`로 자동 스키마 갱신

---

## 배포

[DEPLOYMENT.md](./DEPLOYMENT.md) 참고 (GCP Cloud Run + Cloud Build CI/CD)

