# PostgreSQL Failover Test — 측정 자리

> 작성: 2026-05-21 / 실행 예정: bitnami HA 배포 후

## 0. 목적
T7.5 — Primary 강제 종료 시 자동 promotion 동작 + Application 회복 시간 측정.

## 1. 사전 준비
| 항목 | 명령 |
|------|------|
| HA cluster 배포 확인 | `kubectl --context=staging get pods -n giftify -l app.kubernetes.io/name=postgresql` |
| Primary/Replica 식별 | `psql -c "SELECT pg_is_in_recovery();"` (false=Primary) |
| Application replica 2개 ready | `kubectl get pods -l app=api-server` |
| k6 small load 준비 | `--vus 10 --duration 1m` |

## 2. 시나리오: Primary 강제 종료
```bash
START=$(date +%s)
kubectl --context=staging delete pod -n giftify postgres-postgresql-0 \
  --grace-period=0 --force

# 매 5초마다 promotion 상태 polling
while true; do
  IS_RECOVERY=$(kubectl exec -n giftify postgres-postgresql-1 -- \
    psql -U postgres -tAc "SELECT pg_is_in_recovery();")
  NOW=$(date +%s)
  echo "[$(( NOW - START ))s] postgres-postgresql-1.pg_is_in_recovery = $IS_RECOVERY"
  [ "$IS_RECOVERY" = "f" ] && break
  sleep 5
done
echo "Promotion completed at $(( NOW - START ))s"
```

## 3. 측정 결과 (실행 후 채움)

| 단계 | 측정값 | 기준 | 결과 |
|------|--------|------|------|
| Primary 종료 → Replica promotion | ?s | < 30s | ✅/❌ |
| Application reconnect (Hikari) | ?s | < 60s | ✅/❌ |
| k6 error rate (failover 중) | ?% | < 5% | ✅/❌ |
| 신규 Primary 트래픽 수용 | ?s | < 90s | ✅/❌ |

## 4. 회복 로그
```
[k6 출력 발췌]
[Application 재연결 로그 발췌]
```

## 5. 후속 액션 (실행 후 채움)
- [ ] Replication lag alert threshold 조정
- [ ] HikariCP connectionTimeout / validationTimeout 재조정 여부
- [ ] PgBouncer 등 connection pool proxy 도입 평가 (선택)

---
**상태**: 미실행 (도입 manifests + 의사결정 doc 만 완료)
