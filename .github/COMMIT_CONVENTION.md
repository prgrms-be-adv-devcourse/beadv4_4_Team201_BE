# Commit Message Convention

## Format

```
<Type>: <Subject>

<Body>

<Footer>
```

## Types

- **Feat**: 새로운 기능 추가
- **Fix**: 버그 수정
- **Docs**: 문서 수정
- **Style**: 코드 포맷팅 (코드 변경 없음)
- **Refactor**: 리팩토링
- **Test**: 테스트 코드
- **Chore**: 빌드, 패키지 매니저 수정
- **Design**: UI 디자인 변경
- **Comment**: 주석 추가/변경
- **Rename**: 파일/폴더 이름 변경
- **Remove**: 파일 삭제
- **!HOTFIX**: 긴급 버그 수정
- **!BREAKING CHANGE**: API 변경

## Rules

1. Type은 영문 대문자로 시작
2. Subject는 한글, 50자 이내, 명령조
3. Subject 끝에 마침표 없음
4. Body는 무엇을, 왜 변경했는지 설명
5. Footer에 이슈 번호 포함: `Resolves #123`

## Examples

```
Feat: 회원 로그인 API 추가

JWT 기반 인증 방식으로 로그인 기능 구현
- Spring Security 설정 추가
- JWT 토큰 생성 및 검증 로직 구현

Resolves #123
```

```
Fix: 결제 금액 계산 오류 수정

할인율 적용 순서 변경으로 정확한 금액 계산
- 쿠폰 할인 먼저 적용 후 포인트 차감

Resolves #456
```
