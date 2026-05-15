# 백엔드 배포 가이드

백엔드는 `Vercel + GCP Cloud Run` 환경으로 배포됩니다.

## 파일 구성

- `Dockerfile`: 멀티스테이지 Spring Boot 이미지 빌드
- `cloudbuild.yaml`: Cloud Build 빌드 + Cloud Run 배포 설정
- `env.properties`: 로컬 실행 환경 설정값
- `deploy/CLOUD_RUN.md`: Cloud Run 배포 상세 가이드

## 환경 설정 방식

- 로컬 개발: `env.properties`
- Cloud Run 운영: Cloud Run 환경변수 또는 Secret Manager

## CI/CD 흐름

1. 처음 한 번은 필요한 환경변수를 모두 설정해서 Cloud Run 수동 배포
2. GitHub `main` 브랜치에 Cloud Build 트리거 생성
3. 이후 `main` 푸시 시마다 이미지 자동 빌드 및 `highpass-backend` 재배포

트리거는 컨테이너 이미지만 교체하므로 Cloud Run에 설정된 아래 항목들은 유지됩니다:
- 환경변수
- Cloud SQL 연결
- 스케일링 설정
- 퍼블릭 액세스 설정

## 운영 환경변수 목록

백엔드는 `FRONTEND_URL`에 프론트엔드 origin을 요구합니다.  
이 값은 배포된 Vercel origin과 정확히 일치해야 CORS, OAuth 리다이렉트, 쿠키 정책이 정상 동작합니다.

| 환경변수 | 값 예시 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `FRONTEND_URL` | `https://highpassfrontend.vercel.app` |
| `SECURE_COOKIE` | `true` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql:///highpassdb?cloudSqlInstance=<PROJECT:REGION:INSTANCE>&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul` |
| `SPRING_DATASOURCE_USERNAME` | `<운영 DB 유저>` |
| `SPRING_DATASOURCE_PASSWORD` | `<운영 DB 비밀번호>` |
| `JWT_SECRET_KEY` | `<운영 시크릿>` |
| `JWT_EXPIRATION` | `900000` (15분, ms 단위) |
| `GOOGLE_CLIENT_ID` | `<Google OAuth 클라이언트 ID>` |
| `GOOGLE_CLIENT_SECRET` | `<Google OAuth 시크릿>` |
| `KAKAO_REST_API_KEY` | `<카카오 REST API 키>` |
| `PUBLIC_DATA_API_KEY` | `<공공데이터포털 API 키>` |

`prod` 프로파일에서는 Flyway로 DB 마이그레이션이 실행되고 Hibernate는 `ddl-auto=validate`로 동작합니다.  
`SPRING_JPA_HIBERNATE_DDL_AUTO`는 의도적인 유지보수 작업 외에는 변경하지 마세요.

프론트엔드 도메인이 변경되면 Cloud Run의 `FRONTEND_URL`을 새 origin으로 업데이트해야 합니다.  
소셜 로그인 제공자에도 새 프론트엔드 origin과 백엔드 콜백 URI를 허용 목록에 추가해야 합니다.

## 주의사항

- `server.forward-headers-strategy=framework` 설정으로 Cloud Run 뒤에서 리다이렉트와 쿠키가 정상 동작합니다.
- `server.port=${PORT:8080}` 설정으로 Cloud Run 런타임 포트에 바인딩됩니다.
- `/api/**` 하위 인증 실패 시 OAuth 로그인 페이지로 리다이렉트하지 않고 `401`을 반환합니다.
- Cloud Run 상세 배포 절차는 [deploy/CLOUD_RUN.md](./deploy/CLOUD_RUN.md)를 참고하세요.
