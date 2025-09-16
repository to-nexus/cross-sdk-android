import com.android.build.gradle.BaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("release-scripts")
    id("version-bump")
}

// Changeset 스크립트 적용 (수동 워크플로우용)
apply(from = "scripts/auto-changeset.gradle.kts")

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

// deploy to Cross Nexus
tasks.register("deploy") {
    group = "publishing"
    description = "Deploy to Cross Nexus Release repository only"
    doFirst {
        println("🚀 Deploying to release repository only.")
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
        println("🚀 Deploying to snapshot repository only.")
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
        println("🚀 Deploying to both release and snapshot repositories.")
    }
    dependsOn("deploy", "deploySnap")
}

// deploy tasks per environment
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