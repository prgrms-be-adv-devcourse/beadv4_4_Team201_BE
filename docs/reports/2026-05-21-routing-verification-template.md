# DataSource Routing Verification — 측정 자리

> 작성: 2026-05-21 / T7.9

## 0. 목적
RoutingDataSource + DataSourceRoutingAspect 가 실제 환경에서 의도대로 동작하는지 확인.

## 1. 사전 준비
application-staging.yml 에 routing 로그 활성:
```yaml
logging:
  level:
    org.springframework.jdbc.datasource.lookup: DEBUG
```

(이미 staging 프로파일에 포함)

## 2. 시나리오별 라우팅 기대

| API | Method | Expected Pool | Actual Pool | 결과 |
|-----|--------|--------------|-------------|------|
| GET /api/v1/products | read | giftify-replica | ? | ✅/❌ |
| GET /api/v1/products/{id} | read | giftify-replica | ? | ✅/❌ |
| GET /api/v1/wishlists/{id} | read | giftify-replica | ? | ✅/❌ |
| GET /api/v1/cart/me | read | giftify-replica | ? | ✅/❌ |
| POST /api/v1/cart/items | write | giftify-primary | ? | ✅/❌ |
| POST /api/v1/funding | write | giftify-primary | ? | ✅/❌ |
| POST /api/v1/orders/confirm | write | giftify-primary | ? | ✅/❌ |
| GET /api/v2/images/presigned-download | read (no DB) | (none) | ? | N/A |

## 3. 검증 방법
```bash
# kubectl 으로 routing 로그 추출
kubectl --context=staging logs -n giftify deploy/api-server --tail=200 \
  | grep -E "RoutingDataSource|HikariPool-giftify"

# Hikari pool 이름으로 어느 풀에서 connection 획득했는지 추적
# giftify-primary → Primary, giftify-replica → Replica
```

## 4. 발견 케이스 (실행 후 채움)
### 4.1 정상 라우팅
- ...

### 4.2 오라우팅 (의도 ≠ 실제)
- 사례:
- 원인 추정:
- 수정 PR:

## 5. 후속 (실행 후)
- [ ] @Transactional 누락 메서드 추가 보강
- [ ] LazyConnectionDataSourceProxy 적용 누락 의심 케이스 확인

---
**상태**: 미실행
