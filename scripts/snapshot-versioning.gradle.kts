import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 스냅샷 버저닝 시스템
 * - 버전 범프 없이 push된 경우 자동으로 스냅샷 버전 생성
 * - 고유한 버전으로 추적 가능성 보장
 */

tasks.register("generateSnapshotVersion") {
    group = "versioning"
    description = "Generate unique snapshot version for unchanged modules"
    
    doLast {
        val versionsFile = File(rootProject.projectDir, "buildSrc/src/main/kotlin/Versions.kt")
        if (!versionsFile.exists()) {
            throw GradleException("Versions.kt not found")
        }
        
        // 현재 버전들 읽기
        val versions = mutableMapOf<String, String>()
        val versionLines = mutableListOf<String>()
        
        versionsFile.readLines().forEach { line ->
            if (line.contains("const val") && line.contains("_VERSION")) {
                val key = line.substringBefore("_VERSION").substringAfter("const val ").trim()
                val value = line.substringAfter("\"").substringBefore("\"")
                versions[key] = value
                versionLines.add(line)
            }
        }
        
        // 변경된 모듈 확인 (Git diff 기반)
        val changedModules = getChangedModules()
        
        if (changedModules.isEmpty()) {
            println("📝 No module changes detected - skipping snapshot versioning")
            return@doLast
        }
        
        println("🔄 Changed modules detected: ${changedModules.joinToString(", ")}")
        
        // 스냅샷 버전 생성
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
        val commitHash = getCommitHash()
        
        var updated = false
        val newContent = versionsFile.readText()
        var updatedContent = newContent
        
        changedModules.forEach { module ->
            val versionKey = when (module) {
                "foundation" -> "FOUNDATION_VERSION"
                "android-core", "core" -> "CORE_VERSION"
                "sign" -> "SIGN_VERSION"
                "notify" -> "NOTIFY_VERSION"
                "appkit" -> "APPKIT_VERSION"
                "modal-core" -> "MODAL_CORE_VERSION"
                else -> null
            }
            
            if (versionKey != null && versions.containsKey(versionKey.replace("_VERSION", ""))) {
                val currentVersion = versions[versionKey.replace("_VERSION", "")]!!
                val snapshotVersion = "${currentVersion}-SNAPSHOT.${timestamp}.${commitHash}"
                
                println("📦 $module: $currentVersion → $snapshotVersion")
                
                // 버전 교체
                val oldLine = "const val $versionKey = \"$currentVersion\""
                val newLine = "const val $versionKey = \"$snapshotVersion\""
                updatedContent = updatedContent.replace(oldLine, newLine)
                updated = true
            }
        }
        
        if (updated) {
            versionsFile.writeText(updatedContent)
            println("✅ Snapshot versions generated successfully")
            
            // 변경사항 요약
            println("\n📋 Updated Versions:")
            versionsFile.readLines().forEach { line ->
                if (line.contains("const val") && line.contains("_VERSION") && line.contains("SNAPSHOT")) {
                    println("  $line")
                }
            }
        } else {
            println("⏭️ No versions updated")
        }
    }
}

tasks.register("revertSnapshotVersions") {
    group = "versioning"
    description = "Revert snapshot versions back to original versions"
    
    doLast {
        val versionsFile = File(rootProject.projectDir, "buildSrc/src/main/kotlin/Versions.kt")
        if (!versionsFile.exists()) {
            throw GradleException("Versions.kt not found")
        }
        
        var content = versionsFile.readText()
        var reverted = false
        
        // SNAPSHOT 버전을 원래 버전으로 되돌리기
        val snapshotPattern = Regex("""(const val \w+_VERSION = ")([^"]+)-SNAPSHOT\.[^"]+"""")
        content = snapshotPattern.replace(content) { matchResult ->
            reverted = true
            val prefix = matchResult.groupValues[1]
            val originalVersion = matchResult.groupValues[2]
            println("🔄 Reverting: ${matchResult.value} → ${prefix}${originalVersion}\"")
            "${prefix}${originalVersion}\""
        }
        
        if (reverted) {
            versionsFile.writeText(content)
            println("✅ Snapshot versions reverted successfully")
        } else {
            println("📝 No snapshot versions found to revert")
        }
    }
}

fun getChangedModules(): List<String> {
    return try {
        // 마지막 태그 또는 HEAD~1과 비교
        val lastTag = "git describe --tags --abbrev=0".execute().ifEmpty { "HEAD~1" }
        val changedFiles = "git diff --name-only $lastTag..HEAD".execute()
        
        val modules = mutableSetOf<String>()
        changedFiles.lines().forEach { file ->
            when {
                file.startsWith("foundation/") -> modules.add("foundation")
                file.startsWith("core/android/") -> modules.add("android-core")
                file.startsWith("core/modal/") -> modules.add("modal-core")
                file.startsWith("protocol/sign/") -> modules.add("sign")
                file.startsWith("protocol/notify/") -> modules.add("notify")
                file.startsWith("product/appkit/") -> modules.add("appkit")
            }
        }
        
        modules.toList()
    } catch (e: Exception) {
        println("⚠️ Could not detect changed modules: ${e.message}")
        emptyList()
    }
}

fun getCommitHash(): String {
    return try {
        "git rev-parse --short HEAD".execute()
    } catch (e: Exception) {
        "unknown"
    }
}

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
