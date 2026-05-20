# Event Serialization Decision

> 작성: 2026-05-21 / MS4 W11 T11.5

## 0. 결정 사항
**선택: JSON (Jackson)**.
Schema Registry 미도입. 호환성은 BACKWARD_TRANSITIVE 규칙 + 코드 리뷰 + 통합 테스트로 강제.

## 1. 옵션 비교

| 항목 | JSON (Jackson) | Avro + Schema Registry | Protobuf |
|------|----------------|------------------------|----------|
| 인간 가독성 | 높 (디버깅 편리) | 낮 (binary) | 낮 (binary) |
| 메시지 크기 | 크 | 작 (40-60%) | 작 (35-55%) |
| Schema evolution | 명시 안 됨 (수동 규칙) | 강제 (registry) | 명시 (.proto 파일) |
| 운영 의존성 | 없음 | Schema Registry 추가 (Confluent/Apicurio) | proto 컴파일러 |
| Spring Modulith 통합 | 기본 | 별도 customizer | 별도 customizer |
| 학습 곡선 | 낮 | 중-고 | 중 |
| 디버깅 (Redpanda Console) | 즉시 가능 | Schema Registry 의존 | proto decode 필요 |
| 비용 | 무료 | Registry 운영 비용 | 무료 |

## 2. 선택 근거

### 2.1 왜 JSON 인가
- **Spring Modulith Externalization 기본 직렬화** — 추가 설정 없이 동작.
- **Redpanda Console 에서 즉시 가독** — 운영 중 이벤트 흐름 디버깅 편리.
- **현재 트래픽 규모 < 1k events/sec** — 메시지 크기 차이가 비용에 영향 미미.
- **포트폴리오 데모 컨텍스트** — Schema Registry 운영 자체보다 이벤트 외부화 패턴 학습이 가치.

### 2.2 왜 Avro 가 아닌가
- Schema Registry (Confluent/Apicurio) 추가 운영 부담.
- Avro IDL 학습 곡선 + 코드 생성 step 추가.
- 본 사이클에서는 evolution 강제보다 *시작* 이 더 중요.

### 2.3 왜 Protobuf 가 아닌가
- gRPC 없이 Kafka 만 사용 시 proto 의 이점이 일부만 살아남.
- 마찬가지 학습 곡선.

## 3. 호환성 보장 규칙 (Avro 부재 시 수동)

### 3.1 BACKWARD_TRANSITIVE 동등 정책
| 변경 | 허용 여부 | 비고 |
|------|----------|------|
| 신규 optional 필드 추가 | ✅ | Jackson default 처리 |
| 신규 required 필드 추가 | ❌ | 기존 consumer 깨짐 |
| 필드 제거 | ❌ (1 cycle deprecation 후) | |
| 필드 이름 변경 | ❌ (alias 사용) | `@JsonAlias` |
| 타입 변경 | ❌ | 신규 필드 추가 + 구필드 deprecation |
| enum value 추가 | ✅ | `@JsonEnumDefaultValue` 설정 시 |
| enum value 제거 | ❌ | |

### 3.2 강제 메커니즘
- **ArchUnit 규칙**: 모든 이벤트 클래스는 `record` 또는 `@JsonInclude` 적용 (필드 누락 방지).
- **통합 테스트**: 이전 버전 직렬화 → 신규 버전 역직렬화 round-trip 테스트.
- **CI 체크**: PR 시 이벤트 클래스 diff 자동 검출 (Plan 외 작업).

## 4. Sample 직렬화 결과
```json
{
  "eventId": "01HXXXXXXX",
  "eventType": "PaymentSucceeded",
  "occurredAt": "2026-05-21T12:34:56Z",
  "correlationId": "ord-abc-123",
  "payload": {
    "paymentId": 42,
    "orderId": 101,
    "amount": 50000,
    "method": "TOSS_PAYMENTS"
  }
}
```

- 봉투 (envelope) 필드 (`eventId`, `eventType`, `occurredAt`, `correlationId`) 는 공통.
- `payload` 는 이벤트별 도메인 데이터.

## 5. 후속 (out of scope)
- 트래픽이 5k events/sec 이상이 되거나, 메시지 크기 비용이 두드러질 때 Avro 재평가.
- Cross-team 이벤트 발행 (외부 시스템 consumer) 가 생기면 Schema Registry 필수.

---
**상태**: 결정 확정. 구현은 Spring Modulith default Jackson 사용.
