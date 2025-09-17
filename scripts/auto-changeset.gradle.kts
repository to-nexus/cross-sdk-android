import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Android SDK용 자동 Changeset 생성 스크립트
 * 
 * 사용법:
 * ./gradlew autoChangeset -Pversion=1.2.0 -Ptype=release
 * ./gradlew autoChangeset -Pversion=1.1.1 -Ptype=fix
 */

tasks.register("autoChangeset") {
    group = "versioning"
    description = "Generate changeset for Android SDK modules"
    
    doLast {
        val version = project.findProperty("version") as String? 
            ?: throw GradleException("Version is required. Use -Pversion=X.Y.Z")
        val changeType = project.findProperty("type") as String? ?: "minor"
        
        // 모듈 목록 (Versions.kt에서 추출)
        val versionsFile = File(rootProject.projectDir, "buildSrc/src/main/kotlin/Versions.kt")
        if (!versionsFile.exists()) {
            throw GradleException("Versions.kt not found")
        }
        
        val modules = mutableListOf<String>()
        versionsFile.readLines().forEach { line ->
            if (line.contains("const val") && line.contains("_VERSION")) {
                val moduleName = line.substringBefore("_VERSION").substringAfter("const val ").trim()
                when (moduleName) {
                    "BOM" -> modules.add("android-bom")
                    "FOUNDATION" -> modules.add("foundation")
                    "CORE" -> modules.add("android-core")
                    "SIGN" -> modules.add("sign")
                    "NOTIFY" -> modules.add("notify")
                    "APPKIT" -> modules.add("appkit")
                    "MODAL_CORE" -> modules.add("modal-core")
                }
            }
        }
        
        if (modules.isEmpty()) {
            throw GradleException("No modules found in Versions.kt")
        }
        
        // Changeset 디렉토리 생성
        val changesetDir = File(rootProject.projectDir, ".changeset")
        changesetDir.mkdirs()
        
        // Changeset 파일 생성
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        val filename = "release-${version.replace(".", "-")}-${timestamp}.md"
        val changesetFile = File(changesetDir, filename)
        
        val content = buildString {
            appendLine("---")
            modules.forEach { module ->
                appendLine("\"io.crosstoken:${module}\": $changeType")
            }
            appendLine("---")
            appendLine()
            appendLine("# Cross SDK Android Release $version")
            appendLine()
            
            when (changeType) {
                "major" -> {
                    appendLine("## 🚀 Major Release")
                    appendLine("Breaking changes and new major features")
                }
                "minor" -> {
                    appendLine("## ✨ Minor Release") 
                    appendLine("New features and improvements")
                }
                "patch", "fix" -> {
                    appendLine("## 🐛 Patch Release")
                    appendLine("Bug fixes and minor improvements")
                }
            }
            
            appendLine()
            appendLine("### 📦 Updated Modules")
            modules.forEach { module ->
                appendLine("- `io.crosstoken:${module}`: $version")
            }
            
            appendLine()
            appendLine("### 🏪 Repository")
            appendLine("```kotlin")
            appendLine("repositories {")
            appendLine("    maven {")
            appendLine("        url = uri(\"https://package.cross-nexus.com/repository/cross-sdk-android/\")")
            appendLine("    }")
            appendLine("}")
            appendLine("```")
        }
        
        changesetFile.writeText(content)
        
        println("✅ Changeset created: $filename")
        println("📝 Version: $version")
        println("🔄 Change type: $changeType")
        println("📦 Modules: ${modules.joinToString(", ")}")
    }
}

tasks.register("listModules") {
    group = "versioning"
    description = "List all SDK modules and their current versions"
    
    doLast {
        val versionsFile = File(rootProject.projectDir, "buildSrc/src/main/kotlin/Versions.kt")
        if (!versionsFile.exists()) {
            throw GradleException("Versions.kt not found")
        }
        
        println("📦 Cross SDK Android Modules:")
        println("=".repeat(40))
        
        versionsFile.readLines().forEach { line ->
            if (line.contains("const val") && line.contains("_VERSION")) {
                val moduleName = line.substringBefore("_VERSION").substringAfter("const val ").trim()
                val version = line.substringAfter("\"").substringBefore("\"")
                
                val artifactId = when (moduleName) {
                    "BOM" -> "android-bom"
                    "FOUNDATION" -> "foundation"
                    "CORE" -> "android-core"
                    "SIGN" -> "sign"
                    "NOTIFY" -> "notify"
                    "APPKIT" -> "appkit"
                    "MODAL_CORE" -> "modal-core"
                    else -> moduleName.lowercase()
                }
                
                println("${moduleName.padEnd(12)} | io.crosstoken:${artifactId.padEnd(15)} | $version")
            }
        }
    }
}

tasks.register("generateReleaseNotes") {
    group = "versioning"
    description = "Generate release notes from changesets"
    
    doLast {
        val changesetDir = File(rootProject.projectDir, ".changeset")
        if (!changesetDir.exists()) {
            println("No changeset directory found")
            return@doLast
        }
        
        val changesetFiles = changesetDir.listFiles { file ->
            file.name.endsWith(".md") && file.name.startsWith("release-")
        }?.sortedByDescending { it.lastModified() }
        
        if (changesetFiles.isNullOrEmpty()) {
            println("No changeset files found")
            return@doLast
        }
        
        val releaseNotesFile = File(rootProject.projectDir, "RELEASE_NOTES.md")
        val content = buildString {
            appendLine("# Cross SDK Android Release Notes")
            appendLine()
            
            changesetFiles.forEach { file ->
                val fileContent = file.readText()
                val version = file.name.substringAfter("release-").substringBefore("-").replace("-", ".")
                
                appendLine("## Version $version")
                appendLine()
                
                // changeset 내용에서 실제 릴리즈 노트 부분만 추출
                val lines = fileContent.lines()
                var inContent = false
                lines.forEach { line ->
                    if (line == "---" && !inContent) {
                        inContent = true
                    } else if (line == "---" && inContent) {
                        inContent = false
                    } else if (!inContent && line.isNotBlank()) {
                        appendLine(line)
                    }
                }
                
                appendLine()
                appendLine("---")
                appendLine()
            }
        }
        
        releaseNotesFile.writeText(content)
        println("✅ Release notes generated: RELEASE_NOTES.md")
    }
}
