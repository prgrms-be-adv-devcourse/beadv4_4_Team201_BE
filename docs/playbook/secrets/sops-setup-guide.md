# SOPS + age Secret 관리 설정 가이드

Giftify 프로젝트의 Secret 관리 도구 설정 가이드.
SOPS로 Secret을 암호화하여 Git에 안전하게 저장하고, 팀원 간 공유할 수 있다.

---

## 개요

```
SOPS  = Secret 파일의 "값만" 암호화하는 도구 (키 이름은 평문 유지)
age   = 암호화 키 생성/관리 도구 (SOPS의 백엔드)
ksops = ArgoCD + Kustomize에서 SOPS 파일을 자동 복호화하는 플러그인

팀원 A가 Secret 암호화 → Git push
팀원 B가 Git pull → 자기 age 키로 복호화 → 로컬에서 사용
ArgoCD → ksops로 자동 복호화 → K8s Secret 생성
```

---

## 디렉터리 구조

```
infra/k3s/
  base/
    apps/api-server/
      configmap.yaml          # 비민감 환경변수 (DB_URL, REDIS_HOST 등)
      deployment.yaml         # envFrom: configMapRef + secretRef
    ...
  secrets-plain/              # 평문 Secret 원본 (.gitignore)
    api-server-secrets.yaml
    postgres-secrets.yaml
    redis-secrets.yaml
    grafana-secrets.yaml
  overlays/
    prod/
      *.enc.yaml              # SOPS 암호화 Secret (Git 추적)
      secret-generator.yaml   # ksops generator
      kustomization.yaml
```

### ConfigMap / Secret 분리 원칙

- **ConfigMap** (`api-server-config`): 비민감 환경설정 (URL, 호스트, 포트 등)
- **Secret** (`api-server-secrets`): 민감 자격증명 (패스워드, API 키, 토큰 등)
- 다른 워크로드(postgres, redis, grafana 등)는 Secret만 사용

---

## 1. 도구 설치

### macOS

```bash
brew install sops age
```

### Windows

**winget (권장):**

```powershell
winget install FiloSottile.age
winget install Mozilla.sops
```

**Chocolatey:**

```powershell
choco install age.portable
choco install sops
```

**수동 설치 (winget/choco 미사용 시):**

1. age: https://github.com/FiloSottile/age/releases 에서 `age-vX.X.X-windows-amd64.zip` 다운로드 → 압축 해제 → `age.exe`, `age-keygen.exe`를 PATH가 등록된 디렉토리에 복사
2. sops: https://github.com/getsops/sops/releases 에서 `sops-vX.X.X.exe` 다운로드 → `sops.exe`로 이름 변경 → PATH가 등록된 디렉토리에 복사

### Linux (Ubuntu/Debian)

```bash
sudo apt install age

# sops (공식 릴리스에서 다운로드)
SOPS_VERSION=3.9.4
curl -LO "https://github.com/getsops/sops/releases/download/v${SOPS_VERSION}/sops-v${SOPS_VERSION}.linux.amd64"
sudo mv "sops-v${SOPS_VERSION}.linux.amd64" /usr/local/bin/sops
sudo chmod +x /usr/local/bin/sops
```

### 설치 확인

```bash
sops --version
# sops 3.9.x 이상

age --version
# v1.x.x 이상
```

---

## 2. age 키 생성

각 팀원이 **한 번만** 실행하면 된다. 이미 키가 있으면 이 단계를 건너뛴다.

### macOS / Linux

```bash
mkdir -p ~/.config/sops/age
age-keygen -o ~/.config/sops/age/keys.txt
```

### Windows (PowerShell)

```powershell
mkdir -Force "$env:APPDATA\sops\age"
age-keygen -o "$env:APPDATA\sops\age\keys.txt"
```

### 출력 예시

```
Public key: age1ql3z7hjy54pw3hyww5ayyfg7zqgvc7w3j2elw8zmrj2kg5sfn9aqmcac8p
```

### SOPS_AGE_KEY_FILE 환경변수 설정

macOS에서 SOPS는 기본적으로 `~/Library/Application Support/sops/age/keys.txt`를 찾는다.
`~/.config/sops/age/keys.txt`에 키를 생성했다면 환경변수를 설정해야 한다.

```bash
# macOS / Linux (.zshrc 또는 .bashrc에 추가)
export SOPS_AGE_KEY_FILE="$HOME/.config/sops/age/keys.txt"
```

```powershell
# Windows (PowerShell 프로필에 추가)
$env:SOPS_AGE_KEY_FILE = "$env:APPDATA\sops\age\keys.txt"
```

### 키 확인

```bash
# macOS / Linux
cat ~/.config/sops/age/keys.txt

# Windows (PowerShell)
cat "$env:APPDATA\sops\age\keys.txt"
```

```
# created: 2026-03-18T10:00:00+09:00
# public key: age1ql3z7hjy54pw3hyww5ayyfg7zqgvc7w3j2elw8zmrj2kg5sfn9aqmcac8p
AGE-SECRET-KEY-1XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

### 주의사항

- **공개키** (`age1...`): 팀원에게 공유 OK. `.sops.yaml`에 등록된다.
- **개인키** (`AGE-SECRET-KEY-...`): 절대 공유 금지. 본인 머신에만 보관.
- 키 파일 분실 시 새로 생성하고 `.sops.yaml`에 공개키 교체 → `sops updatekeys` 실행.

---

## 3. 공개키 공유

키 생성 후 **공개키만** 팀 채널(Discord/Slack)에 공유한다.

```
@팀원 SOPS 공개키 공유합니다:
age1ql3z7hjy54pw3hyww5ayyfg7zqgvc7w3j2elw8zmrj2kg5sfn9aqmcac8p
```

인프라 담당자가 모든 팀원의 공개키를 `.sops.yaml`에 등록한다.

---

## 4. 일상 사용법

### Secret 평문 확인 (복호화)

```bash
# 특정 Secret 복호화
sops -d infra/k3s/overlays/prod/api-server-secrets.enc.yaml

# 모든 Secret 한번에 확인
for f in infra/k3s/overlays/prod/*.enc.yaml; do
  echo "=== $(basename $f) ==="
  sops -d "$f"
  echo
done
```

### Secret 편집

```bash
# 에디터에서 평문으로 열림 → 수정 → 저장 시 자동 재암호화
sops infra/k3s/overlays/prod/api-server-secrets.enc.yaml
```

기본 에디터를 변경하려면:

```bash
# macOS / Linux
export EDITOR=vim   # 또는 nano, code --wait 등

# Windows (PowerShell)
$env:EDITOR = "notepad"
```

### Secret 새로 만들기

```bash
# 1. 평문 Secret YAML을 secrets-plain/ 에 작성
vi infra/k3s/secrets-plain/my-secret.yaml

# 2. SOPS로 암호화하여 overlay에 배치
sops -e infra/k3s/secrets-plain/my-secret.yaml \
  > infra/k3s/overlays/prod/my-secret.enc.yaml

# 3. secret-generator.yaml의 files 목록에 추가
vi infra/k3s/overlays/prod/secret-generator.yaml

# 4. Git 커밋 (암호화된 파일 + generator만, 평문은 .gitignore)
git add infra/k3s/overlays/prod/my-secret.enc.yaml
git add infra/k3s/overlays/prod/secret-generator.yaml
```

### Secret 값 일괄 재암호화 (평문 수정 후)

```bash
# secrets-plain/ 의 평문을 수정한 뒤 overlay에 반영
for name in api-server-secrets postgres-secrets redis-secrets \
            grafana-secrets; do
  sops -e infra/k3s/secrets-plain/${name}.yaml \
    > infra/k3s/overlays/prod/${name}.enc.yaml
done
```

### Git diff 예시

SOPS 암호화 파일의 diff는 키 이름이 평문이라 "무엇이 변경됐는지" 알 수 있다:

```diff
  stringData:
    DB_PASSWORD: ENC[AES256_GCM,data:xxx...,type:str]
-   REDIS_PASSWORD: ENC[AES256_GCM,data:old...,type:str]
+   REDIS_PASSWORD: ENC[AES256_GCM,data:new...,type:str]
    AUTH0_CLIENT_SECRET: ENC[AES256_GCM,data:yyy...,type:str]
```

---

## 5. 팀원 추가/제거

### 팀원 추가

```bash
# 1. 새 팀원의 공개키를 .sops.yaml에 추가 (에디터로 수동 편집)
# 2. 기존 모든 암호화 파일에 새 키 적용
for f in infra/k3s/overlays/prod/*.enc.yaml; do
  sops updatekeys "$f"
done
# 3. Git 커밋
```

### 팀원 제거

```bash
# 1. .sops.yaml에서 해당 공개키 삭제
# 2. sops updatekeys 실행
for f in infra/k3s/overlays/prod/*.enc.yaml; do
  sops updatekeys "$f"
done
# 3. Secret 값 자체도 교체 (퇴사자가 값을 알고 있으므로!)
sops infra/k3s/overlays/prod/api-server-secrets.enc.yaml
# → 에디터에서 비밀번호 등 변경 → 저장
# 4. Git 커밋
```

---

## 6. 트러블슈팅

### "could not decrypt data key" 오류

```
원인: 내 age 개인키가 .sops.yaml에 등록된 공개키와 매칭되지 않음
해결:
  1. 내 공개키 확인: grep "public key" ~/.config/sops/age/keys.txt
  2. .sops.yaml에 내 공개키가 있는지 확인
  3. 없으면 인프라 담당자에게 공개키 등록 요청
```

### "no matching keys found in key sources" 오류

```
원인: SOPS가 age 키 파일을 찾지 못함
해결:
  macOS/Linux: ls ~/.config/sops/age/keys.txt
  Windows:     dir "$env:APPDATA\sops\age\keys.txt"

  파일이 없으면 2번(age 키 생성) 다시 실행.
  경로가 다르면 환경변수로 지정:
    export SOPS_AGE_KEY_FILE=/path/to/keys.txt        # macOS/Linux
    $env:SOPS_AGE_KEY_FILE = "C:\path\to\keys.txt"    # Windows
```

### "no matching creation rules found" 오류

```
원인: .sops.yaml의 path_regex가 입력 파일 경로와 매칭되지 않음
해결:
  1. .sops.yaml의 path_regex 확인
  2. sops -e 실행 시 입력 파일 경로가 regex에 매칭되는지 확인
  3. path_regex는 출력 파일이 아닌 입력 파일 경로를 기준으로 매칭됨
```

### Windows에서 에디터가 안 열림

```powershell
# EDITOR 환경변수 설정
$env:EDITOR = "notepad"
# 또는 VS Code 사용 시
$env:EDITOR = "code --wait"
```

---

## 참고

- [SOPS 공식 문서](https://getsops.io/docs/)
- [age 공식 저장소](https://github.com/FiloSottile/age)
- [ksops (Kustomize SOPS 플러그인)](https://github.com/viaduct-ai/kustomize-sops)
