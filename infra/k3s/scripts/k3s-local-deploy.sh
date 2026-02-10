#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}/../../.."

echo ">>> Docker image 굽는 중..."
cd "$PROJECT_ROOT"
docker build -t giftify-backend:local -f bootstrap/api-server/Dockerfile .

echo ">>> 이미지 k3d 로 옮기는 중..."
k3d image import giftify-backend:local -c giftify

echo ">>> 시크릿 반영중..."
if [ -f "${SCRIPT_DIR}/../base/secrets.yaml" ]; then
    kubectl apply -f "${SCRIPT_DIR}/../base/secrets.yaml"
else
    echo "WARNING: secrets.yaml not found. secrets.yaml.template을 복사하여 실제 값을 채워주세요."
fi

echo ">>> Kustomize를 활용한 배포중..."
kubectl apply -k "${SCRIPT_DIR}/../overlays/dev-k3s"

echo ">>> 백엔드 서버 rollout 대기중..."
kubectl rollout status deployment/backend -n giftify --timeout=180s

echo ">>> 배포 완료"
kubectl get pods -n giftify
