#!/bin/bash
# k3s 재시작 후 kubeconfig 파일 권한을 644로 설정
# systemd 서비스(k3s-kubeconfig-fix.service)로 등록하여 사용
set -e

K3S_YAML="/etc/rancher/k3s/k3s.yaml"

# k3s.yaml이 생성될 때까지 대기 (최대 60초)
TIMEOUT=60
ELAPSED=0
until [ -f "$K3S_YAML" ]; do
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "ERROR: $K3S_YAML not found after ${TIMEOUT}s"
        exit 1
    fi
    sleep 2
    ELAPSED=$((ELAPSED + 2))
done

chmod 644 "$K3S_YAML"
echo ">>> kubeconfig permission fixed: $(ls -la "$K3S_YAML")"
