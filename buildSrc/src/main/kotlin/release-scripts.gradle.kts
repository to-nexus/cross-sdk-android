import org.apache.tools.ant.taskdefs.condition.Os
import java.util.Locale
import kotlin.reflect.full.safeCast

// Example ./gradlew releaseAllSDKs -Ptype=local
// Example ./gradlew releaseAllSDKs -Ptype=sonatype
// Example ./gradlew releaseAllSDKs -Ptype=crossnexus
tasks.register("releaseAllSDKs") {
    doLast {
        project.findProperty("type")
            ?.run(String::class::safeCast)
            ?.run {
                println("Converting parameter to an supported ReleaseType value")
                ReleaseType.valueOf(this.uppercase(Locale.getDefault()))
            }?.let { releaseType ->
                generateListOfModuleTasks(releaseType).forEach { task ->
                    println("Executing Task: $task")
                    exec {
                        val gradleCommand = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                            "gradlew.bat"
                        } else {
                            "./gradlew"
                        }
                        commandLine(gradleCommand, task.path)
                    }
                }
            } ?: throw Exception("Missing Type parameter")
    }
}

// task for Cross Nexus
tasks.register("releaseAllSDKsToCrossNexus") {
    group = "publishing"
    description = "Release all SDKs to Cross Nexus repositories (both release and snapshot)"
    doLast {
        generateListOfModuleTasks(ReleaseType.CROSSNEXUS_RELEASE).forEach { task ->
            println("Executing Release Task: $task")
            exec {
                val gradleCommand = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                    "gradlew.bat"
                } else {
                    "./gradlew"
                }
                commandLine(gradleCommand, task.path)
            }
        }
        
        generateListOfModuleTasks(ReleaseType.CROSSNEXUS_SNAPSHOT).forEach { task ->
            println("Executing Snapshot Task: $task")
            exec {
                val gradleCommand = if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                    "gradlew.bat"
                } else {
                    "./gradlew"
                }
                commandLine(gradleCommand, task.path)
            }
        }
    }
}

fun generateListOfModuleTasks(type: ReleaseType): List<Task> = compileListOfSDKs().extractListOfPublishingTasks(type)

// Triple consists of the root module name, the child module name, and if it's a JVM or Android module
fun compileListOfSDKs(): List<Triple<String, String?, String>> = mutableListOf(
    Triple("foundation", null, "jvm"),
    Triple("core", "android", "android"),
    Triple("core", "modal", "android"),
    Triple("protocol", "sign", "android"),
    Triple("protocol", "notify", "android"),
    Triple("product", "appkit", "android")
).apply {
    // The BOM has to be last artifact
    add(Triple("core", "bom", "jvm"))
}

// This extension function will determine which task to run based on the type passed
fun List<Triple<String, String?, String>>.extractListOfPublishingTasks(type: ReleaseType): List<Task> = map { (parentModule, childModule, env) ->
    val task = when {
        env == "jvm" && type == ReleaseType.LOCAL -> "${publishJvmRoot}MavenLocal"
        env == "jvm" && type == ReleaseType.SONATYPE -> "${publishJvmRoot}SonatypeRepository"
        env == "jvm" && type == ReleaseType.CROSSNEXUS_RELEASE -> "${publishJvmRoot}CrossNexusReleaseRepository"
        env == "jvm" && type == ReleaseType.CROSSNEXUS_SNAPSHOT -> "${publishJvmRoot}CrossNexusSnapshotRepository"
        env == "android" && type == ReleaseType.LOCAL -> "${publishAndroidRoot}MavenLocal"
        env == "android" && type == ReleaseType.SONATYPE -> "${publishAndroidRoot}SonatypeRepository"
        env == "android" && type == ReleaseType.CROSSNEXUS_RELEASE -> "${publishAndroidRoot}CrossNexusReleaseRepository"
        env == "android" && type == ReleaseType.CROSSNEXUS_SNAPSHOT -> "${publishAndroidRoot}CrossNexusSnapshotRepository"
        else -> throw Exception("Unknown Type or Env")
    }

    val module = if (childModule != null) {
        subprojects.first { it.name == parentModule }.subprojects.first { it.name == childModule }
    } else {
        subprojects.first { it.name == parentModule }
    }

    module.tasks.getByName(task)
}

private val publishJvmRoot = "publishMavenJvmPublicationTo"
private val publishAndroidRoot = "publishReleasePublicationTo"

enum class ReleaseType {
    LOCAL, SONATYPE, CROSSNEXUS_RELEASE, CROSSNEXUS_SNAPSHOT
}