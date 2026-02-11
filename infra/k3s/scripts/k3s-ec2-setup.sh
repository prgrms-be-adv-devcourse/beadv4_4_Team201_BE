#!/bin/bash
# EC2 최소 요구사항:
#   인스턴스: t3.large (2 vCPU, 8 GiB RAM)
#   EBS 루트 볼륨: 30 GiB gp3 이상 (PVC 10GiB + 이미지 캐시 + OS)
#   OS: Amazon Linux 2023
set -e

CLUSTER_NAME="${1:-giftify}"

echo "=== Giftify k3d EC2 셋업 ==="
echo ""

# Docker
if ! command -v docker &> /dev/null; then
    echo ">>> Docker 설치중..."
    sudo dnf install -y docker
    sudo systemctl enable --now docker
    sudo usermod -aG docker "$(whoami)"
    echo ">>> Docker 설치 완료. 그룹 변경 적용을 위해 재로그인 후 이 스크립트를 다시 실행하세요."
    exit 0
fi
echo ">>> Docker: $(docker --version)"

# k3d
if ! command -v k3d &> /dev/null; then
    echo ">>> k3d 설치중..."
    K3D_VERSION="v5.7.5"
    curl -fLo /tmp/k3d "https://github.com/k3d-io/k3d/releases/download/${K3D_VERSION}/k3d-linux-amd64"
    sudo install -o root -g root -m 0755 /tmp/k3d /usr/local/bin/k3d
    rm -f /tmp/k3d
fi
echo ">>> k3d: $(k3d --version)"

# kubectl
if ! command -v kubectl &> /dev/null; then
    echo ">>> kubectl 설치중..."
    KUBECTL_VERSION=$(curl -sL https://dl.k8s.io/release/stable.txt)
    curl -fLo /tmp/kubectl "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl"
    curl -fLo /tmp/kubectl.sha256 "https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl.sha256"
    echo "$(cat /tmp/kubectl.sha256)  /tmp/kubectl" | sha256sum -c -
    sudo install -o root -g root -m 0755 /tmp/kubectl /usr/local/bin/kubectl
    rm -f /tmp/kubectl /tmp/kubectl.sha256
fi
echo ">>> kubectl: $(kubectl version --client --short 2>/dev/null || kubectl version --client)"

# k3d 클러스터 생성
if k3d cluster list 2>/dev/null | grep -q "$CLUSTER_NAME"; then
    echo ">>> k3d 클러스터 '$CLUSTER_NAME' 이미 존재합니다."
else
    echo ">>> k3d 클러스터 생성중: $CLUSTER_NAME"
    k3d cluster create "$CLUSTER_NAME" \
        --api-port 6550 \
        --servers 1 \
        --agents 1 \
        --port "8080:80@loadbalancer" \
        --port "8443:443@loadbalancer"
    echo ">>> k3d 클러스터 생성 완료"
fi

kubectl cluster-info
echo ""
echo "=== 셋업 완료 ==="
echo "다음 단계: k3s-ec2-deploy.sh <IMAGE_TAG> 실행"
