#!/bin/bash
# EC2 최소 요구사항:
#   인스턴스: t3.large (2 vCPU, 8 GiB RAM)
#   EBS 루트 볼륨: 30 GiB gp3 이상 (PVC + 이미지 캐시 + OS)
#   OS: Amazon Linux 2023
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K3S_VERSION="${K3S_VERSION:-v1.31.5+k3s1}"

echo "=== Giftify k3s EC2 셋업 ==="
echo ">>> k3s 버전: $K3S_VERSION"
echo ""

# k3s 설치
if command -v k3s &> /dev/null; then
    echo ">>> k3s 이미 설치됨: $(k3s --version)"
else
    echo ">>> k3s 설치중..."
    curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION="$K3S_VERSION" sh -s - \
        --write-kubeconfig-mode 644
    echo ">>> k3s 설치 완료"
fi

# kubeconfig 설정
if [ ! -f "$HOME/.kube/config" ]; then
    mkdir -p "$HOME/.kube"
    sudo cp /etc/rancher/k3s/k3s.yaml "$HOME/.kube/config"
    sudo chown "$(id -u):$(id -g)" "$HOME/.kube/config"
    echo ">>> kubeconfig 복사됨: $HOME/.kube/config"
else
    echo ">>> kubeconfig 이미 존재: $HOME/.kube/config"
fi

# kustomize 설치
if ! command -v kustomize &> /dev/null; then
    echo ">>> kustomize 설치중..."
    curl -sL "https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh" | bash
    sudo mv kustomize /usr/local/bin/
    echo ">>> kustomize 설치 완료"
else
    echo ">>> kustomize: $(kustomize version --short 2>/dev/null || kustomize version)"
fi

# registries.yaml 안내
REGISTRIES_FILE="/etc/rancher/k3s/registries.yaml"
if [ ! -f "$REGISTRIES_FILE" ]; then
    echo ""
    echo ">>> GHCR 인증을 위해 registries.yaml을 설정해야 합니다."
    echo ""
    echo "    sudo cp ${SCRIPT_DIR}/../configs/registries.yaml.template $REGISTRIES_FILE"
    echo "    sudo vi $REGISTRIES_FILE   # <GHCR_PAT> 를 실제 토큰으로 교체"
    echo "    sudo systemctl restart k3s"
    echo ""
else
    echo ">>> registries.yaml 이미 존재: $REGISTRIES_FILE"
fi

# 상태 확인
echo ""
echo ">>> k3s 서비스 상태:"
sudo systemctl is-active k3s || true

echo ""
echo ">>> 노드 상태:"
kubectl get nodes

echo ""
echo "=== 셋업 완료 ==="
echo "다음 단계:"
echo "  1. registries.yaml 설정 (GHCR 인증)"
echo "  2. k3s-ec2-deploy.sh <IMAGE_TAG> 실행"
