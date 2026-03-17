#!/usr/bin/env bash
set -euo pipefail

SCRIPT="${1:?사용법: ./run-test.sh <script-name> [추가 k6 옵션...]}"
shift

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)/scripts"
RESULT_DIR="$(cd "$(dirname "$0")" && pwd)/results"
DATE=$(date +%Y-%m-%d-%H%M)

if [[ "$SCRIPT" == *.js ]]; then
    SCRIPT_NAME="${SCRIPT%.js}"
else
    SCRIPT_NAME="$SCRIPT"
    SCRIPT="${SCRIPT}.js"
fi

if [ ! -f "$SCRIPT_DIR/$SCRIPT" ]; then
    echo "ERROR: $SCRIPT_DIR/$SCRIPT 파일을 찾을 수 없습니다."
    echo ""
    echo "사용 가능한 스크립트:"
    ls "$SCRIPT_DIR"/*.js | xargs -n1 basename
    exit 1
fi

mkdir -p "$RESULT_DIR"

RESULT_JSON="$RESULT_DIR/${DATE}-${SCRIPT_NAME}.json"
RESULT_TXT="$RESULT_DIR/${DATE}-${SCRIPT_NAME}.txt"

echo "═══════════════════════════════════════════"
echo " 스크립트: $SCRIPT"
echo " 결과 JSON: $RESULT_JSON"
echo " 결과 TXT:  $RESULT_TXT"
echo " 추가 옵션: $*"
echo "═══════════════════════════════════════════"
echo ""

k6 run \
    --out "json=$RESULT_JSON" \
    "$@" \
    "$SCRIPT_DIR/$SCRIPT" 2>&1 | tee "$RESULT_TXT"

echo ""
echo "═══════════════════════════════════════════"
echo " 완료: $(date)"
echo " JSON: $RESULT_JSON"
echo " TXT:  $RESULT_TXT"
echo "═══════════════════════════════════════════"
