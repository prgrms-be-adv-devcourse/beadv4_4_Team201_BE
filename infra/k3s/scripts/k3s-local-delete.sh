#!/bin/bash
set -e

CLUSTER_NAME="${1:-giftify}"

echo ">>> k3d 클러스터 삭제중 : $CLUSTER_NAME"
  k3d cluster delete "$CLUSTER_NAME"
echo ">>> k3d 클러스터 삭제 완료"
