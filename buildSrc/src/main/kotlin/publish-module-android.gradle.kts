import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.api.DefaultAndroidSourceDirectorySet

plugins {
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}

tasks {
    register("javadocJar", Jar::class) {
        dependsOn(named("dokkaHtml"))
        archiveClassifier.set("javadoc")
        from("${layout.buildDirectory}/dokka/html")
    }

    register("sourcesJar", Jar::class) {
        archiveClassifier.set("sources")
        from(
            (project.extensions.getByType<BaseExtension>().sourceSets.getByName("main").kotlin.srcDirs("kotlin") as DefaultAndroidSourceDirectorySet).srcDirs,
            (project.extensions.getByType<BaseExtension>().sourceSets.getByName("release").kotlin.srcDirs("kotlin") as DefaultAndroidSourceDirectorySet).srcDirs
        )
    }
}

(project as ExtensionAware).extensions.configure<LibraryExtension>("android") {
    publishing.singleVariant("release")
}

afterEvaluate {
    // 환경변수로 리포지토리 URL 설정 (GitHub Actions에서 제공)
    val releaseRepoUrl = System.getenv("NEXUS_RELEASE_URL") ?: "https://package.cross-nexus.com/repository/cross-sdk-android/"
    val snapshotRepoUrl = System.getenv("NEXUS_SNAPSHOT_URL") ?: "https://package.cross-nexus.com/repository/cross-sdk-android-snap/"
    
    println("📦 Release repository: $releaseRepoUrl")
    println("📦 Snapshot repository: $snapshotRepoUrl")
    
    publishing {
        repositories {
            maven {
                name = "CrossNexusRelease"
                url = uri(releaseRepoUrl)
                credentials {
                    username = System.getenv("NEXUS_USERNAME") ?: project.findProperty("nexusUsername") as String?
                    password = System.getenv("NEXUS_PASSWORD") ?: project.findProperty("nexusPassword") as String?
                }
            }
            
            maven {
                name = "CrossNexusSnapshot"
                url = uri(snapshotRepoUrl)
                credentials {
                    username = System.getenv("NEXUS_USERNAME") ?: project.findProperty("nexusUsername") as String?
                    password = System.getenv("NEXUS_PASSWORD") ?: project.findProperty("nexusPassword") as String?
                }
            }
        }
        
        publications {
            register<MavenPublication>("mavenAndroid") {
                groupId = "io.crosstoken"
                artifactId = requireNotNull(extra.get(KEY_PUBLISH_ARTIFACT_ID)).toString()
                version = requireNotNull(extra.get(KEY_PUBLISH_VERSION)).toString()
                
                from(components["release"])
                
                artifact(tasks.getByName("sourcesJar"))
                artifact(tasks.getByName("javadocJar"))

                pom {
                    name.set("Cross ${requireNotNull(extra.get(KEY_SDK_NAME))}")
                    description.set("${requireNotNull(extra.get(KEY_SDK_NAME))} SDK for Cross")
                    url.set("https://github.com/to-nexus/cross-sdk-android")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                        license {
                            name.set("SQLCipher Community Edition")
                            url.set("https://www.zetetic.net/sqlcipher/license/")
                        }
                    }

                    developers {
                        developer {
                            id.set("NexusCrossDev")
                            name.set("Nexus Cross Dev")
                            email.set("dev@to.nexus")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/to-nexus/cross-sdk-android.git")
                        developerConnection.set("scm:git:ssh://github.com/to-nexus/cross-sdk-android.git")
                        url.set("https://github.com/to-nexus/cross-sdk-android")
                    }
                }
            }
        }
    }
    
    val deployType = project.findProperty("type") as String? ?: "both"
    
    tasks.withType<PublishToMavenRepository>().configureEach {
        onlyIf {
            when (deployType) {
                "release" -> repository.name == "CrossNexusRelease"
                "snap" -> repository.name == "CrossNexusSnapshot"
                "both" -> true
                else -> true
            }
        }
    }
}

signing {
    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")
    
    if (!signingKey.isNullOrBlank() && !signingPassword.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    } else {
        println("⚠️ Signing skipped: SIGNING_KEY or SIGNING_PASSWORD is missing or empty")
    }
}