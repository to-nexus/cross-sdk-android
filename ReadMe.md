# **Cross SDK - Android**

This repository contains Kotlin implementation of Cross SDK protocols for Android applications.

## 🚀 Release & Deployment

### Prerequisites

- **Java 17** (required for building)
- **Android SDK** (for Android modules)

### Environment Variables

Set the following environment variables (used in GitHub Actions):

```bash
export NEXUS_USERNAME=your-nexus-username
export NEXUS_PASSWORD=your-nexus-password
export SIGNING_KEY=your-signing-key
export SIGNING_PASSWORD=your-signing-password
```

### 🏷️ Tag-based Deployment (Recommended)

Deploy using Git tags with the format:

**Release Deployment:**
```bash
# Create and push a release tag
git tag -a "sdk-v1.0.0" -m "Release SDK v1.0.0"
git push origin sdk-v1.0.0
```

**Snapshot Deployment:**
```bash
# Create and push a snapshot tag  
git tag -a "sdk-snap-v1.0.0" -m "Snapshot SDK v1.0.0"
git push origin sdk-snap-v1.0.0
```

**⚠️ Important: Version Management**
- **Tag version**: Version for release (eg: `sdk-v1.0.0`)
- **Module version**: Versions of each module are defined in `Versions.kt`.
- Each module has its own version. (eg: Foundation 1.0.0, Core 1.0.2, Sign 1.0.1)

This will automatically:
1. Use versions defined in `Versions.kt` for each module (not tag version)
2. Deploy to appropriate Cross Nexus repository (Release or Snapshot)
3. Create GitHub Release with actual module versions (Release deployments only)

### 🔄 Version Management Workflow

**Step 1: Update module versions**
```bash
# Update all modules to same version
./gradlew manualBump -PBOM=1.2.0 -PFOUNDATION=1.2.0 -PCORE=1.2.0 -PSIGN=1.2.0 -PNOTIFY=1.2.0 -PAPPKIT=1.2.0 -PMODAL_CORE=1.2.0

# Or update specific modules only
./gradlew fixBump -Pmodules=APPKIT
./gradlew releaseBump -Pmodules=CORE
```

**Step 2: Create and push tag**
```bash
git add buildSrc/src/main/kotlin/Versions.kt ReadMe.md
git commit -m "Bump versions for release"
git tag -a "sdk-v1.2.0" -m "Release SDK v1.2.0"
git push origin main
git push origin sdk-v1.2.0
```

**Step 3: Automated deployment**
- GitHub Actions will automatically deploy using versions from `Versions.kt`
- GitHub Release will show actual module versions, not tag version

### 🔧 Manual Deployment Options

#### 1. Simple Gradle Commands

```bash
# Deploy to Release repository only
./gradlew deploy -Ptype=release

# Deploy to Snapshot repository only  
./gradlew deploySnap -Ptype=snap

# Deploy to both repositories (default)
./gradlew deployBoth
```

#### 2. GitHub Actions Manual Trigger

Go to **Actions** → **Deploy SDK to Cross Nexus** → **Run workflow**
- Enter version (e.g., `1.0.0`)
- Select deployment type: `release` or `snapshot`

#### 3. Local Gradle Deployment with Environment Setup

```bash
# Set up environment
export ANDROID_HOME=~/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"

# Set Nexus credentials
export NEXUS_USERNAME=your-username
export NEXUS_PASSWORD=your-password

# Deploy (choose one)
./gradlew deploy -Ptype=release      # Release only
./gradlew deploySnap -Ptype=snap     # Snapshot only  
./gradlew deployBoth                 # Both repositories
```

### 📦 Repositories

- **Release**: `https://package.cross-nexus.com/repository/cross-sdk-android/`
- **Snapshot**: `https://package.cross-nexus.com/repository/cross-sdk-android-snap/`

## 📚 BOM Instructions

To help manage compatible dependencies stay in sync, we've introduced a [BOM](https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import)
to the Kotlin SDK. With this, you only need to
update the BOM version to get the latest SDKs. Just add the BOM as a dependency and then list the SDKs you want to include into your project.

### Repository Configuration

Add the Cross Nexus repository to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://package.cross-nexus.com/repository/cross-sdk-android/")
        credentials {
            username = "your-nexus-username"
            password = "your-nexus-password"
        }
    }
}
```

### Example build.gradle.kts

```kotlin
dependencies {
    implementation(platform("io.crosstoken:android-bom:{BOM version}"))
    implementation("io.crosstoken:android-core")
    implementation("io.crosstoken:appkit")
}
```

### Individual Modules

```kotlin
dependencies {
    implementation("io.crosstoken:foundation:{version}")
    implementation("io.crosstoken:android-core:{version}")
    implementation("io.crosstoken:sign:{version}")
    implementation("io.crosstoken:notify:{version}")
    implementation("io.crosstoken:appkit:{version}")
    implementation("io.crosstoken:modal-core:{version}")
}
```

## 📋 SDK Chart

| BOM   | [Core SDK](core/android) | [Sign SDK](protocol/sign) | [AppKit](product/appkit) |
|-------|--------------------------|---------------------------|--------------------------|
| 1.0.1 | 1.0.0                    | 1.0.0                     | 1.0.0                    |
| 1.0.0 | 1.0.0                    | 1.0.0                     | 1.0.0                    |

## 🛠️ Development

### Build Requirements

- **Java 17+**
- **Android SDK API 34**
- **Gradle 8.11.1+**

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd cross-sdk-android
   ```

2. **Set up Java 17 environment**
   ```bash
   # macOS (with Homebrew)
   brew install openjdk@17
   export JAVA_HOME=/opt/homebrew/opt/openjdk@17
   export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
   ```

3. **Install Android SDK**
   
   **Option 1: Android Studio (Recommended)**
   - Download and install [Android Studio](https://developer.android.com/studio)
   - Android SDK will be installed automatically
   
   **Option 2: Command Line Tools Only**
   ```bash
   # Download command line tools from:
   # https://developer.android.com/studio/command-line
   
   # Extract and set up SDK
   mkdir -p ~/Library/Android/sdk/cmdline-tools
   unzip commandlinetools-*.zip -d ~/Library/Android/sdk/cmdline-tools
   mv ~/Library/Android/sdk/cmdline-tools/cmdline-tools ~/Library/Android/sdk/cmdline-tools/latest
   
   # Set environment variables
   export ANDROID_HOME=~/Library/Android/sdk
   export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
   
   # Accept licenses and install required components
   sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   ```

4. **Configure Android SDK path in `local.properties`**
   ```properties
   # Update with your actual Android SDK path
   sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
   ```

5. **Set up signing credentials in `secrets.properties`**
   ```properties
   # Add your actual signing configuration
   CROSS_PROJECT_ID=your-project-id
   # ... other credentials
   ```

6. **Test the build**
   ```bash
   ./gradlew build
   ```

### Available Gradle Tasks

```bash
# Publishing tasks
./gradlew deploy -Ptype=release       # Deploy to Release repository only
./gradlew deploySnap -Ptype=snap      # Deploy to Snapshot repository only  
./gradlew deployBoth                  # Deploy to both repositories

# Legacy tasks (still available)
./gradlew deployDev                   # Deploy to dev environment
./gradlew deployStage                 # Deploy to staging environment
./gradlew deployProd                  # Deploy to production environment
```

### Module Build Order

Due to inter-module dependencies, modules are built in the following order:
1. `foundation` - Base foundation module
2. `core:android` - Core Android functionality
3. `core:modal` - Modal core components
4. `protocol:sign` - Sign protocol
5. `protocol:notify` - Notify protocol  
6. `product:appkit` - AppKit product
7. `core:bom` - Bill of Materials

### GitHub Actions Configuration

#### Secrets
Configure the following secrets in your GitHub repository (Settings → Secrets and variables → Actions → Secrets):

**Nexus & Signing:**
- `NEXUS_USERNAME` - Cross Nexus repository username
- `NEXUS_PASSWORD` - Cross Nexus repository password
- `SIGNING_KEY` - GPG private key (base64 encoded)
- `SIGNING_PASSWORD` - GPG key passphrase

**SDK Configuration:**
- `CROSS_PROJECT_ID` - Cross project ID for SDK configuration

**Android Keystore (for samples):**
- `SAMPLE_KEYSTORE_BASE64` - Base64 encoded sample.keystore file
- `CROSS_STORE_PASSWORD_UPLOAD` - Release keystore password
- `CROSS_KEY_PASSWORD_UPLOAD` - Release key password
- `CROSS_STORE_PASSWORD_INTERNAL` - Internal keystore password
- `CROSS_KEY_PASSWORD_INTERNAL` - Internal key password
- `CROSS_STORE_PASSWORD_DEBUG` - Debug keystore password
- `CROSS_KEY_PASSWORD_DEBUG` - Debug key password

#### Variables
Configure the following variables in your GitHub repository (Settings → Secrets and variables → Actions → Variables):

**Android Keystore Paths:**
- `CROSS_FILENAME_UPLOAD` - Release keystore file path (e.g., ./sample.keystore)
- `CROSS_FILENAME_INTERNAL` - Internal keystore file path (e.g., ./sample.keystore)
- `CROSS_FILENAME_DEBUG` - Debug keystore file path (e.g., ./sample.keystore)

**Android Keystore Aliases:**
- `CROSS_KEYSTORE_ALIAS` - Release keystore alias
- `CROSS_KEYSTORE_ALIAS_DEBUG` - Debug keystore alias

**SDK Configuration:**
- `CROSS_PROJECT_ID` - Cross project ID for SDK configuration

#### Keystore Setup
To encode your keystore file for GitHub Secrets:

```bash
# Encode keystore file to base64
base64 -i sample.keystore | pbcopy  # macOS
base64 sample.keystore | xclip -selection clipboard  # Linux

# Then paste the output as SAMPLE_KEYSTORE_BASE64 secret
```

## 📄 License

Cross SDK is released under the Apache 2.0 license. [See LICENSE](/LICENSE) for details.