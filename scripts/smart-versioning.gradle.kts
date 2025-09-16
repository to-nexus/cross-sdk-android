import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 스마트 버저닝 시스템
 * - BOM 변경 여부에 따른 적응적 버전 관리
 * - 개별 모듈 변경사항 추적
 * - 의미있는 릴리즈 태그 생성
 */

tasks.register("analyzeChanges") {
    group = "versioning"
    description = "Analyze changes since last release and suggest versioning strategy"
    
    doLast {
        val versionsFile = File(rootProject.projectDir, "buildSrc/src/main/kotlin/Versions.kt")
        if (!versionsFile.exists()) {
            throw GradleException("Versions.kt not found")
        }
        
        // 현재 버전들 읽기
        val versions = mutableMapOf<String, String>()
        versionsFile.readLines().forEach { line ->
            if (line.contains("const val") && line.contains("_VERSION")) {
                val key = line.substringBefore("_VERSION").substringAfter("const val ").trim()
                val value = line.substringAfter("\"").substringBefore("\"")
                versions[key] = value
            }
        }
        
        println("📊 Current Module Versions:")
        println("=".repeat(50))
        versions.forEach { (key, value) ->
            val artifactId = when (key) {
                "BOM" -> "android-bom"
                "FOUNDATION" -> "foundation"
                "CORE" -> "android-core"
                "SIGN" -> "sign"
                "NOTIFY" -> "notify"
                "APPKIT" -> "appkit"
                "MODAL_CORE" -> "modal-core"
                else -> key.lowercase()
            }
            println("${key.padEnd(12)} | ${artifactId.padEnd(15)} | $value")
        }
        
        // Git 태그 분석
        try {
            val lastReleaseTag = "git tag -l release/android-v* | grep -v -E '(alpha|beta|rc|patch|build)' | sort -V | tail -n1".execute()
            
            if (lastReleaseTag.isNotEmpty()) {
                println("\n🏷️ Last Release Tag: $lastReleaseTag")
                
                // 변경된 파일들 분석
                val changedFiles = "git diff --name-only $lastReleaseTag..HEAD -- foundation/ core/ protocol/ product/ buildSrc/src/main/kotlin/Versions.kt".execute()
                
                if (changedFiles.isNotEmpty()) {
                    println("\n📝 Changed Files Since Last Release:")
                    changedFiles.lines().forEach { file ->
                        if (file.isNotBlank()) {
                            println("  - $file")
                        }
                    }
                    
                    // 모듈별 변경사항 분석
                    val changedModules = mutableSetOf<String>()
                    changedFiles.lines().forEach { file ->
                        when {
                            file.startsWith("foundation/") -> changedModules.add("foundation")
                            file.startsWith("core/android/") -> changedModules.add("android-core")
                            file.startsWith("core/modal/") -> changedModules.add("modal-core")
                            file.startsWith("protocol/sign/") -> changedModules.add("sign")
                            file.startsWith("protocol/notify/") -> changedModules.add("notify")
                            file.startsWith("product/appkit/") -> changedModules.add("appkit")
                            file.contains("Versions.kt") -> changedModules.add("versions")
                        }
                    }
                    
                    println("\n🔄 Changed Modules:")
                    if (changedModules.isEmpty()) {
                        println("  - No module changes detected (build/CI changes only)")
                    } else {
                        changedModules.forEach { module ->
                            println("  - $module")
                        }
                    }
                    
                    // BOM 변경 확인
                    if (changedModules.contains("versions")) {
                        val oldBom = "git show $lastReleaseTag:buildSrc/src/main/kotlin/Versions.kt | grep 'const val BOM_VERSION' | sed 's/.*\"\\(.*\\)\".*/\\1/'".execute()
                        val currentBom = versions["BOM"] ?: ""
                        
                        if (oldBom != currentBom) {
                            println("\n✅ BOM Version Changed: $oldBom → $currentBom")
                            println("   Recommended: Full release with BOM version tag")
                        } else {
                            println("\n⏭️ BOM Version Unchanged: $currentBom")
                            println("   Recommended: Module patch release")
                        }
                    }
                    
                    // 권장 태그 전략
                    println("\n🎯 Recommended Tagging Strategy:")
                    when {
                        changedModules.contains("versions") -> {
                            val bomChanged = "git show $lastReleaseTag:buildSrc/src/main/kotlin/Versions.kt | grep 'const val BOM_VERSION' | sed 's/.*\"\\(.*\\)\".*/\\1/'".execute() != versions["BOM"]
                            if (bomChanged) {
                                println("  📦 BOM Release: release/android-v${versions["BOM"]}")
                            } else {
                                val highestVersion = versions.values.maxByOrNull { it } ?: "1.0.0"
                                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                                println("  🔄 Module Patch: release/android-v${highestVersion}-patch.${timestamp}")
                            }
                        }
                        changedModules.isNotEmpty() -> {
                            val highestVersion = versions.values.maxByOrNull { it } ?: "1.0.0"
                            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                            println("  🔄 Module Update: release/android-v${highestVersion}-patch.${timestamp}")
                        }
                        else -> {
                            val commitHash = "git rev-parse --short HEAD".execute()
                            println("  🏗️ Build Release: release/android-v${versions["BOM"]}-build.${commitHash}")
                        }
                    }
                } else {
                    println("\n✨ No changes since last release")
                }
            } else {
                println("\n🆕 No previous release tags found - this will be the initial release")
                println("   Recommended: release/android-v${versions["BOM"]}")
            }
        } catch (e: Exception) {
            println("\n⚠️ Could not analyze git history: ${e.message}")
        }
    }
}

tasks.register("suggestVersionBump") {
    group = "versioning"
    description = "Suggest version bump strategy based on changes"
    
    doLast {
        try {
            val lastReleaseTag = "git tag -l release/android-v* | grep -v -E '(alpha|beta|rc|patch|build)' | sort -V | tail -n1".execute()
            
            if (lastReleaseTag.isEmpty()) {
                println("🆕 Initial release - no version bump needed")
                return@doLast
            }
            
            // 커밋 메시지 분석으로 변경 타입 감지
            val commitMessages = "git log $lastReleaseTag..HEAD --oneline".execute()
            
            var hasMajor = false
            var hasMinor = false
            var hasPatch = false
            
            commitMessages.lines().forEach { line ->
                when {
                    line.contains("BREAKING CHANGE") || line.contains("!:") -> hasMajor = true
                    line.startsWith("feat") -> hasMinor = true
                    line.startsWith("fix") || line.startsWith("perf") -> hasPatch = true
                }
            }
            
            println("📈 Suggested Version Bump Strategy:")
            when {
                hasMajor -> {
                    println("  🚨 MAJOR: Breaking changes detected")
                    println("     Command: ./gradlew versionBump -Ptype=major")
                }
                hasMinor -> {
                    println("  ✨ MINOR: New features detected")
                    println("     Command: ./gradlew versionBump -Ptype=release")
                }
                hasPatch -> {
                    println("  🐛 PATCH: Bug fixes detected")
                    println("     Command: ./gradlew versionBump -Ptype=fix")
                }
                else -> {
                    println("  📝 BUILD: Documentation/build changes only")
                    println("     Command: No version bump needed")
                }
            }
            
        } catch (e: Exception) {
            println("⚠️ Could not analyze commits: ${e.message}")
        }
    }
}

tasks.register("generateSmartTag") {
    group = "versioning"
    description = "Generate smart release tag based on changes"
    
    doLast {
        val strategy = project.findProperty("strategy") as String? ?: "auto"
        val versionsFile = File(rootProject.projectDir, "buildSrc/src/main/kotlin/Versions.kt")
        
        // 현재 BOM 버전 읽기
        val bomVersion = versionsFile.readLines()
            .find { it.contains("const val BOM_VERSION") }
            ?.substringAfter("\"")?.substringBefore("\"") ?: "1.0.0"
        
        val tag = when (strategy) {
            "bom" -> "release/android-v$bomVersion"
            "patch" -> {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                "release/android-v$bomVersion-patch.$timestamp"
            }
            "build" -> {
                val commitHash = "git rev-parse --short HEAD".execute()
                "release/android-v$bomVersion-build.$commitHash"
            }
            else -> {
                // Auto strategy - analyze changes
                try {
                    val lastTag = "git tag -l release/android-v* | sort -V | tail -n1".execute()
                    if (lastTag.isEmpty()) {
                        "release/android-v$bomVersion"
                    } else {
                        val changedFiles = "git diff --name-only $lastTag..HEAD -- buildSrc/src/main/kotlin/Versions.kt".execute()
                        if (changedFiles.isNotEmpty()) {
                            val oldBom = "git show $lastTag:buildSrc/src/main/kotlin/Versions.kt | grep 'const val BOM_VERSION' | sed 's/.*\"\\(.*\\)\".*/\\1/'".execute()
                            if (oldBom != bomVersion) {
                                "release/android-v$bomVersion"
                            } else {
                                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                                "release/android-v$bomVersion-patch.$timestamp"
                            }
                        } else {
                            val commitHash = "git rev-parse --short HEAD".execute()
                            "release/android-v$bomVersion-build.$commitHash"
                        }
                    }
                } catch (e: Exception) {
                    "release/android-v$bomVersion"
                }
            }
        }
        
        println("🏷️ Generated Tag: $tag")
        
        // 태그 생성 (실제로는 하지 않고 출력만)
        val createTag = project.findProperty("create") as String? == "true"
        if (createTag) {
            "git tag -a $tag -m 'Auto-generated release tag'".execute()
            "git push origin $tag".execute()
            println("✅ Tag created and pushed: $tag")
        } else {
            println("💡 To create this tag, run: ./gradlew generateSmartTag -Pcreate=true")
        }
    }
}

// Helper function to execute shell commands
fun String.execute(): String {
    return try {
        val process = ProcessBuilder(*this.split(" ").toTypedArray())
            .directory(File("."))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        
        process.waitFor()
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        ""
    }
}
