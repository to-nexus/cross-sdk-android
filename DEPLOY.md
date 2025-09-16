# 🚀 Cross SDK Android 배포 가이드

이 문서는 Cross SDK Android의 새로운 배포 시스템에 대한 완전한 가이드입니다.

## 📋 목차

- [배포 시스템 개요](#배포-시스템-개요)
- [배포 방법](#배포-방법)
- [스마트 태그 전략](#스마트-태그-전략)
- [버전 관리](#버전-관리)
- [Changeset 시스템](#changeset-시스템)
- [로컬 개발 도구](#로컬-개발-도구)
- [트러블슈팅](#트러블슈팅)

---

## 🎯 배포 시스템 개요

### 주요 특징

- ✅ **Main 브랜치 기반** - 태그 없이도 자동 배포
- ✅ **스마트 태그 전략** - BOM 변경 여부에 따른 적응형 태그
- ✅ **자동 버전 범프** - 커밋 메시지 기반 자동 버전 관리
- ✅ **SonarQube 통합** - 코드 품질 검사 후 배포
- ✅ **Changeset 시스템** - 자동 변경사항 추적
- ✅ **스냅샷 버저닝** - 고유한 스냅샷 버전 생성
- ✅ **Dry Run 지원** - 안전한 테스트 배포
- ✅ **모듈별 배포** - 특정 모듈만 선택 배포

### 배포 대상

| 환경 | 리포지토리 | 용도 |
|------|------------|------|
| **Snapshot** | `cross-sdk-android-snap/` | 개발/테스트용 |
| **Release** | `cross-sdk-android/` | 프로덕션용 |

---

## 🚀 배포 방법

### 1️⃣ 자동 배포 (Main 브랜치)

```bash
# 개발 완료 후 main 브랜치에 push
git add .
git commit -m "feat: add new feature"
git push origin main
```

**자동 실행 과정:**
1. SonarQube 코드 품질 검사
2. 변경사항 자동 감지
3. **자동 버전 범프** (커밋 메시지 분석)
4. Changeset 생성
5. Snapshot 배포

### 2️⃣ 수동 배포 (GitHub Actions UI)

#### 고급 배포 (Deploy SDK - Improved)

**GitHub** → **Actions** → **Deploy SDK (Improved)** → **Run workflow**

| 옵션 | 설명 | 예시 |
|------|------|------|
| `deploy_type` | 배포 타입 | `release`, `snapshot`, `both` |
| `version_bump_type` | 버전 범프 | `none`, `fix`, `release`, `manual` |
| `manual_versions` | 수동 버전 설정 | `BOM=1.2.0,APPKIT=1.2.1` |
| `target_modules` | 대상 모듈 | `foundation,core:android` |
| `dry_run` | 테스트 실행 | `true` (실제 배포 안함) |

#### 간단한 수동 배포 (Manual Deploy)

**GitHub** → **Actions** → **Manual Deploy (Simple)** → **Run workflow**

| 옵션 | 설명 | 값 |
|------|------|-----|
| `deploy_type` | 배포 타입 | `snapshot`, `release`, `both` |
| `modules` | 배포 모듈 | `foundation,appkit` (비어있으면 전체) |
| `version_action` | 버전 관리 | `no-change`, `auto-bump-fix`, `manual-set` |
| `manual_versions` | 수동 버전 | `BOM=1.2.0,APPKIT=1.2.1` |
| `dry_run` | 테스트 실행 | `true` |

### 3️⃣ 로컬 배포

```bash
# Release 배포만
./gradlew deploy -Ptype=release

# Snapshot 배포만
./gradlew deploySnap -Ptype=snap

# 양쪽 모두 배포
./gradlew deployBoth

# 환경별 배포 (레거시)
./gradlew deployDev/deployStage/deployProd
```

---

## 🏷️ 스마트 태그 전략

### 태그 생성 규칙

#### 1️⃣ BOM 기반 태그 (Full Release)
```bash
release/android-v1.2.0
```
- **조건**: BOM_VERSION이 실제로 변경됨
- **의미**: 전체 SDK의 메이저/마이너 릴리즈
- **GitHub Release**: "Full Release" 타입

#### 2️⃣ 모듈 패치 태그 (Module Update)
```bash
release/android-v1.0.1-patch.202501161430
```
- **조건**: BOM은 그대로, 개별 모듈 코드 변경
- **의미**: 특정 모듈의 버그 수정이나 개선
- **GitHub Release**: "Module Update" 타입

#### 3️⃣ 빌드 태그 (Build Release)
```bash
release/android-v1.0.2-build.a1b2c3d
```
- **조건**: 실제 모듈 코드 변경 없음
- **의미**: 빌드 설정, CI/CD, 문서 변경
- **GitHub Release**: "Build Release" 타입

### 변경사항 감지

| 경로 | 매핑 모듈 |
|------|-----------|
| `foundation/` | foundation |
| `core/android/` | android-core |
| `core/modal/` | modal-core |
| `protocol/sign/` | sign |
| `protocol/notify/` | notify |
| `product/appkit/` | appkit |
| `buildSrc/Versions.kt` | 버전 관리 |

---

## 📊 버전 관리

### 현재 모듈 버전

```kotlin
// buildSrc/src/main/kotlin/Versions.kt
const val BOM_VERSION = "1.0.2"           // 전체 SDK 버전
const val FOUNDATION_VERSION = "1.0.0"    // 기초 모듈
const val CORE_VERSION = "1.0.0"          // 안드로이드 코어
const val SIGN_VERSION = "1.0.0"          // 서명 프로토콜
const val NOTIFY_VERSION = "1.0.0"        // 알림 프로토콜
const val APPKIT_VERSION = "1.0.1"        // UI 킷
const val MODAL_CORE_VERSION = "1.0.0"    // 모달 코어
```

### 버전 범프 명령어

```bash
# 자동 버전 범프 (변경 감지 기반)
./gradlew versionBump -Ptype=fix      # 패치 버전 증가
./gradlew versionBump -Ptype=release  # 마이너 버전 증가

# 수동 버전 설정
./gradlew manualBump -PBOM=1.2.0 -PFOUNDATION=1.2.0 -PCORE=1.2.0

# 특정 모듈만 버전 범프
./gradlew releaseBump -Pmodules=APPKIT
./gradlew fixBump -Pmodules=CORE,SIGN
```

### 버전 범프 전략

#### 수동 버전 범프
| 변경 타입 | 범프 타입 | 예시 |
|-----------|-----------|------|
| **Breaking Changes** | Major | 1.0.0 → 2.0.0 |
| **New Features** | Minor | 1.0.0 → 1.1.0 |
| **Bug Fixes** | Patch | 1.0.0 → 1.0.1 |
| **Documentation** | None | 버전 변경 없음 |

#### 🤖 자동 버전 범프 (Main Push시)
| 커밋 메시지 패턴 | 감지 타입 | 범프 결과 | 예시 |
|------------------|-----------|-----------|------|
| `feat:` | release | Minor 증가 | 1.0.1 → 1.1.0 |
| `BREAKING CHANGE` | release | Minor 증가 | 1.0.1 → 1.1.0 |
| `fix:` | fix | Patch 증가 | 1.0.1 → 1.0.2 |
| `perf:` | fix | Patch 증가 | 1.0.1 → 1.0.2 |
| 기타 | fix | Patch 증가 (기본값) | 1.0.1 → 1.0.2 |

> **💡 참고**: Main 브랜치에 push할 때 코드 변경이 감지되면 커밋 메시지를 분석해서 자동으로 적절한 버전 범프를 수행합니다.

---

## 📝 Changeset 시스템

### 자동 Changeset 생성

```bash
# 자동 changeset 생성
./gradlew autoChangeset -Pversion=1.2.0 -Ptype=minor

# 모듈 목록 확인
./gradlew listModules

# 릴리즈 노트 생성
./gradlew generateReleaseNotes
```

### Changeset 파일 예시

```markdown
---
"io.crosstoken:foundation": minor
"io.crosstoken:android-core": minor
"io.crosstoken:sign": minor
"io.crosstoken:notify": minor
"io.crosstoken:appkit": minor
"io.crosstoken:modal-core": minor
"io.crosstoken:android-bom": minor
---

# Cross SDK Android Release 1.2.0

## ✨ Minor Release
New features and improvements

### 📦 Updated Modules
- `io.crosstoken:foundation`: 1.2.0
- `io.crosstoken:android-core`: 1.2.0
- `io.crosstoken:sign`: 1.2.0
- `io.crosstoken:notify`: 1.2.0
- `io.crosstoken:appkit`: 1.2.0
- `io.crosstoken:modal-core`: 1.2.0
- `io.crosstoken:android-bom`: 1.2.0

### 🏪 Repository
```kotlin
repositories {
    maven {
        url = uri("https://package.cross-nexus.com/repository/cross-sdk-android/")
    }
}
```
```

---

## 👨‍💻 개발자 가이드

### 🎯 개발 완료 후 Push 전 체크리스트

#### ✅ **권장 사전 작업**

```bash
# 1. 변경사항 분석
./gradlew analyzeChanges

# 2. 적절한 버전 범프 (권장)
# 새 기능 추가시
./gradlew versionBump -Ptype=release

# 버그 수정시  
./gradlew versionBump -Ptype=fix

# 특정 모듈만 수정시
./gradlew fixBump -Pmodules=APPKIT

# 3. 의미있는 커밋 메시지 작성
git add .
git commit -m "feat: add biometric authentication support"
# 또는
git commit -m "fix: resolve memory leak in AppKit modal"

# 4. Push
git push origin main
```

#### 📋 **커밋 메시지 가이드라인**

| 타입 | 형식 | 예시 | 결과 |
|------|------|------|------|
| **새 기능** | `feat: 설명` | `feat: add OAuth2 support` | Minor 버전 증가 |
| **버그 수정** | `fix: 설명` | `fix: resolve crash on startup` | Patch 버전 증가 |
| **성능 개선** | `perf: 설명` | `perf: optimize image loading` | Patch 버전 증가 |
| **Breaking Change** | `feat!: 설명` 또는 `BREAKING CHANGE:` | `feat!: remove deprecated API` | Minor 버전 증가 |
| **문서** | `docs: 설명` | `docs: update API documentation` | 버전 변경 없음 |
| **빌드/CI** | `ci: 설명` | `ci: update deployment workflow` | 버전 변경 없음 |

---

### ⚠️ **사전 작업을 안 했을 때 일어나는 일**

#### 🤖 **시스템 자동 처리 (백업 메커니즘)**

**시나리오 1: 의미있는 커밋 메시지 + 버전 범프 안함**
```bash
# 개발자 작업
git commit -m "fix: resolve modal crash in AppKit"
git push origin main  # 버전 범프 없이 push

# 🤖 시스템 자동 처리
✅ 커밋 메시지 분석: "fix:" → patch 범프 감지
✅ 변경된 모듈 감지: appkit
✅ 자동 버전 범프: APPKIT 1.0.1 → 1.0.2
✅ Changeset 자동 생성
✅ 배포: appkit:1.0.2-SNAPSHOT
✅ 자동 커밋: "chore: auto bump appkit to 1.0.2"
```

**시나리오 2: 애매한 커밋 메시지 + 버전 범프 안함**
```bash
# 개발자 작업
git commit -m "update appkit code"  # 애매한 메시지
git push origin main

# 🤖 시스템 자동 처리
⚠️ 커밋 메시지 분석: 타입 불명확 → 기본값(patch) 적용
✅ 변경된 모듈 감지: appkit
✅ 자동 버전 범프: APPKIT 1.0.1 → 1.0.2 (기본 patch)
⚠️ 부정확한 버전 범프 가능성
```

**시나리오 3: 문서만 수정**
```bash
# 개발자 작업
git commit -m "docs: update README"
git push origin main

# 🤖 시스템 자동 처리
✅ 변경된 모듈: 없음 (코드 변경 없음)
✅ 버전 범프: 실행 안됨
✅ 배포: 문서 변경만 반영
```

#### 🚨 **잠재적 문제점들**

| 상황 | 문제점 | 해결책 |
|------|--------|--------|
| **애매한 커밋 메시지** | 부정확한 버전 범프 | 명확한 커밋 메시지 작성 |
| **Breaking Change 미표시** | Minor 범프 대신 Major 필요 | `BREAKING CHANGE:` 명시 |
| **여러 모듈 동시 수정** | 일부 모듈만 범프될 수 있음 | 사전에 수동 범프 권장 |
| **복잡한 변경사항** | 자동 감지 한계 | 수동 버전 관리 권장 |

---

### 🎯 **권장 워크플로우**

#### **🥇 이상적인 플로우 (Best Practice)**
```bash
# 1. 개발 시작 전 브랜치 생성
git checkout -b feature/biometric-auth

# 2. 개발 완료 후 변경사항 분석
./gradlew analyzeChanges

# 3. 적절한 버전 범프
./gradlew versionBump -Ptype=release

# 4. 명확한 커밋 메시지
git add .
git commit -m "feat: add biometric authentication support

- Add fingerprint authentication
- Add face recognition support  
- Update security protocols
- Add comprehensive tests"

# 5. Main에 머지
git checkout main
git merge feature/biometric-auth
git push origin main

# ✅ 결과: 정확한 버전, 완전한 문서화, 추적 가능
```

#### **🥈 간소화된 플로우 (Quick & Safe)**
```bash
# 1. 개발 완료 후 의미있는 커밋 메시지만 작성
git add .
git commit -m "fix: resolve memory leak in AppKit modal"

# 2. Push (버전 범프는 시스템이 자동 처리)
git push origin main

# ✅ 결과: 자동 버전 관리, 기본적인 추적 가능
```

#### **🥉 최소한의 플로우 (Emergency)**
```bash
# 1. 급한 수정 후 바로 Push
git add .
git commit -m "hotfix for critical issue"
git push origin main

# ⚠️ 결과: 기본 patch 범프, 제한적 추적성
```

---

### 💡 **개발자 팁**

#### **🎯 효율적인 개발을 위한 팁**

1. **변경사항 미리 확인**
   ```bash
   # 현재 상태 확인
   ./gradlew listModules
   
   # 마지막 릴리즈 이후 변경사항
   ./gradlew analyzeChanges
   ```

2. **적절한 브랜치 전략**
   ```bash
   # 기능별 브랜치 사용
   git checkout -b feat/new-auth
   git checkout -b fix/modal-crash
   git checkout -b perf/optimize-loading
   ```

3. **커밋 메시지 템플릿 사용**
   ```bash
   # .gitmessage 파일 생성
   echo "type: subject

   body

   footer" > ~/.gitmessage
   
   # Git 설정
   git config --global commit.template ~/.gitmessage
   ```

4. **배포 전 로컬 테스트**
   ```bash
   # Dry run으로 배포 테스트
   # GitHub Actions → Manual Deploy → dry_run: true
   
   # 또는 로컬 빌드 테스트
   ./gradlew build
   ```

#### **🚨 주의사항**

- **Major 변경시 반드시 수동 관리**: Breaking changes는 자동 시스템으로 감지하기 어려움
- **복잡한 변경시 사전 계획**: 여러 모듈 동시 수정시 수동 버전 관리 권장
- **Production 배포는 별도**: Main push는 Snapshot 배포, Production은 수동 트리거

---

## 🛠️ 로컬 개발 도구

### 스마트 버저닝 도구

#### 변경사항 분석
```bash
./gradlew analyzeChanges
```

**출력 예시:**
```
📊 Current Module Versions:
BOM          | android-bom     | 1.0.2
FOUNDATION   | foundation      | 1.0.0
APPKIT       | appkit          | 1.0.1

🏷️ Last Release Tag: release/android-v1.0.1
📝 Changed Files Since Last Release:
  - product/appkit/src/main/kotlin/AppKit.kt
  - protocol/sign/src/main/kotlin/SignClient.kt

🔄 Changed Modules:
  - appkit
  - sign

⏭️ BOM Version Unchanged: 1.0.2
   Recommended: Module patch release

🎯 Recommended Tagging Strategy:
  🔄 Module Patch: release/android-v1.0.2-patch.202501161430
```

#### 버전 범프 제안
```bash
./gradlew suggestVersionBump
```

**출력 예시:**
```
📈 Suggested Version Bump Strategy:
  ✨ MINOR: New features detected
     Command: ./gradlew versionBump -Ptype=release
```

#### 스마트 태그 생성
```bash
# 자동 전략 (권장)
./gradlew generateSmartTag

# 수동 전략 지정
./gradlew generateSmartTag -Pstrategy=patch
./gradlew generateSmartTag -Pstrategy=build
./gradlew generateSmartTag -Pstrategy=bom

# 실제 태그 생성
./gradlew generateSmartTag -Pcreate=true
```

### 모듈 정보 확인

```bash
# 모든 모듈과 버전 확인
./gradlew listModules

# 출력 예시:
📦 Cross SDK Android Modules:
========================================
BOM          | io.crosstoken:android-bom     | 1.0.2
FOUNDATION   | io.crosstoken:foundation      | 1.0.0
CORE         | io.crosstoken:android-core    | 1.0.0
SIGN         | io.crosstoken:sign            | 1.0.0
NOTIFY       | io.crosstoken:notify          | 1.0.0
APPKIT       | io.crosstoken:appkit          | 1.0.1
MODAL_CORE   | io.crosstoken:modal-core      | 1.0.0
```

---

## 🔧 환경 설정

### GitHub Secrets

배포를 위해 다음 시크릿들이 설정되어야 합니다:

#### Nexus & 서명
- `NEXUS_USERNAME` - Cross Nexus 사용자명
- `NEXUS_PASSWORD` - Cross Nexus 패스워드
- `SIGNING_KEY` - GPG 개인키 (base64 인코딩)
- `SIGNING_PASSWORD` - GPG 키 패스프레이즈

#### SonarQube
- `SONAR_TOKEN` - SonarQube 액세스 토큰

#### SDK 설정
- `CROSS_PROJECT_ID` - Cross 프로젝트 ID

#### Android 키스토어 (샘플용)
- `KEYSTORE_BASE64` - Base64 인코딩된 키스토어 파일
- `CROSS_STORE_PASSWORD_*` - 키스토어 패스워드들
- `CROSS_KEY_PASSWORD_*` - 키 패스워드들

### GitHub Variables

- `CROSS_FILENAME_*` - 키스토어 파일 경로들
- `CROSS_KEYSTORE_ALIAS*` - 키스토어 별칭들
- `CROSS_PROJECT_ID` - Cross 프로젝트 ID

### 로컬 환경 설정

```bash
# Nexus 자격증명 설정
export NEXUS_USERNAME=your-username
export NEXUS_PASSWORD=your-password

# 서명 설정 (선택사항)
export SIGNING_KEY=your-signing-key
export SIGNING_PASSWORD=your-signing-password

# Android SDK 경로 (local.properties에서 설정됨)
export ANDROID_HOME=~/Library/Android/sdk
```

---

## 📈 실제 시나리오

### 시나리오 1: 버그 수정 (AppKit만)

```bash
# 1. 버그 수정 후 커밋
git add product/appkit/
git commit -m "fix: resolve crash in AppKit modal"
git push origin main

# 2. 자동 배포 실행됨
# - SonarQube 검사
# - 변경사항 감지: appkit 모듈
# - Snapshot 배포
# - 태그: release/android-v1.0.1-patch.202501161430
```

### 시나리오 2: 새 기능 추가 (BOM 업데이트)

```bash
# 1. 새 기능 개발 완료
git add .
git commit -m "feat: add new authentication method"

# 2. 버전 범프
./gradlew versionBump -Ptype=release

# 3. 수동 Release 배포
# GitHub Actions → Deploy SDK (Improved)
# - deploy_type: release
# - version_bump_type: none (이미 범프됨)
# - 태그: release/android-v1.1.0
```

### 시나리오 3: CI/CD 개선 (빌드 변경만)

```bash
# 1. GitHub Actions 워크플로우 수정
git add .github/workflows/
git commit -m "ci: improve deployment workflow"
git push origin main

# 2. 자동 배포 실행됨
# - 코드 변경 없음 감지
# - 태그: release/android-v1.0.2-build.a1b2c3d
```

---

## 🔍 트러블슈팅

### 일반적인 문제들

#### 1. 배포 실패

**문제**: GitHub Actions에서 배포가 실패함

**해결책**:
```bash
# 1. 로컬에서 빌드 테스트
./gradlew build

# 2. Dry Run으로 테스트
# GitHub Actions → Manual Deploy → dry_run: true

# 3. 로그 확인
# Actions 탭에서 실패한 단계의 로그 확인
```

#### 2. 버전 충돌

**문제**: 같은 버전이 이미 배포됨

**해결책**:
```bash
# 1. 현재 버전 확인
./gradlew listModules

# 2. 버전 범프
./gradlew fixBump -Pmodules=APPKIT

# 3. 재배포
```

#### 3. SonarQube 실패

**문제**: 코드 품질 검사 실패

**해결책**:
```bash
# 1. 로컬에서 코드 품질 확인
./gradlew check

# 2. SonarQube 규칙 확인
# sonar-project.properties 파일 검토

# 3. 코드 수정 후 재시도
```

#### 4. 태그 생성 실패

**문제**: Git 태그 생성이 실패함

**해결책**:
```bash
# 1. 기존 태그 확인
git tag -l "release/android-v*"

# 2. 중복 태그 삭제 (필요시)
git tag -d release/android-v1.0.0
git push origin :refs/tags/release/android-v1.0.0

# 3. 수동 태그 생성
./gradlew generateSmartTag -Pcreate=true
```

### 디버깅 도구

#### 변경사항 분석
```bash
# 마지막 릴리즈 이후 변경사항 확인
./gradlew analyzeChanges

# 특정 모듈의 변경사항 확인
git diff HEAD~10 -- product/appkit/
```

#### 버전 히스토리 확인
```bash
# 모든 릴리즈 태그 확인
git tag -l "release/android-v*" | sort -V

# 특정 태그의 정보 확인
git show release/android-v1.0.0
```

#### 배포 상태 확인
```bash
# Nexus 리포지토리에서 배포된 버전 확인
curl -u $NEXUS_USERNAME:$NEXUS_PASSWORD \
  "https://package.cross-nexus.com/repository/cross-sdk-android/io/crosstoken/android-bom/"
```

---

## 📚 추가 리소스

- [README.md](README.md) - 프로젝트 개요 및 사용법
- [SDK-Documentation.md](SDK-Documentation.md) - 상세 API 문서
- [.github/workflows/](/.github/workflows/) - GitHub Actions 워크플로우
- [scripts/](scripts/) - 배포 및 버저닝 스크립트

---

## 🤝 기여하기

배포 시스템 개선에 기여하고 싶으시다면:

1. 이슈 생성 또는 기존 이슈 확인
2. 개선사항 구현
3. 테스트 (Dry Run 사용)
4. Pull Request 생성

---

**📞 문의사항이 있으시면 개발팀에 연락해주세요!**
