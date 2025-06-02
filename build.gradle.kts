import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("release-scripts")
    id("version-bump")
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = jvmVersion.toString()
        }
    }

    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "androidx.navigation" && requested.name == "navigation-compose") {
                useVersion(libs.versions.androidxNavigation.get())
            }
            if (requested.group == "org.bouncycastle" && requested.name == "bcprov-jdk15on") {
                useTarget(libs.bouncyCastle)
            }
        }
    }
}

subprojects {
    afterEvaluate {
        if (hasProperty("android")) {
            extensions.configure(BaseExtension::class.java) {
                packagingOptions {
                    with(resources.excludes) {
                        add("META-INF/INDEX.LIST")
                        add("META-INF/DEPENDENCIES")
                        add("META-INF/LICENSE.md")
                        add("META-INF/NOTICE.md")
                    }
                }

                dependencies {
                    add("testImplementation", libs.mockk)
                    add("testImplementation", libs.jUnit)
                    add("testRuntimeOnly", libs.jUnit.engine)
                }
            }
        }

        plugins.withId(rootProject.libs.plugins.javaLibrary.get().pluginId) {
            dependencies {
                add("testImplementation", libs.mockk)
                add("testImplementation", libs.jUnit)
                add("testRuntimeOnly", libs.jUnit.engine)
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

task<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

// Cross Nexus용 배포 작업들
tasks.register("deploy") {
    group = "publishing"
    description = "Deploy to Cross Nexus Release repository only"
    doFirst {
        println("🚀 Release 리포지토리에만 배포합니다...")
    }
    dependsOn(
        ":foundation:publishAllPublicationsToCrossNexusReleaseRepository",
        ":core:android:publishAllPublicationsToCrossNexusReleaseRepository",
        ":core:bom:publishAllPublicationsToCrossNexusReleaseRepository",
        ":core:modal:publishAllPublicationsToCrossNexusReleaseRepository",
        ":protocol:sign:publishAllPublicationsToCrossNexusReleaseRepository",
        ":protocol:notify:publishAllPublicationsToCrossNexusReleaseRepository",
        ":product:appkit:publishAllPublicationsToCrossNexusReleaseRepository"
    )
}

tasks.register("deploySnap") {
    group = "publishing"
    description = "Deploy to Cross Nexus Snapshot repository only"
    doFirst {
        println("🚀 Snapshot 리포지토리에만 배포합니다...")
    }
    dependsOn(
        ":foundation:publishAllPublicationsToCrossNexusSnapshotRepository",
        ":core:android:publishAllPublicationsToCrossNexusSnapshotRepository",
        ":core:bom:publishAllPublicationsToCrossNexusSnapshotRepository",
        ":core:modal:publishAllPublicationsToCrossNexusSnapshotRepository",
        ":protocol:sign:publishAllPublicationsToCrossNexusSnapshotRepository",
        ":protocol:notify:publishAllPublicationsToCrossNexusSnapshotRepository",
        ":product:appkit:publishAllPublicationsToCrossNexusSnapshotRepository"
    )
}

tasks.register("deployBoth") {
    group = "publishing"
    description = "Deploy to both Cross Nexus Release and Snapshot repositories"
    doFirst {
        println("🚀 Release와 Snapshot 리포지토리 모두에 배포합니다...")
    }
    dependsOn("deploy", "deploySnap")
}

// 환경별 배포 태스크들
tasks.register("deployDev") {
    group = "publishing"
    description = "Deploy to Dev environment with Cross Nexus"
    dependsOn("deployBoth")
}

tasks.register("deployStage") {
    group = "publishing"
    description = "Deploy to Stage environment with Cross Nexus"
    dependsOn("deployBoth")
}

tasks.register("deployProd") {
    group = "publishing"
    description = "Deploy to Production environment with Cross Nexus"
    dependsOn("deployBoth")
}