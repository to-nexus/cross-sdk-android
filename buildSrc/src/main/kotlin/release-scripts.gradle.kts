import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.GradleException
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.execution.TaskExecutionGraphListener
import java.util.Locale
import kotlin.reflect.full.safeCast

// Example ./gradlew releaseAllSDKs -Ptype=local
// Example ./gradlew releaseAllSDKs -Ptype=sonatype
// Example ./gradlew releaseAllSDKs -Ptype=crossnexus
val releaseAllSDKs = tasks.register("releaseAllSDKs") {
    group = "publishing"
    description = "Release all SDKs by wiring publish tasks (use -Ptype=local|sonatype|crossnexus_release|crossnexus_snapshot)"

    doFirst {
        // Keep this around for parity with the previous behavior / user feedback.
        val gradleCommand = if (Os.isFamily(Os.FAMILY_WINDOWS)) "gradlew.bat" else "./gradlew"
        println("Note: Gradle 9+ no longer supports nested Gradle invocation via Project.exec; this task now runs publish tasks within the same build.")
        println("If you used to run: $gradleCommand releaseAllSDKs -Ptype=local")
        println("It will still work, but without spawning a nested Gradle process.")
    }
}

// task for Cross Nexus
val releaseAllSDKsToCrossNexus = tasks.register("releaseAllSDKsToCrossNexus") {
    group = "publishing"
    description = "Release all SDKs to Cross Nexus repositories (both release and snapshot)"
}

gradle.taskGraph.addTaskExecutionGraphListener(object : TaskExecutionGraphListener {
    override fun graphPopulated(graph: TaskExecutionGraph) {
        if (graph.hasTask(releaseAllSDKs.get())) {
        val releaseType = project.findProperty("type")
            ?.run(String::class::safeCast)
            ?.run {
                println("Converting parameter to a supported ReleaseType value")
                ReleaseType.valueOf(this.uppercase(Locale.getDefault()))
            } ?: throw GradleException("Missing Type parameter. Example: ./gradlew releaseAllSDKs -Ptype=local")

            releaseAllSDKs.get().dependsOn(generateListOfModuleTasks(releaseType))
        }

        if (graph.hasTask(releaseAllSDKsToCrossNexus.get())) {
            releaseAllSDKsToCrossNexus.get().dependsOn(
                generateListOfModuleTasks(ReleaseType.CROSSNEXUS_RELEASE) +
                    generateListOfModuleTasks(ReleaseType.CROSSNEXUS_SNAPSHOT)
            )
        }
    }
})

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