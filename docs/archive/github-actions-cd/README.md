# GitHub Actions CD Pipeline (Archived)

ArgoCD 도입으로 대체되어 2026-02-25 아카이브.

## 아카이브된 파일

| 파일 | 원래 위치 | 역할 |
|------|-----------|------|
| `cd.yaml` | `.github/workflows/cd.yaml` | CI 완료 후 self-hosted runner에서 k3s 배포 트리거 |
| `k3s-ec2-deploy.sh` | `infra/k3s/scripts/k3s-ec2-deploy.sh` | kustomize 기반 수동 배포 스크립트 |

## 대체 방식

ArgoCD가 Git 저장소(`infra/k3s/overlays/prod`)를 감시하여 변경 감지 시 자동 Sync.
