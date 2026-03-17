#!/usr/bin/env bash
#
# ★ Insight ─────────────────────────────────────
# k6는 PostgreSQL 클라이언트를 내장하지 않는다.
# xk6-sql 같은 확장이 있지만 별도 빌드가 필요하고,
# teardown()에서 DB 검증을 하면 테스트 자체의 관심사와 섞인다.
#
# 따라서 DB 정합성 검증은 별도 쉘 스크립트로 분리.
# 테스트 종료 후 수동으로 실행하는 구조.
#
# 접근 방법 2가지:
# 1. psql 직접 연결 (DB가 외부 노출된 경우)
# 2. kubectl exec으로 k3s pod 내부에서 실행 (prod 환경)
# ─────────────────────────────────────────────────
#
# 사용법:
#   ./verify.sh <FUNDING_ID> [PRODUCT_ID]
#
# 예시:
#   ./verify.sh 42           # 펀딩 정합성만 검증
#   ./verify.sh 42 7         # 펀딩 + 재고 정합성 검증
#

set -euo pipefail

# ── 인자 파싱 + 입력값 검증 ──────────────────────
# SQL injection 방어: 숫자만 허용
FUNDING_ID="${1:?사용법: ./verify.sh <FUNDING_ID> [PRODUCT_ID]}"
PRODUCT_ID="${2:-}"

if ! [[ "${FUNDING_ID}" =~ ^[0-9]+$ ]]; then
    echo "ERROR: FUNDING_ID는 숫자만 허용됩니다: '${FUNDING_ID}'"
    exit 1
fi
if [ -n "${PRODUCT_ID}" ] && ! [[ "${PRODUCT_ID}" =~ ^[0-9]+$ ]]; then
    echo "ERROR: PRODUCT_ID는 숫자만 허용됩니다: '${PRODUCT_ID}'"
    exit 1
fi

# ── DB 접속 설정 ───────────────────────────────
# 환경변수로 주입하거나 기본값 사용
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-giftify_db}"
DB_USER="${DB_USER:-giftify}"

# Phase 1 (loadtest 스키마)에서는 DB_SCHEMA=loadtest로 설정.
# 기본값은 운영 스키마(g7app).
DB_SCHEMA="${DB_SCHEMA:-g7app}"

# ★ 설계 의도 ─────────────────────────────────────
# PGPASSWORD 환경변수로 비밀번호를 전달하면
# psql이 인터랙티브 프롬프트 없이 실행된다.
# .pgpass 파일 대비 일회성 스크립트에 적합.
#
# -c "SET search_path ..."를 매 쿼리마다 붙이는 대신
# PGOPTIONS로 세션 전체에 search_path를 적용한다.
# ─────────────────────────────────────────────────
export PGOPTIONS="-c search_path=${DB_SCHEMA},public"
PSQL="psql -h ${DB_HOST} -p ${DB_PORT} -d ${DB_NAME} -U ${DB_USER} -t -A"

echo "═══════════════════════════════════════════"
echo " DB 정합성 검증"
echo " 스키마: ${DB_SCHEMA}"
echo " 펀딩 ID: ${FUNDING_ID}"
[ -n "${PRODUCT_ID}" ] && echo " 상품 ID: ${PRODUCT_ID}"
echo "═══════════════════════════════════════════"
echo ""

PASS=0
FAIL=0

# ── 검증 1: 펀딩 초과 여부 ────────────────────
# current_amount가 target_amount를 초과하면 동시성 버그
echo "▶ 검증 1: 펀딩 금액 초과 여부"
RESULT=$(${PSQL} -v funding_id="${FUNDING_ID}" -c "
    SELECT id, current_amount, target_amount
    FROM fundings
    WHERE id = :funding_id
      AND current_amount > target_amount;
")

if [ -z "${RESULT}" ]; then
    echo "  ✓ PASS — current_amount <= target_amount"
    PASS=$((PASS + 1))
else
    echo "  ✗ FAIL — 펀딩 금액 초과 감지!"
    echo "  ${RESULT}"
    FAIL=$((FAIL + 1))
fi
echo ""

# ── 검증 2: 중복 참여 여부 ────────────────────
# 동일 사용자가 같은 펀딩에 2번 이상 참여하면 동시성 버그
# 테이블명: funding_participant_members (funding_participant 아님)
echo "▶ 검증 2: 중복 참여 여부"
RESULT=$(${PSQL} -v funding_id="${FUNDING_ID}" -c "
    SELECT participant_id, COUNT(*)
    FROM funding_participant_members
    WHERE funding_id = :funding_id
    GROUP BY participant_id
    HAVING COUNT(*) > 1;
")

if [ -z "${RESULT}" ]; then
    echo "  ✓ PASS — 중복 참여 없음"
    PASS=$((PASS + 1))
else
    echo "  ✗ FAIL — 중복 참여 감지!"
    echo "  ${RESULT}"
    FAIL=$((FAIL + 1))
fi
echo ""

# ── 검증 3: 재고 음수 여부 (PRODUCT_ID 지정 시) ──
# 재고 차감은 펀딩 수락 → FundingAcceptedEvent → ProductEventListener
# 동시 수락 시 stock이 음수가 되면 동시성 버그
if [ -n "${PRODUCT_ID}" ]; then
    echo "▶ 검증 3: 재고 음수 여부"
    RESULT=$(${PSQL} -v product_id="${PRODUCT_ID}" -c "
        SELECT id, stock
        FROM products
        WHERE id = :product_id
          AND stock < 0;
    ")

    if [ -z "${RESULT}" ]; then
        echo "  ✓ PASS — stock >= 0"
        PASS=$((PASS + 1))
    else
        echo "  ✗ FAIL — 재고 음수 감지!"
        echo "  ${RESULT}"
        FAIL=$((FAIL + 1))
    fi
    echo ""
fi

# ── 결과 요약 ──────────────────────────────────
echo "═══════════════════════════════════════════"
echo " 결과: ${PASS} PASS / ${FAIL} FAIL"
if [ ${FAIL} -gt 0 ]; then
    echo " ⚠ 동시성 버그가 감지되었습니다!"
    exit 1
else
    echo " 모든 검증 통과"
    exit 0
fi
