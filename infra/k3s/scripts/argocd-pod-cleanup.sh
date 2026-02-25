#!/bin/bash
# k3s 재시작 후 Completed 상태로 stuck된 ArgoCD Pod 자동 정리
# systemd 서비스(argocd-pod-cleanup.service)로 등록하여 사용
set -e

export KUBECONFIG=/etc/rancher/k3s/k3s.yaml

# k3s API server가 준비될 때까지 대기 (최대 120초)
TIMEOUT=120
ELAPSED=0
until kubectl get nodes &>/dev/null; do
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "ERROR: k3s API server not ready after ${TIMEOUT}s"
        exit 1
    fi
    sleep 5
    ELAPSED=$((ELAPSED + 5))
done

# Completed(Succeeded) 상태의 ArgoCD Pod 삭제
STUCK_PODS=$(kubectl get pods -n argocd --field-selector=status.phase==Succeeded -o name 2>/dev/null || true)

if [ -n "$STUCK_PODS" ]; then
    echo ">>> Stuck ArgoCD pods found, deleting:"
    echo "$STUCK_PODS"
    echo "$STUCK_PODS" | xargs kubectl delete -n argocd
    echo ">>> Cleanup complete. Deployments will recreate pods automatically."
else
    echo ">>> No stuck ArgoCD pods found."
fi
