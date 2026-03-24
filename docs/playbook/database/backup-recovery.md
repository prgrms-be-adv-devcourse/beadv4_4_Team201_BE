# PostgreSQL Backup & Recovery Runbook

## 1. Backup Architecture

```
+-----------------------------------------------------------+
|  Daily Automated Backup (CronJob: postgres-backup)        |
+-----------------------------------------------------------+
|                                                           |
|  Schedule: 09:30 KST (UTC 00:30)                         |
|  Format:   pg_dump -Fc (Custom, zlib compressed)          |
|  Storage:  gs://giftify-db-backups/{env}/                 |
|  Retention: 14 days (GCS Lifecycle Policy)                |
|                                                           |
|  +-------+    pg_dump    +----------+   gsutil   +------+ |
|  | PG DB | ------------> | /tmp/*.dump | -------> | GCS  | |
|  +-------+               +----------+            +------+ |
|                                                           |
|  Image: google/cloud-sdk:alpine + postgresql16-client     |
|  Schema: g7app (Module-Aware Flyway)                      |
+-----------------------------------------------------------+
```

| Item | Value |
|------|-------|
| CronJob | `postgres-backup` (`infra/k3s/base/apps/postgres/backup-cronjob.yaml`) |
| Schedule | `30 0 * * *` (UTC) = 09:30 KST |
| GCS Bucket | `gs://giftify-db-backups/` |
| Path | `{env}/giftify_db_{YYYY-MM-DD_HHMMSS}.dump` |
| Retention | 14 days (GCS Lifecycle) |
| backoffLimit | 2 |
| activeDeadlineSeconds | 900 (15min) |

---

## 2. Manual Backup (Emergency)

prod VM에서 실행:

```bash
gcloud compute ssh giftify-app --zone=asia-northeast3-a --tunnel-through-iap

# CronJob에서 수동 Job 생성 (가장 안전한 방법)
kubectl create job --from=cronjob/postgres-backup manual-backup-$(date +%Y%m%d-%H%M) -n giftify

# Job 완료 대기
kubectl wait --for=condition=complete job/manual-backup-* -n giftify --timeout=300s

# 결과 확인
kubectl logs job/manual-backup-* -n giftify

# Job 정리
kubectl delete job manual-backup-* -n giftify
```

---

## 3. Recovery Procedure

### Stage 1: Test Recovery (non-production)

staging VM에서 임시 Pod으로 복원하여 데이터 무결성 먼저 검증.

```bash
gcloud compute ssh giftify-staging --zone=asia-northeast3-a --tunnel-through-iap

# 1. 임시 PostgreSQL Pod 생성
cat <<'EOF' | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: postgres-restore-test
  namespace: giftify
spec:
  restartPolicy: Never
  containers:
    - name: postgres
      image: postgres:16-alpine
      env:
        - name: POSTGRES_PASSWORD
          value: "test-restore-password"
        - name: POSTGRES_USER
          value: "giftify"
        - name: POSTGRES_DB
          value: "giftify_restore_test"
      resources:
        limits:
          memory: "512Mi"
          cpu: "500m"
EOF
kubectl wait --for=condition=ready pod/postgres-restore-test -n giftify --timeout=60s

# 2. 백업 다운로드 + Pod 복사 + 복원
LATEST=$(gsutil ls gs://giftify-db-backups/prod/ | sort | tail -1)
echo "Restoring from: $LATEST"
gsutil cp $LATEST /tmp/backup.dump
kubectl cp /tmp/backup.dump giftify/postgres-restore-test:/tmp/backup.dump

kubectl exec postgres-restore-test -n giftify -- sh -c "
  apk add --no-cache postgresql16-client &&
  PGPASSWORD=test-restore-password pg_restore \
    -h localhost -U giftify -d giftify_restore_test \
    --no-owner --no-acl /tmp/backup.dump
"

# 3. 데이터 무결성 검증 (Verification Checklist 참조)

# 4. 정리
kubectl delete pod postgres-restore-test -n giftify
rm -f /tmp/backup.dump
```

### Stage 2: Downtime Notification

```
Discord #alerts:
  "[긴급 점검] 데이터베이스 복구 작업
   시작: {시간}
   예상 종료: {시간} (약 15분)
   사유: {사유}"
```

### Stage 3: Production Recovery

```bash
gcloud compute ssh giftify-app --zone=asia-northeast3-a --tunnel-through-iap

# 1. api-server scale down (DB connection 차단)
kubectl scale deployment api-server --replicas=0 -n giftify

# 2. 대상 백업 파일 다운로드
TARGET="gs://giftify-db-backups/prod/giftify_db_YYYY-MM-DD_HHMMSS.dump"
gsutil cp $TARGET /tmp/restore.dump

# 3. Pod 내부에서 복원
kubectl exec -it postgres-0 -n giftify -- sh -c "
  apk add --no-cache postgresql16-client google-cloud-sdk
"
kubectl cp /tmp/restore.dump giftify/postgres-0:/tmp/restore.dump

kubectl exec -it postgres-0 -n giftify -- sh -c "
  pg_restore \
    -h localhost -U giftify -d giftify_db \
    --clean --if-exists \
    --no-owner --no-acl \
    /tmp/restore.dump &&
  rm -f /tmp/restore.dump
"

# 4. api-server scale up
kubectl scale deployment api-server --replicas=1 -n giftify
kubectl wait --for=condition=ready pod -l app=api-server -n giftify --timeout=120s

# 5. Health check
kubectl exec -it deployment/api-server -n giftify -- \
  curl -sf http://localhost:8080/actuator/health
```

---

## 4. Verification Checklist

Stage 1 (test) 및 Stage 3 (prod) 복원 후 반드시 확인:

- [ ] pg_restore exit code = 0
- [ ] 핵심 테이블 존재 (search_path = g7app):

```sql
SET search_path TO g7app;
SELECT 'members' AS tbl, COUNT(*) FROM members
UNION ALL SELECT 'products', COUNT(*) FROM products
UNION ALL SELECT 'wishlists', COUNT(*) FROM wishlists
UNION ALL SELECT 'fundings', COUNT(*) FROM fundings
UNION ALL SELECT 'orders', COUNT(*) FROM orders
UNION ALL SELECT 'payments', COUNT(*) FROM payments
UNION ALL SELECT 'wallets', COUNT(*) FROM wallets
ORDER BY tbl;
```

- [ ] 각 테이블 레코드 수 > 0
- [ ] Flyway schema history 일관성:

```sql
SELECT installed_rank, version, description, success
FROM g7app.flyway_schema_history
ORDER BY installed_rank DESC LIMIT 5;
```

- [ ] (Stage 3 only) API health check 성공
- [ ] (Stage 3 only) 주요 API 동작 확인 (상품 검색, 위시리스트 조회)

---

## 5. Emergency Contacts

| Role | Name | GitHub |
|------|------|--------|
| Infra Lead | chan99 | @chan99k |
| Team | team201.sy | - |
| Team | team201.yk | - |
| Team | satoru18704 | - |

---

## 6. Recovery History

| Date | Operator | Reason | Downtime | Result |
|------|----------|--------|----------|--------|
| 2026-03-24 | chan99 | T2.4 Test recovery (staging) | 0min | Success - 254.7KB, 7 tables verified (members:6, products:91, wishlists:6, fundings:4, orders:5, payments:3, wallets:6). Schema: g7app |
