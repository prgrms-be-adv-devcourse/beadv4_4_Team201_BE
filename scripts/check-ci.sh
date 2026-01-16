#!/bin/bash

   # 1. 색상 설정 (터미널 지원 여부 체크)
   if [[ -t 1 ]]; then
       GREEN='\033[0;32m'
       RED='\033[0;31m'
       YELLOW='\033[0;33m'
       BLUE='\033[0;34m'
       NC='\033[0m'
   else
       GREEN='' RED='' YELLOW='' BLUE='' NC=''
   fi

   echo -e "${BLUE}== 🛡️  Local CI Consistency Check (act) ==${NC}"

   # 2. OS 및 아키텍처 감지
   OS_TYPE="$(uname -s)"
   ARCH_TYPE="$(uname -m)"

   # 3. act 설치 여부 확인
   if ! command -v act &> /dev/null; then
       echo -e "${RED}❌ 'act'가 설치되어 있지 않습니다.${NC}"
       case "$OS_TYPE" in
           Darwin) echo "👉 실행: brew install act" ;;
           Linux)  echo "👉 참고: https://github.com/nektos/act" ;;
           *)      echo "👉 Windows: 'choco install act-cli' 또는 'scoop install act'" ;;
       esac
       exit 1
   fi

   # 4. Docker 소켓 탐색 (OrbStack, Docker Desktop, Linux Native 대응)
   SOCKET_PATH=""
   # 탐색 우선 순위: 1. OrbStack 2. Standard(Docker Desktop/Linux) 3. Rootless
   CANDIDATES=(
       "$HOME/.orbstack/run/docker.sock"
       "/var/run/docker.sock"
       "/run/user/$(id -u)/docker.sock"
   )

   for socket in "${CANDIDATES[@]}"; do
       if [ -S "$socket" ]; then
           SOCKET_PATH="$socket"
           break
       fi
   done

   ACT_OPTS=()

   # 5. 소켓 미검출 시 가이드 출력
   if [ -z "$SOCKET_PATH" ]; then
       echo -e "${YELLOW}⚠️  Docker 소켓을 찾을 수 없습니다.${NC}"
       echo -e "---------------------------------------------------"
       echo -e "💡 ${BLUE}Docker Desktop 사용자:${NC} Settings > Advanced > 'Allow the default Docker socket to be used' 체크 확인"
       echo -e "💡 ${BLUE}OrbStack 사용자:${NC} OrbStack이 실행 중인지 확인"
       echo -e "💡 ${BLUE}Linux 사용자:${NC} 'sudo usermod -aG docker \$USER' 후 재로그인 확인"
       echo -e "---------------------------------------------------"
   else
       echo -e "✅ 사용 중인 Docker 소켓: ${GREEN}$SOCKET_PATH${NC}"
       ACT_OPTS+=(--container-daemon-socket "$SOCKET_PATH")
   fi

   # 6. 아키텍처 최적화 (Apple Silicon 대응)
   if [[ "$OS_TYPE" == "Darwin" && "$ARCH_TYPE" == "arm64" ]]; then
       # CI 환경(AMD64)과의 일관성을 위해 강제 설정하거나,
       # 로컬 실행 속도를 위해 생략할 수 있습니다. 여기서는 일관성을 위해 유지합니다.
       echo -e "🍎 Apple Silicon 감지: ${YELLOW}linux/amd64${NC} 모드로 실행합니다."
       ACT_OPTS+=(--container-architecture linux/amd64)
   fi

   # 7. 프로젝트 루트 결정 (Git 기준)
   PROJECT_ROOT=$(git rev-parse --show-toplevel 2>/dev/null)
   if [[ -z "$PROJECT_ROOT" ]]; then
       PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
   fi

   cd "$PROJECT_ROOT" || exit 1
   echo -e "🚀 실행 위치: ${GREEN}$PROJECT_ROOT${NC}"
   echo "---------------------------------------------------"

   # 8. act 실행
   # -j: 특정 job 실행 (build-module)
   # --rm: 실행 후 컨테이너 삭제
   act push \
     -j build-module \
     --rm \
     "${ACT_OPTS[@]}"

   # 9. 결과 처리
   RESULT=$?
   echo "---------------------------------------------------"
   if [ $RESULT -eq 0 ]; then
       echo -e "${GREEN}✅ LOCAL CI 성공! 안전하게 Push 하셔도 됩니다.${NC}"
   else
       echo -e "${RED}❌ LOCAL CI 실패. 에러를 수정한 후 다시 시도해주세요.${NC}"
       exit $RESULT
   fi
