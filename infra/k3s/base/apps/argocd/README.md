# ArgoCD 설치 가이드

EC2 k3s 클러스터에 ArgoCD + Image Updater를 설치하는 절차.

## 사전 준비

- k3s 클러스터가 동작 중이어야 함
- `kubectl`이 k3s에 연결된 상태
- GitHub PAT (packages:read, repo 권한)

```bash
export GH_USERNAME="your-username"
export GH_PAT="your-pat-token"
```

## 1. ArgoCD 설치

```bash
kubectl create namespace argocd
kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml \
  --server-side --force-conflicts
```

> `--server-side --force-conflicts`: install.yaml의 CRD annotation이 262KB를 초과하여
> client-side apply가 실패함. server-side apply 필수.

Pod이 모두 Running 상태인지 확인:

```bash
kubectl get pods -n argocd
```

## 2. ArgoCD Image Updater 설치

```bash
kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj-labs/argocd-image-updater/stable/manifests/install.yaml
```

## 3. GHCR 인증 Secret 생성

Image Updater가 private GHCR에 접근하기 위한 인증 정보:

```bash
kubectl create secret docker-registry ghcr-creds \
  -n argocd \
  --docker-server=ghcr.io \
  --docker-username=$GH_USERNAME \
  --docker-password=$GH_PAT
```

## 4. Git 레포 인증 Secret 생성

ArgoCD가 private Git 레포에 접근하기 위한 인증 정보:

```bash
kubectl create secret generic repo-creds -n argocd \
  --from-literal=url=https://github.com/prgrms-be-adv-devcourse/beadv4_4_team201_be.git \
  --from-literal=username=$GH_USERNAME \
  --from-literal=password=$GH_PAT

kubectl label secret repo-creds -n argocd \
  argocd.argoproj.io/secret-type=repository
```

## 5. ArgoCD Server --insecure 설정

Cloudflare Tunnel -> Traefik -> ArgoCD 경로에서 HTTP로 접근하므로
ArgoCD의 내장 HTTPS redirect를 비활성화:

```bash
kubectl patch deployment argocd-server -n argocd \
  --type='json' \
  -p='[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--insecure"}]'
```

## 6. Application + IngressRoute + Image Updater config 적용

```bash
kubectl apply -f infra/k3s/base/apps/argocd/application.yaml
kubectl apply -f infra/k3s/base/apps/argocd/ingress.yaml
kubectl apply -f infra/k3s/base/apps/argocd/image-updater/config.yaml
```

## 7. 초기 비밀번호 확인

```bash
kubectl get secret argocd-initial-admin-secret -n argocd \
  -o jsonpath='{.data.password}' | base64 -d && echo
```

- Username: `admin`
- Password: 위 명령어 출력값

## 8. 검증

1. ArgoCD UI 접속: `http://giftify-argocd.chan99k.dev`
2. admin으로 로그인
3. `giftify` Application이 표시되는지 확인
4. Sync Status가 `Synced`, Health Status가 `Healthy`인지 확인

## 업그레이드

ArgoCD 버전 업그레이드 시:

```bash
kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/v{VERSION}/manifests/install.yaml
```

## 제거

```bash
kubectl delete -f infra/k3s/base/apps/argocd/application.yaml
kubectl delete -f infra/k3s/base/apps/argocd/ingress.yaml
kubectl delete -f infra/k3s/base/apps/argocd/image-updater/config.yaml
kubectl delete -n argocd \
  -f https://raw.githubusercontent.com/argoproj-labs/argocd-image-updater/stable/manifests/install.yaml
kubectl delete -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl delete namespace argocd
```
