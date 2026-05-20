# Stateless Multi-Pod Verification — 측정 자리

> 작성: 2026-05-21 / T8.3

## 0. 목적
api-server replicas=2 환경에서 동일 사용자의 연속 요청이 다른 Pod 으로 분산되어도
상태 일관성이 유지되는지 검증.

## 1. 검증 포인트
| 항목 | 위험 | 검증 방법 |
|------|------|----------|
| JWT validation | 낮 (stateless 본질) | Pod 분산 후 동일 토큰 200 OK 확인 |
| Token blacklist (Redis 공유) | **높** | Pod A logout → Pod B 동일 토큰 사용 시 401 |
| Idempotency key (Redis 공유) | 중 | Pod A POST → Pod B 재요청 시 첫 응답 반환 |
| HTTP session | N/A | session 미사용 (verified) |
| In-memory cache | 중 | 로컬 ConcurrentMapCache 대신 Redis 사용 verified |

## 2. e2e 시나리오
```bash
TOKEN=$(curl -s -X POST https://staging-auth.../oauth/token ... | jq -r .access_token)

# 동일 user 로 20회 요청
for i in {1..20}; do
  curl -s -H "Authorization: Bearer $TOKEN" \
    http://giftify-api-staging.chan99k.dev/actuator/info \
    | jq -r '.pod // .hostname'
done | sort | uniq -c
# Expected: 두 Pod 분산 (예: api-server-xxx 10회, api-server-yyy 10회)
```

## 3. Blacklist 검증
```bash
# Pod A 에서 logout
curl -X POST .../api/v1/auth/logout -H "Authorization: Bearer $TOKEN"

# 다음 20회 요청은 모두 401 expected
for i in {1..20}; do
  STATUS=$(curl -o /dev/null -s -w "%{http_code}" \
    -H "Authorization: Bearer $TOKEN" .../api/v1/me)
  echo "$i: $STATUS"
done
# Expected: 20회 모두 401
```

## 4. 측정 결과 (실행 후 채움)

| 시나리오 | 측정값 | 기준 | 결과 |
|---------|--------|------|------|
| 동일 user 요청 Pod 분산 | ? % each | 40-60% 범위 | ✅/❌ |
| Logout 후 모든 Pod 401 | ?/20 | 20/20 | ✅/❌ |
| Idempotency 중복 처리 회피 | ?/N | N/N | ✅/❌ |

## 5. 발견 결함 (실행 후)
- 없음 / 또는 구체 케이스

---
**상태**: 미실행
