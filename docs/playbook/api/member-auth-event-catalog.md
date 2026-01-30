# Member & Auth Domain - Event Catalog

**최종 수정일:** 2026-01-30
**버전:** 1.1
**담당 모듈:** `bc/member`, `bc/shared`, `support/common`

---

## 개요

Member 및 Auth 도메인에서 발행하거나 소비하는 도메인 이벤트를 정의합니다.
이벤트 기반 아키텍처를 통해 도메인 간 느슨한 결합을 유지합니다.

---

## 이벤트 분류

| 분류                     | Package                         | 설명                         |
|------------------------|---------------------------------|----------------------------|
| **Domain Events**      | `bc/shared/.../event/member`    | 도메인 비즈니스 이벤트               |
| **Application Events** | `support/common/.../event/auth` | Spring ApplicationEvent 기반 |

---

## Published Events (발행 이벤트)

### 1. MemberSignedEvent

회원가입이 완료되었을 때 발행됩니다.

| 항목              | 값                                                          |
|-----------------|------------------------------------------------------------|
| **Event Class** | `app.giftify.shared.domain.event.member.MemberSignedEvent` |
| **Publisher**   | `MemberService.registerMember()`                           |
| **Trigger**     | 회원가입 완료 시                                                  |
| **Base Class**  | `BaseDomainEvent`                                          |

#### Payload

| Field      | Type     | Description  |
|------------|----------|--------------|
| `memberId` | `Long`   | 생성된 회원 ID    |
| `authSub`  | `String` | Auth0 고유 식별자 |
| `nickname` | `String` | 회원 닉네임       |

#### Example

```java
new MemberSignedEvent(
    1L,                        // memberId
    "auth0|abc123",            // authSub
    "행복한고양이1234"           // nickname
)
```

#### Consumers

| Consumer | Module | Action              |
|----------|--------|---------------------|
| (향후 확장)  | -      | 환영 이메일 발송, 포인트 적립 등 |

---

### 2. MemberUpdatedEvent

회원 정보가 수정되었을 때 발행됩니다.

| 항목              | 값                                                           |
|-----------------|-------------------------------------------------------------|
| **Event Class** | `app.giftify.shared.domain.event.member.MemberUpdatedEvent` |
| **Publisher**   | `MemberService.updateMember()`                              |
| **Trigger**     | 회원정보 수정 시                                                   |
| **Base Class**  | `BaseDomainEvent`                                           |

#### Payload

| Field      | Type     | Description  |
|------------|----------|--------------|
| `memberId` | `Long`   | 회원 ID        |
| `authSub`  | `String` | Auth0 고유 식별자 |
| `nickname` | `String` | 변경된 닉네임      |

#### Example

```java
new MemberUpdatedEvent(
    1L,                        // memberId
    "auth0|abc123",            // authSub
    "새로운닉네임"               // nickname
)
```

#### Consumers

| Consumer | Module | Action                       |
|----------|--------|------------------------------|
| (향후 확장)  | -      | 다른 BC의 Member Replica 업데이트 등 |

---

### 3. UserAuthenticatedEvent

사용자 인증이 성공했을 때 (특히 신규 사용자) 발행됩니다.

| 항목              | 값                                                              |
|-----------------|----------------------------------------------------------------|
| **Event Class** | `app.giftify.support.common.event.auth.UserAuthenticatedEvent` |
| **Publisher**   | `LoginService.login()`                                         |
| **Trigger**     | 신규 사용자가 로그인 시                                                  |
| **Base Class**  | `ApplicationEvent` (Spring)                                    |

#### Payload

| Field      | Type     | Description   |
|------------|----------|---------------|
| `authSub`  | `String` | Auth0 고유 식별자  |
| `nickname` | `String` | 닉네임 (JWT 클레임) |
| `email`    | `String` | 이메일 (JWT 클레임) |
| `name`     | `String` | 이름 (JWT 클레임)  |

#### Example

```java
new UserAuthenticatedEvent(
    this,                      // source
    "auth0|xyz789",            // authSub
    "user123",                 // nickname
    "newuser@example.com",     // email
    "홍길동"                    // name
)
```

#### Consumers

| Consumer                         | Module             | Action             |
|----------------------------------|--------------------|--------------------|
| `UserAuthenticatedEventListener` | `bc/member`        | PreSignup 임시 정보 생성 |
| `AuthEventListener`              | `bc/member` (auth) | 로깅 (디버그용)          |

---

## Consumed Events (소비 이벤트)

### 1. UserAuthenticatedEvent 처리

#### UserAuthenticatedEventListener

| 항목                | 값                                                                    |
|-------------------|----------------------------------------------------------------------|
| **Handler Class** | `app.giftify.member.adapter.in.event.UserAuthenticatedEventListener` |
| **Event**         | `UserAuthenticatedEvent`                                             |
| **Annotation**    | `@EventListener`                                                     |

#### 처리 로직

```java
@EventListener
public void handleUserAuthenticatedEvent(UserAuthenticatedEvent event) {
    // 1. 이미 가입된 회원인지 확인
    if (registerMemberUseCase.existsByEmail(event.getEmail())) {
        log.info("[Event] 이미 가입된 회원입니다. 임시 정보를 생성하지 않습니다.");
        return;
    }

    // 2. PreSignup 임시 정보 생성
    preSignupPort.save(new PreSignup(
        event.getAuthSub(),
        event.getEmail(),
        event.getName(),
        event.getNickname()
    ));
}
```

#### Flow Diagram

```
UserAuthenticatedEvent
         │
         ▼
┌─────────────────────────┐
│ UserAuthenticated       │
│ EventListener           │
└───────────┬─────────────┘
            │
            ▼
      ┌───────────┐
      │ 기존 회원? │
      └─────┬─────┘
            │
    ┌───────┴───────┐
    │               │
   Yes             No
    │               │
    ▼               ▼
 (무시)      ┌─────────────┐
             │ PreSignup   │
             │ 생성        │
             └─────────────┘
```

---

## Event Publishing Pattern

### EventPublisher Interface

```java
// bc/shared/.../EventPublisher.java
public interface EventPublisher {
    void publish(BaseDomainEvent event);
}
```

### Usage in Service

```java
@Service
@RequiredArgsConstructor
public class MemberService {
    private final EventPublisher eventPublisher;

    public Member registerMember(RegisterCommand command) {
        // ... 회원 생성 로직

        Member savedMember = memberRepositoryPort.save(member);

        // 이벤트 발행
        eventPublisher.publish(
            new MemberSignedEvent(
                savedMember.getId(),
                savedMember.getAuthSub(),
                savedMember.getNickname()
            )
        );

        return savedMember;
    }
}
```

---

## Event Flow Diagrams

### 신규 사용자 가입 플로우

```
┌────────────┐    ┌────────────┐    ┌────────────┐    ┌────────────┐
│  Frontend  │    │LoginService│    │ Event Bus  │    │PreSignup   │
│            │    │            │    │            │    │Listener    │
└─────┬──────┘    └─────┬──────┘    └─────┬──────┘    └─────┬──────┘
      │                 │                 │                 │
      │ POST /login     │                 │                 │
      │ {idToken}       │                 │                 │
      │────────────────▶│                 │                 │
      │                 │                 │                 │
      │                 │ UserAuthenticated                 │
      │                 │ Event 발행      │                 │
      │                 │────────────────▶│                 │
      │                 │                 │                 │
      │                 │                 │ Event 전달      │
      │                 │                 │────────────────▶│
      │                 │                 │                 │
      │                 │                 │                 │ PreSignup
      │                 │                 │                 │ 저장
      │                 │                 │                 │────┐
      │                 │                 │                 │    │
      │                 │                 │                 │◀───┘
      │                 │                 │                 │
      │ {isNewUser:true}│                 │                 │
      │◀────────────────│                 │                 │
      │                 │                 │                 │
      │ POST /signup    │                 │                 │
      │ {...}           │                 │                 │
      │────────────────────────────────────────────────────▶│
      │                 │                 │                 │
      │                 │ MemberSigned    │                 │
      │                 │ Event 발행      │                 │
      │                 │────────────────▶│                 │
      │                 │                 │                 │
      │ Member Response │                 │                 │
      │◀────────────────────────────────────────────────────│
      │                 │                 │                 │
      ▼                 ▼                 ▼                 ▼
```

### 회원정보 수정 플로우

```
┌────────────┐    ┌────────────┐    ┌────────────┐    ┌────────────┐
│  Frontend  │    │MemberV2    │    │Member      │    │ Event Bus  │
│            │    │Controller  │    │Service     │    │            │
└─────┬──────┘    └─────┬──────┘    └─────┬──────┘    └─────┬──────┘
      │                 │                 │                 │
      │ PATCH /me       │                 │                 │
      │ {nickname,...}  │                 │                 │
      │────────────────▶│                 │                 │
      │                 │                 │                 │
      │                 │ updateMember()  │                 │
      │                 │────────────────▶│                 │
      │                 │                 │                 │
      │                 │                 │ Member 업데이트 │
      │                 │                 │────┐            │
      │                 │                 │    │            │
      │                 │                 │◀───┘            │
      │                 │                 │                 │
      │                 │                 │ MemberUpdated   │
      │                 │                 │ Event 발행      │
      │                 │                 │────────────────▶│
      │                 │                 │                 │
      │                 │ Updated Member  │                 │
      │                 │◀────────────────│                 │
      │                 │                 │                 │
      │ MemberResponse  │                 │                 │
      │◀────────────────│                 │                 │
      │                 │                 │                 │
      ▼                 ▼                 ▼                 ▼
```

---

## Event Contracts

### BaseDomainEvent

모든 도메인 이벤트의 기본 클래스입니다.

```java
package app.giftify.shared.domain.event;

public abstract class BaseDomainEvent {
    // 공통 필드 및 메서드
}
```

### ApplicationEvent (Spring)

Spring Framework의 이벤트 시스템을 사용하는 이벤트입니다.

```java
package org.springframework.context;

public abstract class ApplicationEvent extends EventObject {
    private final long timestamp;
    // ...
}
```

---

## Best Practices

### 이벤트 발행 시

1. **트랜잭션 경계 고려**: 이벤트는 트랜잭션 커밋 후 발행되어야 함
2. **멱등성 보장**: 동일 이벤트가 여러 번 처리되어도 결과가 동일해야 함
3. **최소 페이로드**: 필요한 정보만 이벤트에 포함

### 이벤트 소비 시

1. **비동기 처리 고려**: 장시간 작업은 `@Async`와 함께 사용
2. **에러 핸들링**: 이벤트 처리 실패 시 재시도 정책 적용
3. **순서 독립성**: 이벤트 순서에 의존하지 않는 설계

---

## Version History

| Version | Date       | Changes                                       |
|---------|------------|-----------------------------------------------|
| 1.1     | 2026-01-30 | UserAuthenticatedEvent 신규 사용자 로그인 시 발행 로직 추가  |
| 1.0     | 2025-12-01 | 초기 버전 (MemberSignedEvent, MemberUpdatedEvent) |
