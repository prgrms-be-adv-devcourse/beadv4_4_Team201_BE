# ArgoCD 설치 가이드

k3s 클러스터에 ArgoCD + ksops + Image Updater를 설치하는 절차.

ArgoCD 본체는 Kustomize patch로 설치하여 ksops 플러그인과 age 키를 선언적으로 관리한다.

## 사전 준비

- k3s 클러스터가 동작 중이어야 함
- `kubectl`이 k3s에 연결된 상태
- GitHub PAT (packages:read, repo 권한)
- age 개인키 파일 (`keys.txt`) — 로컬에서 `gcloud compute scp`로 전송

```bash
export GH_USERNAME="your-username"
export GH_PAT="your-pat-token"
```

## 1. Secret 사전 생성

ArgoCD 설치 전에 필요한 Secret을 먼저 생성한다.

```bash
kubectl create namespace argocd
```

### age 개인키 (ksops SOPS 복호화용)

```bash
kubectl create secret generic sops-age-key \
  -n argocd \
  --from-file=keys.txt=/tmp/keys.txt
rm /tmp/keys.txt
```

### GHCR 인증 (Image Updater용)

```bash
kubectl create secret docker-registry ghcr-creds \
  -n argocd \
  --docker-server=ghcr.io \
  --docker-username=$GH_USERNAME \
  --docker-password=$GH_PAT
```

### Git 레포 인증 (repo-server용)

```bash
kubectl create secret generic repo-creds -n argocd \
  --from-literal=url=https://github.com/prgrms-be-adv-devcourse/beadv4_4_team201_be.git \
  --from-literal=username=$GH_USERNAME \
  --from-literal=password=$GH_PAT

kubectl label secret repo-creds -n argocd \
  argocd.argoproj.io/secret-type=repository
```

## 2. ArgoCD + ksops 설치 (Kustomize)

`infra/k3s/argocd/kustomization.yaml`에 다음이 포함되어 있다:

- ArgoCD 공식 `install.yaml`을 base로 사용
- repo-server에 ksops initContainer + age 키 volume mount 패치
- argocd-server에 `--insecure` 플래그 패치 (Cloudflare Tunnel이 TLS 종단)
- argocd-cm에 `kustomize.buildOptions: --enable-alpha-plugins --enable-exec` 설정

```bash
kubectl apply -k infra/k3s/argocd/ --server-side --force-conflicts
```

> `--server-side --force-conflicts`: install.yaml의 CRD annotation이 262KB를 초과하여
> client-side apply가 실패함. server-side apply 필수.

Pod이 모두 Running 상태인지 확인:

```bash
kubectl get pods -n argocd
```

## 3. NetworkPolicy 제거

ArgoCD 기본 NetworkPolicy가 Traefik(kube-system)에서 오는 트래픽을 차단하므로 삭제:

```bash
kubectl delete networkpolicy argocd-server-network-policy \
  -n argocd 2>/dev/null || true
```

## 4. ArgoCD Image Updater 설치

```bash
kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj-labs/argocd-image-updater/stable/manifests/install.yaml
```

## 5. Application + IngressRoute + Image Updater config 적용

```bash
kubectl apply -f infra/k3s/base/apps/argocd/application.yaml
kubectl apply -f infra/k3s/base/apps/argocd/ingress.yaml
kubectl apply -f infra/k3s/base/apps/argocd/image-updater/config.yaml
```

## 6. 초기 비밀번호 확인

```bash
kubectl get secret argocd-initial-admin-secret -n argocd \
  -o jsonpath='{.data.password}' | base64 -d && echo
```

- Username: `admin`
- Password: 위 명령어 출력값

## 7. 검증

### 기본 동작

1. ArgoCD UI 접속: `http://giftify-argocd.chan99k.dev`
2. admin으로 로그인
3. `giftify` Application이 표시되는지 확인
4. Sync Status가 `Synced`, Health Status가 `Healthy`인지 확인

### ksops 복호화 확인

giftify 네임스페이스에 SOPS 암호화 Secret이 복호화되어 생성되었는지 확인:

```bash
kubectl get secrets -n giftify | grep -E "api-server|postgres|redis|grafana"
```

4개 Secret이 존재하면 ksops가 정상 동작하는 것.

### Image Updater 확인

```bash
kubectl logs -n argocd deployment/argocd-image-updater --tail=20
```

GHCR 레지스트리 폴링 로그가 출력되면 정상.

## 업그레이드

`infra/k3s/argocd/kustomization.yaml`의 resources URL 버전을 변경 후 재적용:

```bash
# kustomization.yaml에서 URL을 특정 버전으로 변경
# - https://raw.githubusercontent.com/argoproj/argo-cd/v{VERSION}/manifests/install.yaml

kubectl apply -k infra/k3s/argocd/ --server-side --force-conflicts
```

## 제거

```bash
kubectl delete -f infra/k3s/base/apps/argocd/application.yaml
kubectl delete -f infra/k3s/base/apps/argocd/ingress.yaml
kubectl delete -f infra/k3s/base/apps/argocd/image-updater/config.yaml
kubectl delete -n argocd \
  -f https://raw.githubusercontent.com/argoproj-labs/argocd-image-updater/stable/manifests/install.yaml
kubectl delete -k infra/k3s/argocd/
kubectl delete namespace argocd
```
