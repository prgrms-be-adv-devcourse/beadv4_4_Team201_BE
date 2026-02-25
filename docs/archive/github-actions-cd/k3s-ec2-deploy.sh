#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OVERLAY_DIR="${SCRIPT_DIR}/../overlays/prod"
BASE_DIR="${SCRIPT_DIR}/../base"

IMAGE_TAG="${1:?Usage: $0 <IMAGE_TAG>}"

echo "=== Giftify k3s EC2 배포 ==="
echo ">>> 이미지 태그: $IMAGE_TAG"

# registries.yaml 확인 (GHCR 인증)
if [ ! -f /etc/rancher/k3s/registries.yaml ]; then
    echo "ERROR: /etc/rancher/k3s/registries.yaml 이 없습니다."
    echo "k3s-ec2-setup.sh를 먼저 실행하고 registries.yaml을 설정하세요."
    exit 1
fi

# giftify 네임스페이스 확인
if ! kubectl get namespace giftify &> /dev/null; then
    echo ">>> giftify 네임스페이스 생성중..."
    kubectl apply -f "${BASE_DIR}/namespace.yaml"
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
    "ghcr.io/prgrms-be-adv-devcourse/beadv4_4_team201_be/api-server=ghcr.io/prgrms-be-adv-devcourse/beadv4_4_team201_be/api-server:$IMAGE_TAG"
kubectl apply -k .

echo ">>> Rollout 대기중..."
if kubectl rollout status deployment/api-server -n giftify --timeout=600s; then
    echo ""
    echo "=== 배포 완료 ==="
    kubectl get pods -n giftify
else
    echo "!!! Rollout 실패 - 자동 롤백 수행"
    kubectl get pods -n giftify -l app=api-server
    kubectl logs -l app=api-server -n giftify --tail=50 || true
    kubectl rollout undo deployment/api-server -n giftify
    kubectl rollout status deployment/api-server -n giftify --timeout=240s
    echo "=== 롤백 완료 ==="
    kubectl get pods -n giftify
    exit 1
fi
