# Image Storage Decision (MS2 W5)

본 문서는 Giftify 백엔드의 이미지 저장소 백엔드 결정을 기록한다.
Roadmap T5.1 의 결과물이며, 이후 T5.2-T5.7 의 모든 구현은 본 결정을
전제로 한다.

## 결정 — MinIO 유지 + AWS SDK 통합

```
                  AWS SDK v2 (S3Client / S3Presigner)
                              │
            ┌─────────────────┼─────────────────┐
            │                 │                 │
       endpoint=             endpoint=        endpoint=
   http://minio:9000   http://minio.staging  https://s3.amazonaws.com
       (local/dev/      (staging k3s)         (future prod)
        prod-portfolio)
```

- **포폴 / staging / 현행 prod**: k3s 내 MinIO StatefulSet 그대로 사용.
- **SDK**: `software.amazon.awssdk:s3` + `software.amazon.awssdk:s3-presigner`
  v2 — MinIO 는 S3 wire-protocol 호환이라 endpoint override 만으로 동작.
- **장래 전환**: 진짜 AWS S3 로 가더라도 `S3Config` 에서 endpoint /
  credentials 만 바뀌고 application 코드는 무변경.

## 결정 사유

### 비용 / 운영 / 추상화 비교

| 항목 | MinIO (선택) | GCS | AWS S3 native |
|---|---|---|---|
| 비용 (포폴) | k3s 자원만 (0원) | $0.020/GB-mo + egress | $0.023/GB-mo + egress |
| 운영 (포폴) | StatefulSet + PVC 백업 | 관리형 | 관리형 |
| Pre-signed URL | ✅ | ✅ (signed URL) | ✅ |
| Latency (same cluster) | ~5ms | ~50ms (네트워크) | ~50ms |
| SDK 일치 | AWS SDK v2 | google-cloud-storage | AWS SDK v2 |
| 향후 S3 전환 비용 | endpoint 만 교체 | SDK 전부 교체 | 동일 |

### 결정 근거

1. **포폴 비용**: 0원. GCS / S3 native 는 트래픽 발생 시 청구 가능성.
2. **SDK 추상화 일치**: AWS SDK v2 를 쓰면 *MinIO ↔ S3 전환 시 코드 무변경*.
   GCS 는 SDK 전체 교체 비용.
3. **운영 부담**: MinIO StatefulSet 은 이미 k3s 에 떠 있고 정상 동작 중.
   추가 자원 0.
4. **레이턴시**: same-cluster MinIO 가 외부 GCS/S3 대비 우위 (~5ms vs ~50ms).
   이미지 업/다운은 작은 latency 차이도 UX 에 즉시 반영.

### 절충 / 제약

- **단점 1**: MinIO PVC 백업 정책 별도 (T6/MS3 백업 도메인에서 처리).
- **단점 2**: prod scale-out 시 MinIO 분산 모드 또는 S3 전환 필요. 현재
  단일 노드 PVC 라 향후 *진짜 prod 트래픽* 발생 전에 의사결정 재방문.
- **GCS 사용 영역 (별도 도메인)**: 다른 영역에서 GCS 를 쓰는 부분이
  있더라도 *이미지 저장소* 도메인은 본 결정에 따라 MinIO 로 통일. 다른
  도메인 정렬은 별도 spec.

## 구현 계획 (T5.2-T5.7)

| Task | 결과물 | 위치 |
|---|---|---|
| T5.2 | `ImageStorageService` 인터페이스 + record DTO | `bc/catalog/.../image/application/` |
| T5.3 | `S3Config` (S3Client / S3Presigner 빈) | `support/common/.../config/` |
| T5.3 | `AwsS3ImageStorageAdapter` (MinIO endpoint 호환) | `bc/catalog/.../image/infrastructure/` |
| T5.4 | `ImageController` — presigned PUT upload + GET download | `bootstrap/api-server/.../image/` |
| T5.5 | imageKey 명명 규칙 명세 | 본 문서 갱신 |
| T5.6 | 통합 e2e (staging 배포 후) | 운영 절차 |
| T5.7 | Frontend 협의 (URL 패턴 공유) | 별도 |

## imageKey 명명 규칙 (T5.5)

```
products/{productId}/{uuid}.{ext}
profiles/{memberId}/{uuid}.{ext}
```

- prefix 로 도메인 구분 → bucket 정책 다르게 가능
- `{uuid}` 충돌 방지 + 보안 (추측 불가)
- `{ext}` 는 클라이언트 hint, 서버는 신뢰하지 않음 (Content-Type 으로 검증)

## 환경 변수 매핑

기존 `infra/k3s/base/apps/api-server/configmap.yaml` 의 환경변수를 그대로
사용:

| 환경변수 | 용도 | 예시 |
|---|---|---|
| `S3_ENDPOINT` | MinIO 엔드포인트 | `http://minio:9000` |
| `S3_IMAGE_BUCKET` | 이미지 버킷 이름 | `giftify-images` |
| `S3_REGION` | SDK region (MinIO 도 형식상 필요) | `us-east-1` |
| `S3_ACCESS_KEY` | MinIO root user (Secret) | TBD |
| `S3_SECRET_KEY` | MinIO root password (Secret) | TBD |

application.yml 매핑은 T5.3 commit 에서 추가.

## 관련

- Roadmap MS2 T5.1-T5.7
- 인프라: `infra/k3s/base/apps/minio/` (StatefulSet, Service, init-job)
- 향후: prod 전환 시 본 문서 갱신
