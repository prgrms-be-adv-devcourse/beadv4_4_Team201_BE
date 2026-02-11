#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OVERLAY_DIR="${SCRIPT_DIR}/../overlays/prod"
BASE_DIR="${SCRIPT_DIR}/../base"

IMAGE_TAG="${1:?Usage: $0 <IMAGE_TAG> [GHCR_PAT]}"
GHCR_PAT="${2:-}"

echo "=== Giftify k3d EC2 배포 ==="
echo ">>> 이미지 태그: $IMAGE_TAG"

# giftify 네임스페이스 확인
if ! kubectl get namespace giftify &> /dev/null; then
    echo ">>> giftify 네임스페이스 생성중..."
    kubectl apply -f "${BASE_DIR}/namespace.yaml"
fi

# GHCR Secret
if [ -n "$GHCR_PAT" ]; then
    echo ">>> GHCR Secret 생성중..."
    kubectl create secret docker-registry ghcr-secret \
        -n giftify \
        --docker-server=ghcr.io \
        --docker-username=giftify-bot \
        --docker-password="$GHCR_PAT" \
        --dry-run=client -o yaml | kubectl apply -f -
elif ! kubectl get secret ghcr-secret -n giftify &> /dev/null; then
    echo "ERROR: ghcr-secret이 없습니다. GHCR_PAT를 두 번째 인자로 전달하세요."
    echo "Usage: $0 <IMAGE_TAG> <GHCR_PAT>"
    exit 1
fi

# Secrets
if [ -f "${BASE_DIR}/secrets.yaml" ]; then
    echo ">>> Secrets 적용중..."
    kubectl apply -f "${BASE_DIR}/secrets.yaml"
else
    echo "ERROR: secrets.yaml이 없습니다. secrets.yaml.template을 복사하여 실제 값을 채워주세요."
    exit 1
fi

# 이미지 태그 설정 + 배포
echo ">>> Kustomize 배포중 (tag: $IMAGE_TAG)..."
cd "$OVERLAY_DIR"
kustomize edit set image \
    "ghcr.io/prgrms-be-adv-devcourse/beadv4_4_team201_be/api-server:$IMAGE_TAG"
kubectl apply -k .

# 이미지 태그 원복 (git diff 방지)
kustomize edit set image \
    "ghcr.io/prgrms-be-adv-devcourse/beadv4_4_team201_be/api-server:SET_BY_CICD"

echo ">>> Rollout 대기중..."
kubectl rollout status deployment/api-server -n giftify --timeout=600s

echo ""
echo "=== 배포 완료 ==="
kubectl get pods -n giftify
