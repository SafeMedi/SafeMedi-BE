---
name: create-pr
description: Run this repo's mandatory pre-PR gate before opening any pull request to dev — verify test coverage exists for changed code, run the full ./gradlew test suite, run a parallel Claude + Codex review of the diff and synthesize findings, and only then create the PR with gh. ALWAYS use this skill when the user asks to create a PR, open a pull request, "PR 만들어줘", "PR 생성해줘", "dev로 PR 열어줘", ship a branch, or merge changes into dev — even if they don't mention tests or review, since skipping straight to `gh pr create` on this repo skips required verification. Also trigger it for "PR 올리기 전에 리뷰해줘" or "테스트랑 리뷰 다 하고 PR 만들어줘".
---

# create-pr

이 저장소(safemedi-backend)에서 PR을 열기 전에 항상 거쳐야 하는 4단계 게이트다: **테스트 존재 확인 → 전체 테스트 통과 → Claude+Codex 병렬 리뷰 → PR 생성.** 이 순서를 건너뛰거나 뒤바꾸지 않는다 — 각 단계는 다음 단계가 신뢰할 수 있는 상태를 전제로 한다(리뷰는 그린 테스트 위에서, PR은 처리된 리뷰 위에서 의미가 있다).

이 스킬은 이미 커밋된 브랜치를 대상으로 한다. 커밋하지 않은 변경이나 브랜치 생성은 이 스킬의 범위 밖이다.

## 0. 사전 확인

```bash
git branch --show-current
git status --short
```

- 현재 브랜치가 `dev`/`main`이면 중단하고 사용자에게 알린다 — 이 스킬은 feature 브랜치에서만 의미가 있다.
- `git status --short`가 비어있지 않으면(커밋 안 된 변경 존재) 진행하지 않는다. 사용자에게 커밋할지, stash 할지, 이대로는 리뷰 대상에서 빠진다는 점을 알릴지 물어본다 — 임의로 커밋하지 않는다.
- base 브랜치는 이 저장소 컨벤션상 `dev`로 고정한다(AGENTS.md: "브랜치와 PR 흐름은 dev를 개발 기준 브랜치로 본다"). 이후 모든 diff는 `git diff dev...<current-branch>` 기준이다.

## 1. 테스트 존재 확인

```bash
git diff dev...<branch> --stat
```

변경된 `src/main/kotlin/.../{controller,service}/*.kt` 파일마다, `src/test/kotlin/`의 같은 패키지 경로에 대응하는 테스트 파일이 있는지 확인한다(`ControllerTest.kt` / `ServiceTest.kt` 네이밍, 이 저장소 기존 파일들 참고). `entity`/`dto`/`repository`만 바뀌고 controller/service 변경이 없다면 이 체크는 완화해도 된다 — AGENTS.md 규칙은 "새 API 추가 시 컨트롤러 테스트 또는 서비스 테스트 중 위험도에 맞는 최소 테스트"이지, 모든 파일에 대한 테스트가 아니다.

테스트가 빠진 controller/service가 있으면 목록으로 보여주고 계속할지 묻는다. 자동으로 테스트를 대신 작성하지 않는다 — 이건 신호일 뿐, 사용자의 판단이 필요하다.

## 2. 전체 테스트 통과

```bash
./gradlew test
```

**실패하면 여기서 멈춘다.** 리뷰나 PR 생성으로 넘어가지 않는다. 실패 로그를 요약해서 보여주고, 원인을 파악해 고칠지 물어본다. 고쳤다면 이 단계부터 다시 시작한다(재실행해서 그린인지 확인 후에만 3단계로 넘어간다) — 리뷰는 깨진 코드 위에서 돌려봐야 의미가 없다.

## 3. Claude + Codex 병렬 리뷰

**한 메시지 안에서 두 개를 동시에 호출한다** (병렬 실행이 이 단계의 핵심이다 — 순차로 돌리면 시간만 두 배):

1. **Claude 측**: `Skill` 도구로 `code-review`를 `args: "high dev"`로 호출한다. 이 스킬은 내부적으로 여러 관점(라인 스캔, 제거된 동작, 크로스파일 추적, 재사용, 단순화, 효율성, 설계 고도, CLAUDE.md/AGENTS.md 컨벤션)의 finder를 병렬로 돌리고 자체 검증까지 거쳐 `ReportFindings`를 호출한다.
2. **Codex 측**: `Agent` 도구로 `subagent_type: "codex:codex-rescue"`를 호출한다. Codex는 이 대화의 맥락이 전혀 없는 상태로 시작하므로, 프롬프트에 diff 범위(`git diff dev...<branch>`), 이 PR의 목적/티켓 배경, 변경 파일 목록, 이 저장소 AGENTS.md의 "반드시 지적할 항목" 목록을 직접 채워 넣어야 한다. `references/codex-review-prompt.md`에 템플릿이 있다 — 그대로 값만 채워서 사용한다.

두 결과가 모두 돌아오면:

- **직접 읽어서 검증한다.** 서브에이전트가 보고한 파일:라인과 주장은 실제로 그 파일을 Read해서 확인하기 전까지는 사실이 아니다 — 라인 번호가 밀려 있거나 이미 고쳐진 코드를 지적하는 경우가 있다. 특히 심각도가 높거나 두 리뷰어가 겹쳐서 지적한 항목은 반드시 실제 코드로 재확인한다.
- **중복 제거하고 취합한다.** Claude와 Codex가 같은 지점을 독립적으로 지적했다면 그 자체가 신뢰도 신호이니 명시한다.
- **`ReportFindings`를 한 번 더 호출**해서 검증을 마친 최종 목록을 심각도 순으로 사용자에게 보여준다(빈 배열이면 그것도 명확히 보고). `code-review` 스킬이 이미 한 번 `ReportFindings`를 호출했더라도, 이 최종 취합 호출이 Codex 발견까지 포함한 완결된 리포트다.

## 4. 발견 사항 처리 — 반드시 사용자에게 물어본다

findings가 하나라도 있으면, 고치지도 무시하지도 않은 채로 사용자에게 진행 방식을 물어본다(`AskUserQuestion` 또는 평문 질문):
- 전부 수정 후 PR
- 일부만 골라서 수정
- 수정 없이 findings를 PR 본문/코멘트에 남기고 진행
- 무시하고 진행

findings가 없으면 곧장 5단계로 넘어간다.

수정하기로 했다면: 고치고 → **2단계(전체 테스트)부터 다시 실행**해서 그린인지 재확인한 뒤 5단계로 진행한다. 리뷰에서 잡은 버그를 고치면서 새 버그를 만들 수 있으므로, 수정 후 재검증 없이 바로 PR로 넘어가지 않는다.

## 5. PR 생성

```bash
git push -u origin <branch>   # 이미 추적 중이면 git push
```

`gh pr create --base dev --head <branch>`로 생성한다. 저장소의 `.github/PULL_REQUEST_TEMPLATE.md` 형식을 그대로 따라 본문을 채운다(파일이 옮겨졌거나 없으면 사용자에게 알리고 그 내용을 확인한다).

- **제목**: `[SAF-00] type: 요약` 형식(AGENTS.md 컨벤션). 티켓 번호는 브랜치명에서 추출한다(`git branch --show-current`가 `SAF-\d+` 패턴을 포함하면 그 번호 사용, 없으면 사용자에게 물어본다). `type`은 이 브랜치의 커밋 메시지들이 이미 따르고 있는 `[SAF-00] type: ...` 접두사에서 그대로 가져온다 — 커밋마다 type이 다르면(feat+fix 혼재 등) 가장 핵심적인 변경을 대표하는 type 하나를 고르거나 사용자에게 확인한다.
- **본문**: 한국어로 작성한다(AGENTS.md: "모든 설명, 리뷰 코멘트, 커밋 메시지는 한국어로 작성"). Summary/Changes/Type of change/Related Issue(Linear 티켓 번호)/API 테스트 결과 섹션을 모두 채운다. 3단계 리뷰에서 findings를 찾았다면(수정했든 안 했든) API 테스트 결과 섹션이나 별도 문단에 리뷰 결과 요약을 남긴다 — 다음 리뷰어가 이미 검증된 내용을 또 반복하지 않도록.

PR 생성 후 URL을 사용자에게 전달하고 끝낸다. 별도 요청이 없는 한 push/PR 생성 이후 추가 조치(머지, 라벨링 등)는 하지 않는다.
