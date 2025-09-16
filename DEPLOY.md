# Cross SDK Android 배포 가이드

## 📋 개요

Cross SDK Android는 **수동 버전 관리**와 **조건부 자동 배포** 시스템을 사용합니다.

### 🎯 핵심 원칙
- **자동 버전 범프 없음** - 모든 버전 변경은 수동으로 관리
- **Versions.kt 변경 시에만 배포** - 의도하지 않은 배포 방지
- **배포 실패 시 롤백** - 안전한 배포 보장

## 🚀 배포 방법

### 1️⃣ 일반적인 개발 워크플로우 (권장)

#### **단계 1: 코드 개발**
```bash
# 예: foundation 모듈 수정
vim foundation/src/main/kotlin/io/crosstoken/foundation/util/UtilFunctions.kt
```

#### **단계 2: 버전 수동 범프**
```kotlin
// buildSrc/src/main/kotlin/Versions.kt 수정
const val FOUNDATION_VERSION = "1.0.1"  // 1.0.0 → 1.0.1
```

#### **단계 3: 커밋 및 푸시**
```bash
git add .
git commit -m "feat: add new utility function in foundation"
git push origin main
```

#### **단계 4: 자동 배포**
- ✅ **Versions.kt가 변경됨** → 자동 배포 실행
- 🚀 **Main 브랜치** → Release + Snapshot 배포
- 🌿 **CI 브랜치** → Snapshot만 배포

### 2️⃣ 수동 워크플로우 배포

GitHub Actions에서 **"Deploy SDK (Improved)"** 워크플로우를 수동 실행:

#### **배포 옵션**
- **배포 타입**: `release`, `snapshot`, `both`
- **버전 범프**: `none`, `fix`, `release`, `manual`
- **수동 버전**: `FOUNDATION=1.0.1,CORE=1.0.2`
- **대상 모듈**: `foundation,android-core` (선택사항)

## 📦 모듈 구조

### 🏗️ 모듈 매핑
| 디렉토리 | 모듈명 | Gradle 상수 |
|---------|--------|-------------|
| `foundation/` | foundation | FOUNDATION |
| `core/android/` | android-core | CORE |
| `core/bom/` | android-bom | BOM |
| `core/modal/` | modal-core | MODAL_CORE |
| `protocol/sign/` | sign | SIGN |
| `protocol/notify/` | notify | NOTIFY |
| `product/appkit/` | appkit | APPKIT |

### 🔄 동적 모듈 감지
- **새 모듈 추가 시** `settings.gradle.kts`에서 자동 감지
- **워크플로우 수정 불필요**
- **Versions.kt에 상수만 추가**하면 자동 매핑

## 🎯 배포 조건

### ✅ 배포되는 경우
1. **Main/CI 브랜치에 push**
2. **AND Versions.kt 파일이 변경됨**
3. **AND 모듈 코드도 변경됨**

### ❌ 배포 안되는 경우
1. **Versions.kt 변경 없이** 코드만 수정
2. **문서(.md) 파일만** 변경
3. **GitHub Actions 설정만** 변경

### 📋 예시 시나리오

#### **시나리오 1: 정상 배포**
```bash
# 변경된 파일들
- foundation/src/main/kotlin/UtilFunctions.kt  ✅
- buildSrc/src/main/kotlin/Versions.kt         ✅
→ 결과: 배포 실행 ✅
```

#### **시나리오 2: 배포 건너뛰기**
```bash
# 변경된 파일들  
- foundation/src/main/kotlin/UtilFunctions.kt  ✅
- README.md                                    ✅
→ 결과: 배포 건너뛰기 ⏭️ (Versions.kt 변경 없음)
```

## 🌿 브랜치별 배포 전략

### 📍 Main 브랜치
- **배포 타입**: `both` (Release + Snapshot)
- **리포지토리**: 
  - Release: `https://package.cross-nexus.com/repository/cross-sdk-android/`
  - Snapshot: `https://package.cross-nexus.com/repository/cross-sdk-android-snap/`

### 📍 CI 브랜치 (`ci/*`)
- **배포 타입**: `snapshot`
- **리포지토리**:
  - Release: `https://package.cross-nexus.com/repository/dev-cross-sdk-android/`
  - Snapshot: `https://package.cross-nexus.com/repository/dev-cross-sdk-android-snap/`

## 🔧 버전 관리 방법

### 1️⃣ 수동 편집 (권장)
```kotlin
// buildSrc/src/main/kotlin/Versions.kt
const val FOUNDATION_VERSION = "1.0.1"  // 직접 수정
```

### 2️⃣ Gradle 태스크 사용
```bash
# 개별 모듈 패치 범프
./gradlew fixBump -Pmodules=FOUNDATION

# 개별 모듈 마이너 범프  
./gradlew releaseBump -Pmodules=FOUNDATION

# 수동 버전 지정
./gradlew manualBump -PFOUNDATION=1.0.1
```

## 🛡️ 안전장치

### 🔄 배포 실패 시 롤백
1. **버전 준비** (커밋하지 않음)
2. **배포 실행**
3. **배포 성공** → 버전 커밋 ✅
4. **배포 실패** → 버전 롤백 ✅

### 🔍 변경사항 감지
- **마지막 릴리즈 태그와 비교**
- **모듈별 세분화된 감지**
- **빌드 설정 변경 별도 추적**

## 📊 배포 로그 해석

### ✅ 성공적인 배포
```bash
🔍 Checking if Versions.kt was changed in this push...
✅ Versions.kt was changed - proceeding with deployment
🚀 Main branch - will deploy to both release and snapshot
📋 Version bump already completed (Versions.kt changed)
⏭️ Push event - skipping version bump (Versions.kt already changed)
🚀 Deploying to Cross Nexus (both)...
✅ Deployment successful! Committing version changes...
```

### ⏭️ 배포 건너뛰기
```bash
🔍 Checking if Versions.kt was changed in this push...
⏭️ Versions.kt was not changed - skipping deployment
💡 Only code changes detected, no version bump needed
```

### ❌ 배포 실패
```bash
🔄 Re-applying version changes for deployment...
🚀 Deploying to Cross Nexus (both)...
❌ Deployment failed - version changes NOT committed
🔄 Repository stays clean (no version pollution)
```

## 🎯 모범 사례

### ✅ 권장사항
1. **작은 단위로 자주 배포**
2. **의미있는 커밋 메시지** 작성
3. **버전 변경과 코드 변경을 함께** 커밋
4. **CI 브랜치에서 먼저 테스트** 후 main 머지

### ❌ 피해야 할 것들
1. **Versions.kt만 단독으로** 변경
2. **여러 모듈을 한 번에** 대량 범프
3. **배포 실패 시 강제 푸시**
4. **수동 워크플로우 남용**

## 🔧 문제 해결

### Q: 배포가 실행되지 않아요
**A**: Versions.kt 파일이 변경되었는지 확인하세요.
```bash
git log --oneline -1 --name-only
# buildSrc/src/main/kotlin/Versions.kt가 포함되어야 함
```

### Q: 특정 모듈만 배포하고 싶어요
**A**: 수동 워크플로우를 사용하세요.
- 버전 범프: `fix` 또는 `release`
- 대상 모듈: `foundation,android-core`

### Q: CI 브랜치에서 프로덕션으로 배포되었어요
**A**: 브랜치명이 `ci/`로 시작하는지 확인하세요.
```bash
git branch --show-current
# ci/feature-name 형태여야 함
```

### Q: 배포 실패 후 버전이 롤백되지 않았어요
**A**: 수동으로 Versions.kt를 되돌리세요.
```bash
git checkout HEAD~1 -- buildSrc/src/main/kotlin/Versions.kt
git commit -m "revert: rollback version after failed deployment"
```

## 📚 추가 정보

### 🔗 관련 파일
- **워크플로우**: `.github/workflows/deploy.yml`
- **버전 파일**: `buildSrc/src/main/kotlin/Versions.kt`
- **수동 배포**: `.github/workflows/manual-deploy.yml`

### 🏷️ Git 태그 전략
- **릴리즈 태그**: `release/android-v{version}`
- **스마트 태깅**: BOM/모듈/빌드 변경에 따른 적응형 태그
- **자동 GitHub Release** 생성

### 📦 Maven 의존성 예시
```kotlin
// BOM 사용 (권장)
implementation(platform("io.crosstoken:android-bom:1.0.3"))
implementation("io.crosstoken:android-core")
implementation("io.crosstoken:foundation")

// 개별 모듈
implementation("io.crosstoken:foundation:1.0.1")
implementation("io.crosstoken:android-core:1.0.1")
```

---

## 🆘 지원

문제가 발생하면 다음을 확인하세요:
1. **GitHub Actions 로그** 상세 분석
2. **Versions.kt 변경사항** 확인  
3. **브랜치명과 파일 경로** 검증
4. **수동 워크플로우** 대안 사용

**Happy Deploying! 🚀**