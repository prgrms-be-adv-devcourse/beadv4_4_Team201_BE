# Giftify 통합 기획서 

```yaml
status: "작성중"
version: "v0.1"
last_modified: 2026.01.08
asignee:
```

---

## 0. Change Log

| **버전** | **날짜** | **수정 내용** | **작성자** |
| --- | --- | --- |---------|
| v0.1 | 2026-01-08 | 최초 기획서 초안 작성  | chan99k |
|  |  |  |         |

---

## 1. Introduction

### 1.1 Purpose

- **배경:** 선물을 주고받는 과정에서 가격 부담으로 인해 진정으로 원하는 선물이 아닌 다른 상품을 선택하거나, 상대방이 원하는 선물을 알고 있음에도 경제적인 이유로 타협해야 했던 불편한 경험이 존재한다.
- **목표:** *친구 간의 부담 없는 선물 문화를 위한 위시리스트 기반 펀딩 시스템 구축*
- **핵심 가치:**
    - **부담 분산 (Shared Responsibility)**
        - 여러 사용자가 하나의 선물에 공동으로 참여함으로써 개인의 경제적 부담을 최소화한다.
    - **의사 존중 (Recipient-Centered Gifting)**
        - 수령자의 위시리스트를 기반으로 선물이 결정되어 실제 선호가 정확히 반영된다.
    - **참여 유연성 (Flexible Participation)**
        - 참여자는 금액에 제한 없이 자유롭게 펀딩에 참여할 수 있다.
    - **투명성 (Contribution Transparency)**
        - 펀딩 진행 상황과 참여 내역이 명확하게 공유되어 신뢰할 수 있는 선물 경험을 제공한다.
    - **관계 강화 (Positive Social Interaction)**
        - 선물 과정에서 발생하는 심리적 부담을 줄이고 긍정적인 관계 형성을 촉진한다.

### 1.2 Scope

**1.2.1 In-Scope**

- 사용자의 회원가입 및 개인 정보 관리
- 사용자의 예치금 생성 및 관리
- 사용자의 장바구니 정보 생성 및 관리
- 사용자의 위시리스트 생성 및 관리
- 1명 이상 친구의 펀딩 참여 및 분할 결제 처리
- 펀딩 성공/실패에 따른 정산 프로세스
- 판매자 또한 구매 관련 기능 모두 이용 가능

**1.2.2 Out-of-Scope**

- 친구간 실시간 채팅 기능
- 다국어 지원
- 오프라인 매장 연동
- 타임딜
- 공동구매

---

## 2. Ubiquitous Language

> *기획서, 코드, DB, 말하기에서 모두 동일하게 사용될 용어입니다.*
>

| **한글 용어** | **영어 용어 (Code)** | **설명** | **제약 조건** |
| --- | --- | --- | --- |
| 펀딩 | `Funding` | 위시리스트 상품에 대해 친구들이 금액을 기여하는 상태 | 최초 결제 성공 시에만 생성되며, 종료된 펀딩에는 참여 불가 |
| 위시리스트 | `Wishlist` | 사용자가 갖고 싶은 상품을 담아두는 목록 | 사용자당 한 개 생성 가능 |
| 위시리스트 상품 | `Wishlist_Item` | 위시리스트에 포함된 개별 상품 | 펀딩은 위시리스트 상품 단위로 생성 |
| 펀딩 참여자 | `Funding_Participant` | 펀딩에 금액을 결제하여 참여한 사용자 | 동일 사용자의 중복 참여 허용 |
| 기여금 | `Funding_Amount` | 참여자가 펀딩에 지불한 금액 (결제 내역과 매핑) | 1,000₩ 보다 큰 금액만 허용 |
| 트리거 | `Trigger` | 상태 머신에서 정의된 전이를 실제로 발생시키는 이벤트 | 사용자 액션 또는 시스템 이벤트로만 발생 |
| 사용자 | `User` | 플랫폼을 이용하는 일반 사용자 | 회원 가입 및 인증 필수 |
| 구매자 | `Buyer` | 상품을 구매하는 사용자 | 결제 정보 및 배송지 입력 필수 |
| 판매자 | `Seller` | 상품을 등록하고 판매하는 사용자 | 판매자 역할을 가진 사용자만 가능 |
| 관리자 | `Admin` | 시스템 운영 및 관리 역할을 수행하는 사용자 | 관리자 권한 필요 |
| 상품 | `Product` | 플랫폼에서 판매되는 상품 | INACTIVE 상태에서는 구매/펀딩 불가 |
| 장바구니 | `Cart` | 사용자가 구매를 위해 상품을 담아두는 공간 | 사용자당 1개만 존재 |
| 장바구니 항목 | `Cart_Item` | 장바구니에 담긴 개별 상품 정보 | 수량은 1 이상 |
| 결제 | `Payment` | 사용자가 금전을 지불하는 행위 | 외부 PG 승인 필수 |
| 결제 상태 | `Payment_Status` | 결제의 진행 상태 (PAID / CANCELED / REFUNDED) | 정의된 상태 외 값 불가 |
| 지갑 | `Wallet` | 사용자의 서비스 내부 자산(예치금)을 관리하는 저장소 | 사용자당 1개만 생성 |
| 지갑 내역 | `Wallet_History` | 지갑에서 발생한 금액 변동 이력 | 모든 금액 변경은 이력으로 기록 |
| 잔액 | `Balance` | 지갑 또는 계정에 남아있는 금액 | 음수 불가 |
| 정산 | `Settlement` | 판매자에게 지급될 금액을 확정하는 과정 | 결제 완료된 금액만 정산 가능 |
| 출금 | `Withdrawal` | 사용자가 잔액을 외부로 인출 요청하는 행위 | 비동기 처리, 실패 시 재시도 가능 |
| 송금 | `Payout` | 시스템이 외부 계좌로 실제 금액을 송금하는 행위 | 정산 확정 이후에만 가능 |
| 환불 | `Refund` | 결제 취소 또는 반품으로 인해 금액을 되돌려주는 행위 | 환불 정책에 따라 처리 |
| 상태 머신 | `State_Machine` | 이 객체가 가질 수 있는 상태들과, 상태 간 이동 규칙을 정의한 모델 | 정의된 상태 전이만 허용 |
| 알림 | `Notification` | 특정 이벤트 발생 시 사용자에게 전달되는 메시지 | 이메일/카카오 등 내부 정책상 채널 제한 |
| 활동 로그 | `Activity_Log` | 시스템 내 주요 사용자/시스템 행위 기록 | 감사 및 추적 목적, 수정 불가 |
| 정책 | `Policy` | 시스템에서 정의하여 결정내릴 수 있는 조건 | 비즈니스 코드를 구현하는데 사용하는 지침 |
| 펀딩 수령자 | `Funding_Receiver` | 펀딩이 완료되어 상품을 선물받는 당사자 |  |

---

## 3. Overall Description

### 3.1 User Personas

1. **User :** 시스템 관리자를 제외한 서비스를 이용하는 일반 사용자
2. **Funding Receiver :** 위시리스트를 등록하고 선물을 받는 사람.
3. **Funding Participants :** 친구의 위시리스트를 보고 펀딩에 참여하여 일부 금액을 결제하는 사람
4. **Buyer :** 상품을 구매하는 사용자
5. **Seller :** 상품을 등록하고 판매하는 사용자
6. **Admin :** 시스템 권한을 보유한 사용자. 결제 취소 및 펀딩 강제 종료 권한 보유

### 3.2 System Architecture

- **Frontend:** `React` / `Flutter`
- **Backend:** `Spring Boot`
- **Infra:** `AWS EC2`
- **External:** `Toss Payments API`

---

## 4. System Features

> 각 기능의 상세 User Story, Acceptance Criteria, Sequence Diagram은 별도 파일로 관리됩니다.
>

### 4.1 펀딩 라이프사이클 (Funding Lifecycle)

> **설명:** 누군가의 위시리스트 상품에 대해 누군가 처음으로 결제하면 펀딩 프로젝트가 자동으로 시작된다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.1-funding-lifecycle.md)
- [Sequence Diagram](../sequences/4.1-funding-lifecycle.md)

### 4.2 펀딩 참여 및 결제 (Participate & Payment)

> **설명:** 여러 사용자가 동시에 펀딩에 참여할 수 있으며, 목표 금액 초과 결제를 방지한다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.2-funding-participate-payment.md)
- [Sequence Diagram](../sequences/4.2-funding-participate-payment.md)

### 4.3 펀딩 완료 처리 (Funding Completion)

> **설명:** 펀딩 금액이 목표 금액에 도달하면 펀딩은 자동으로 완료 상태로 전환된다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.3-funding-completion.md)
- [Sequence Diagram](../sequences/4.3-funding-completion.md)

### 4.4 펀딩 종료일 초과 처리 (Funding Expiration)

> **설명:** 펀딩 종료일까지 목표 금액을 달성하지 못하면 펀딩은 실패 처리된다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.4-funding-expiration.md)
- [Sequence Diagram](../sequences/4.4-funding-expiration.md)

### 4.5 결제 취소 및 환불 (Refund)

> **설명:** 펀딩이 실패한 경우, 참여자의 결제 금액은 자동으로 환불된다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.5-refund.md)
- [Sequence Diagram](../sequences/4.5-refund.md)

### 4.6 위시리스트 상품 상태 관리 (Wishlist State)

> **설명:** 위시리스트 상품은 펀딩 진행 상황에 따라 상태가 변경된다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.6-wishlist-state-management.md)
- [Sequence Diagram](../sequences/4.6-wishlist-state-management.md)

### 4.7 펀딩 참여 알림 (Notification)

> **설명:** 펀딩에 중요한 이벤트가 발생하면 사용자에게 알림이 전송된다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.7-funding-notification.md)
- [Sequence Diagram](../sequences/4.7-funding-notification.md)

### 4.8 예치금 충전 완료

> **설명:** 사용자는 예치금을 충전할 수 있다

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.8-wallet-deposit.md)
- [Sequence Diagram](../sequences/4.8-wallet-deposit.md)

### 4.9 예치금 출금 완료

> **설명:** 사용자는 예치금을 출금할 수 있다

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.9-wallet-withdrawal.md)
- [Sequence Diagram](../sequences/4.9-wallet-withdrawal.md)

### 4.10 판매금 정산

> **설명:** 판매자는 결제 내역을 바탕으로 본인이 판매한 상품의 판매 대금을 정산 받을 수 있다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.10-settlement.md)
- [Sequence Diagram](../sequences/4.10-settlement.md)

### 4.11 장바구니 관리

> **설명:** 사용자는 여러 펀딩 상품을 자신의 장바구니에 담아둘 수 있다

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.11-cart-management.md)
- [Sequence Diagram](../sequences/4.11-cart-management.md)

### 4.12 장바구니 내 금액 수정

> **설명:** 장바구니에 담은 펀딩 상품에 대해 분담하고자 하는 금액을 수정할 수 있다.

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.12-cart-amount-edit.md)
- [Sequence Diagram](../sequences/4.12-cart-amount-edit.md)

### 4.13 판매자 상품 등록

> **설명:** 판매자는 상품을 등록 할 수 있다 .

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.13-product-registration.md)
- [Sequence Diagram](../sequences/4.13-product-registration.md)

### 4.14 판매자의 판매 상품 수정/철회

> **설명:** 판매자는 상품을 수정/철회 할 수 있다 .

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.14-product-edit-withdraw.md)
- [Sequence Diagram](../sequences/4.14-product-edit-withdraw.md)

### 4.15 판매자의 최근 판매 상품 목록 조회

> **설명:** 판매자는 본인이 최근에 판매한 상품 목록을 확인할 수 있다

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.15-recent-sales-list.md)
- [Sequence Diagram](../sequences/4.15-recent-sales-list.md)

### 4.16 판매자의 상품 재고 관리

> **설명:** 판매자는 본인이 등록한 상품의 재고 수량을 조회하고 수정할 수 있다

**상세 문서:**
- [User Story & Acceptance Criteria](../user-stories/4.16-inventory-management.md)
- [Sequence Diagram](../sequences/4.16-inventory-management.md)

---

## 5. Non-Functional Requirements

### 5.1 Performance

- 메인 리스트 조회 API 응답 시간: **P95 기준** **200ms 이하**
- 최대 동시 접속자 수(CCU): **1,000명** 목표
- QPS : **약 300 ~ 500**
- TPS : **약 50~100**

### 5.2 Reliability

- **멱등성(Idempotency):** **동일한 결제 요청이 중복 전송되어도 서버는 1번만 처리**해야 함.
- **데이터 정합성:** `Funding`의 `currentAmount`와 `Funding_Participants` 테이블의 합계는 항상 일치해야 함.

### 5.3 Security

- API 통신 시 `HTTPS` 필수 적용.
- `JWT` 기반 인증 사용 (Access Token 만료 시 Refresh Token 사용)

---

## 6. Data Requirements

### 6.1 Conceptual ERD

*[ERD 이미지는 별도 파일로 관리 예정]*

N/A - ERD 다이어그램이 별도로 추가될 예정입니다.

---

## 7. Project Plan

### 7.1 R&R

| **이름** | **역할** | **담당 업무** | 주요 담당 도메인 |
| --- | --- | --- | --- |
| **김찬규** | **PO** | 프로젝트 로드맵 관리 |  |
| **주영경** | **서기** | 회의록 작성 |  |
| **김성은** | **DevOps** | 클라우드 리소스 관리  |  |
| **김영주** | N/A | N/A |  |
| **고수연** | N/A | N/A |  |

### 7.2 WBS

- **1주차:** 기획 확정, 기술 스택 선정, ERD 설계
- **2주차:** 핵심 기능 구현 (member, product, auth, funding, payment, cart, wallet… ), 테스트 배포
- **3주차:** 통합 테스트 및 최종 배포

---

## 8. Appendix

- API 명세서 링크: N/A
- 요구사항 정의서 *(= 기능 정의서)* 링크: N/A
- 유저 스토리 링크: N/A

---

**문서 작성 완료일:** 2026-01-12
**문서 위치:** `docs/requirements/260112-PRD-giftify-integrated-requirements.md`
**Git 관리:** 이 문서는 Git으로 버전 관리됩니다.
