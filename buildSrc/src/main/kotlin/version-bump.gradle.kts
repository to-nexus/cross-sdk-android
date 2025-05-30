tasks {
    val properties = project.properties as Map<String, Any>

    // Example usage:
    // ./gradlew versionBump -Ptype=fix
    // ./gradlew versionBump -Ptype=release
    register("versionBump", Exec::class) {
        val scriptFilePath = CHECK_MODULES_SCRIPT_PATH
        val outputFilePath = CHECK_MODULES_OUTPUT_PATH
        val bumpType = getBumpType(properties)
        if (bumpType == VersionBumpType.MANUAL) throw Throwable("Unsupported bump type: $bumpType. Please use manualBump task instead.")

        // Run modules changes check script
        commandLine("sh", scriptFilePath, outputFilePath)

        doLast {
            val propertiesWithChangedModules = parseChangedModules(properties)

            when (bumpType) {
                VersionBumpType.FIX -> writeFiles(bumpVersions(propertiesWithChangedModules, VersionBumpType.FIX, InputType.AUTOMATIC))
                VersionBumpType.RELEASE -> writeFiles(bumpVersions(propertiesWithChangedModules, VersionBumpType.RELEASE, InputType.AUTOMATIC))
                else -> {}
            }
        }
    }

    // Example usage:
    // ./gradlew manualBump -PBOM=1.0.0 -PFOUNDATION=1.0.0 -PCORE=1.0.0 -PSIGN=1.0.0 -PNOTIFY=1.0.0 -PAPPKIT=1.0.0 -PMODAL_CORE=1.0.0
    // ./gradlew manualBump -PNOTIFY=2.0.0
    register("manualBump") {
        doLast {
            writeFiles(bumpVersions(properties, VersionBumpType.MANUAL, InputType.MANUAL))
        }
    }

    // Example usage:
    // ./gradlew releaseBump -Pmodules=FOUNDATION,CORE,SIGN,NOTIFY,APPKIT,MODAL_CORE
    // ./gradlew releaseBump -Pmodules=FOUNDATION
    // ./gradlew releaseBump -Pmodules=APPKIT
    register("releaseBump") {
        doLast {
            writeFiles(bumpVersions(properties, VersionBumpType.RELEASE, InputType.AUTOMATIC))
        }
    }

    // Example usage:
    // ./gradlew fixBump -Pmodules=FOUNDATION,CORE,SIGN,NOTIFY,APPKIT,MODAL_CORE
    // ./gradlew fixBump -Pmodules=FOUNDATION
    // ./gradlew fixBump -Pmodules=APPKIT
    register("fixBump") {
        doLast {
            writeFiles(bumpVersions(properties, VersionBumpType.FIX, InputType.AUTOMATIC))
        }
    }
}
