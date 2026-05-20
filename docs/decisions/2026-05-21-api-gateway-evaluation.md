# API Gateway Evaluation

> 작성: 2026-05-21 / MS4 W12 T12.1-T12.3

## 0. 결정 사항
**선택: 도입 안 함 (현 단계).** Traefik(이미 사용 중) + Spring Security 의 JWT 검증으로 충분.

분리 서버 (settlement-server, 추가 서비스) 가 늘어나 *공통 인증 layer* 가 부담이 될 때 재평가.

## 1. 옵션 비교

| 항목 | Traefik ForwardAuth | Spring Cloud Gateway | Kong | 현행 (도입 안 함) |
|------|---------------------|----------------------|------|------------------|
| 인증 offload | ✅ (HTTP forward) | ✅ (Filter) | ✅ (Plugin) | 각 서버 내 처리 |
| 운영 부담 | 낮 (Traefik 기존) | 중 (별도 Spring Boot) | 중-고 (DB + Plugin 설정) | 매우 낮 |
| Rate limit | ✅ (Middleware) | ✅ (Filter) | ✅ (Plugin) | Resilience4j 모듈별 |
| CORS | ✅ | ✅ | ✅ | Spring Security CORS |
| Path rewrite | ✅ | ✅ | ✅ | Spring 내 controller |
| 학습 곡선 | 낮 (Traefik 기존) | 중 | 고 | 없음 |
| 비용 | 무료 | 무료 | OSS 무료 (Enterprise 유료) | 무료 |
| Vendor lock-in | 없음 | Spring 종속 | Kong 종속 | 없음 |

## 2. 선택 근거

### 2.1 왜 "도입 안 함" 인가
- **현재 서비스 수 = 1** (api-server 단일). Gateway 의 핵심 가치는 *여러 서비스 앞단의 공통 layer*.
- **인증 offload 의 이득 < 운영 비용** — Spring Security JWT 검증은 이미 동작 + 비용 측정 시 무시 가능.
- **Traefik 이 이미 Ingress 로 작동** — Path routing 은 Traefik IngressRoute 로 충분.
- **포트폴리오 데모 컨텍스트에서 "Gateway 도입"이 가치를 더하지 않음** — 학습 의의가 적고, 운영 복잡도만 늘어남.

### 2.2 왜 Traefik ForwardAuth 가 아닌가
- ForwardAuth 는 모든 요청을 *별도 인증 서비스* 로 한 번 더 호출 → latency 증가.
- 현재 Spring Security 검증이 충분히 빠름 (JWT signature 검증 + Redis blacklist check 합쳐 < 5ms).

### 2.3 왜 Spring Cloud Gateway 가 아닌가
- 별도 Spring Boot 앱 — 추가 1 pod + JVM 메모리.
- Reactive 기반 — 코드베이스 mvc + JPA 와 다른 패러다임 (학습 부담).
- 본 milestone 의 더 가치 있는 작업 (HA, 외부화, Saga) 보다 우선순위 낮음.

### 2.4 왜 Kong 이 아닌가
- DB (Postgres) + Plugin 설정 — 운영 복잡도 가장 큼.
- Enterprise 기능이 많지만 본 프로젝트에서는 사용처 부재.

## 3. 재평가 트리거 (when to revisit)
- 서비스 수 ≥ 3 (api-server, settlement-server, 추가 서비스 도입 시)
- 인증 로직이 *서비스별 분리* 가 어렵게 복잡해질 때
- 외부 client (mobile / 파트너) 가 늘어 *공통 API 정책* 필요할 때
- Rate limit / Throttling 의 공통 적용이 절실해질 때

## 4. 현재 Traefik 으로 커버 중인 항목
| 기능 | 위치 |
|------|------|
| TLS termination | Traefik Ingress + Cloudflare |
| Path routing | IngressRoute (api-server, prometheus, grafana, argocd) |
| Rate limit (기본) | Traefik middleware (per-IP) |
| CORS | Spring Security `CorsConfigurationSource` |
| JWT validation | Spring Security OAuth2 Resource Server |

## 5. 후속 (out of scope)
- 위 트리거 발생 시 *재평가 doc* 작성 → Traefik ForwardAuth + 경량 인증 microservice 첫 평가.
- API versioning 정책이 복잡해지면 Gateway 도입 가속.

---
**상태**: 결정 확정 — 도입 안 함. 향후 재평가 트리거 명시.
