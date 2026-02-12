#!/bin/bash

CLUSTER_NAME="${1:-giftify}"

echo ">>> k3d 클러스터 생성중 : $CLUSTER_NAME"

k3d cluster create "$CLUSTER_NAME" \
  --api-port 6550 \
  --servers 1 \
  --agents 1 \
  --port "8080:80@loadbalancer" \
  --port "8443:443@loadbalancer"

echo ">>> k3d 클러스터 생성 완료"

kubectl cluster-info
