# Cloud Run 배포 가이드

이 백엔드는 아래 환경으로 배포됩니다.

- 프론트엔드: Vercel
- 백엔드: GCP Cloud Run

## 아키텍처 주의사항

채팅은 Spring의 인메모리 심플 브로커를 사용합니다.

Cloud Run은 초기 배포에 사용할 수 있지만, 인스턴스가 2개 이상으로 스케일아웃되면 WebSocket 연결과 인메모리 메시지 상태가 인스턴스 간에 불일치가 발생합니다.

현재는 아래 설정으로 배포합니다:

- `--max-instances=1`

## 1. 필요한 GCP 서비스 활성화

```bash
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com
```

## 2. Artifact Registry 저장소 생성

```bash
gcloud artifacts repositories create highpass-artifacts \
  --repository-format=docker \
  --location=asia-northeast3
```

## 3. 최초 빌드 및 배포 (수동)

`highpass_backend` 디렉터리에서 실행합니다:

```bash
gcloud builds submit --config cloudbuild.yaml
```

이 설정으로 다음이 수행됩니다:

- 백엔드 이미지 빌드
- Artifact Registry에 푸시
- 기존 Cloud Run 서비스에 새 이미지 배포

## 4. Cloud SQL (MySQL) 인스턴스 생성

권장 초기 설정:

- 엔진: MySQL 8.0
- 리전: `asia-northeast3`
- 인스턴스명: `highpass-mysql`
- DB명: `highpassdb`

```bash
gcloud services enable sqladmin.googleapis.com

gcloud sql instances create highpass-mysql \
  --database-version=MYSQL_8_0 \
  --region=asia-northeast3 \
  --edition=enterprise \
  --cpu=1 \
  --memory=3840MB \
  --root-password=<ROOT_PASSWORD>

gcloud sql databases create highpassdb \
  --instance=highpass-mysql
```

생성 후 인스턴스 연결명을 확인합니다:

```bash
gcloud sql instances describe highpass-mysql \
  --format="value(connectionName)"
```

## 5. Cloud Run 최초 수동 배포

실제 값으로 교체하여 실행합니다.

```bash
gcloud run deploy highpass-backend \
  --image asia-northeast3-docker.pkg.dev/<PROJECT_ID>/highpass-artifacts/highpass-backend:<IMAGE_TAG> \
  --region asia-northeast3 \
  --platform managed \
  --allow-unauthenticated \
  --max-instances 1 \
  --add-cloudsql-instances <PROJECT_ID>:asia-northeast3:highpass-mysql \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod \
  --set-env-vars FRONTEND_URL=https://highpassfrontend.vercel.app \
  --set-env-vars SECURE_COOKIE=true \
  --set-env-vars SPRING_DATASOURCE_URL='jdbc:mysql:///highpassdb?cloudSqlInstance=<PROJECT_ID>:asia-northeast3:highpass-mysql&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul' \
  --set-env-vars SPRING_DATASOURCE_USERNAME=<DB_USER> \
  --set-env-vars SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD> \
  --set-env-vars JWT_SECRET_KEY=<JWT_SECRET_KEY> \
  --set-env-vars JWT_EXPIRATION=900000 \
  --set-env-vars PUBLIC_DATA_API_KEY=<PUBLIC_DATA_API_KEY> \
  --set-env-vars GOOGLE_CLIENT_ID=<GOOGLE_CLIENT_ID> \
  --set-env-vars GOOGLE_CLIENT_SECRET=<GOOGLE_CLIENT_SECRET> \
  --set-env-vars KAKAO_REST_API_KEY=<KAKAO_REST_API_KEY>
```

최초 수동 배포 이후에는 Cloud Run 서비스 설정이 서비스에 저장됩니다. 이후 CI/CD 배포는 이미지만 교체합니다.

## 6. Cloud Build 트리거 생성 (CI/CD)

권장 설정:

- 저장소: 연결된 GitHub 저장소
- 이벤트: 브랜치 push
- 브랜치: `main`
- 빌드 설정 파일: `highpass_backend/cloudbuild.yaml`

설정 후 동작:

- `main` 브랜치에 push
- Cloud Build 실행
- 백엔드 이미지 재빌드
- Cloud Run 서비스 `highpass-backend` 자동 업데이트

주의사항:

- 최초 수동 배포가 완료된 이후에만 트리거를 사용해야 합니다
- 환경변수와 Cloud SQL 연결은 Cloud Run 서비스에 그대로 유지됩니다

## 7. 프론트엔드 환경변수 설정

Vercel 프로젝트 설정에서:

- `NEXT_PUBLIC_FRONTEND_URL=https://highpassfrontend.vercel.app`
- `NEXT_PUBLIC_API_BASE_URL=https://<Cloud Run 도메인 또는 커스텀 도메인>`

환경변수 변경 후 프론트엔드를 재배포합니다.

카카오 소셜 로그인을 위해 카카오 개발자 콘솔에서도 업데이트 필요:

- 사이트 도메인: `https://highpassfrontend.vercel.app`
- Redirect URI: `https://<백엔드 도메인>/login/oauth2/code/kakao`

## 8. 배포 확인

아래 항목을 점검합니다:

- `/api-docs` 접근 가능 여부
- `/swagger-ui.html` 접근 가능 여부
- Vercel 프론트엔드에서 로그인 후 쿠키 발급 확인
- 로그인 전 `/api/users/me` → `401` 반환 (302 리다이렉트 금지)

## 9. 운영 환경 동작 참고사항

- `env.properties`는 로컬 개발 전용입니다. Cloud Run은 최초 배포 또는 Secret Manager에서 값을 받습니다.
- `SPRING_PROFILES_ACTIVE=prod` 설정 시 Flyway 마이그레이션이 실행되고 Hibernate는 `validate` 모드로 동작합니다.
- `SECURE_COOKIE=true`는 Cloud Run이 HTTPS로 서비스되므로 반드시 설정해야 합니다.
- CORS는 `FRONTEND_URL`을 기준으로 동작하므로 Vercel origin과 정확히 일치해야 합니다.
- Cloud Build 트리거는 이미지만 교체하며 기존 서비스 설정을 변경하지 않습니다.
