@file:Suppress("PropertyName")

val mod_version: String by project
val minecraft_version: String by project
val minecraft_version_range_supported: String by project
val neo_loader_version_range_supported: String by project
val neo_version: String by project
val neo_version_range_supported: String by project
val mappings_channel: String by project
val mappings_version: String by project

plugins {
    id("net.kyori.blossom")
    id("net.neoforged.moddev")
}

val sourceNames = listOf("main", "generated")

sourceSets {
    val generated = create("generated")
    main {
        compileClasspath += generated.output
        runtimeClasspath += generated.output
        blossom {
            resources {
                property("mod_version", mod_version)
                property("minecraft_version_range_supported", minecraft_version_range_supported)
                property("neo_loader_version_range_supported", neo_loader_version_range_supported)
                property("neo_version_range_supported", neo_version_range_supported)
            }
        }
    }
}

neoForge {
    version = neo_version
    validateAccessTransformers = true

    runs {
        create("client") {
            client()
        }
        create("data") {
            data()
        }
        create("server") {
            server()
        }
    }

    mods {
        create("plonk") {
            sourceNames.forEach { name -> sourceSet(sourceSets[name])}
        }
    }
}

dependencies {
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Specification-Title" to "Plonk",
            "Specification-Vendor" to "Breakin' Blocks",
            "Specification-Version" to "1",
            "Implementation-Title" to "Plonk",
            "Implementation-Version" to mod_version,
            "Implementation-Vendor" to "Breakin' Blocks"
        )
    }
    val combinedSources = sourceNames.map { sourceSets[it] }
    from(combinedSources.map { it.output }.toTypedArray())
}
