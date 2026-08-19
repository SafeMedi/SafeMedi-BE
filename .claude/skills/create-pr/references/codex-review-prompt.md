# Codex 리뷰 프롬프트 템플릿

`create-pr` 스킬의 3단계에서 `Agent(subagent_type: "codex:codex-rescue")`에 넘길 프롬프트다. Codex는 이 대화의 어떤 맥락도 공유하지 않은 채로 시작하므로, 아래 자리표시자를 실제 값으로 채워서 통째로 전달한다. 값을 채우지 않고 "이 브랜치 리뷰해줘"처럼 보내면 Codex가 무엇을 봐야 하는지 몰라 얕은 리뷰만 하게 된다.

```
safemedi-backend 저장소(Kotlin 2.2 / Spring Boot 4 / Spring Security / Spring Data JPA / Flyway / MySQL)에서
`{{BASE_BRANCH}}` 브랜치 대비 `{{HEAD_BRANCH}}` 브랜치(현재 체크아웃된 브랜치)의 변경사항을 독립적으로 코드 리뷰해줘.
PR을 올리기 전 두 번째 검증 관점으로 요청하는 것이므로, 아직 알려지지 않은 문제를 찾는 데 집중해줘
(이미 컨텍스트를 공유한 다른 리뷰어와 결과를 나중에 취합할 예정).

diff 확인: `git diff {{BASE_BRANCH}}..{{HEAD_BRANCH}}` 또는 `git log {{BASE_BRANCH}}..{{HEAD_BRANCH}} -p`

## 작업 배경
{{TICKET_OR_CONTEXT_SUMMARY}}
<!-- 예: Linear 티켓 SAF-164 요약, 이 변경이 왜 필요한지, 관련 API 명세 핵심 정책.
     티켓이 없으면 커밋 메시지/PR 목적을 2~3문장으로 요약해서 채운다. -->

## 변경 파일
{{CHANGED_FILES_LIST}}
<!-- git diff {{BASE_BRANCH}}...{{HEAD_BRANCH}} --stat 결과를 그대로 붙여넣거나,
     신규/수정 파일을 역할별로 묶어서 정리 -->

## 리뷰 관점 (repo AGENTS.md 기준, 반드시 확인)
- 버그, 보안(토큰/개인정보/민감 정보 로그·응답 노출), 데이터 정합성
- 트랜잭션 경계 (`@Transactional` 누락/오남용)
- 엔티티 변경 시 Flyway migration 누락 여부
- 인증이 필요한 API가 security/JWT 규칙을 우회하는지 (SecurityConfig 변경이 있다면 특히)
- `open-in-view: false`에서 lazy loading 예외 가능성
- 예외가 GlobalExceptionHandler/ErrorCode 체계를 우회해서 노출되는지
- 외부 API 연동 코드가 있다면: 타임아웃, 재시도, 에러 처리, 동시성/스레드 안전성, 리소스 누수
- 배포 워크플로우(.github/workflows/*.yml) 변경이 있다면: 신규 필수 secret이 실제로 컨테이너까지 전달되는지
- 테스트 커버리지 공백

찾은 이슈는 파일 경로:라인, 문제 설명, 재현/실패 시나리오, 심각도를 포함해서 정리해줘.
이슈가 없으면 없다고 명확히 알려줘. 전체 응답은 한국어로, 400단어 이내로 간결하게 요약해줘.
```

## 자리표시자 채우는 법

- `{{BASE_BRANCH}}`: 보통 `dev` 고정.
- `{{HEAD_BRANCH}}`: `git branch --show-current` 결과.
- `{{TICKET_OR_CONTEXT_SUMMARY}}`: Linear 티켓이 있으면 티켓 제목+핵심 정책 요약, 없으면 커밋 메시지 기반 요약.
- `{{CHANGED_FILES_LIST}}`: `git diff {{BASE_BRANCH}}...{{HEAD_BRANCH}} --stat` 출력.

Codex 결과는 텍스트 응답으로 돌아온다(구조화된 스키마 없음) — Claude 쪽 `ReportFindings` 취합 단계에서 파일:라인 단위로 파싱해 실제 코드와 대조 검증한다.
