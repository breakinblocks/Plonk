@file:Suppress("PropertyName")

val mod_id: String by project
val mod_version: String by project
val minecraft_version: String by project
val minecraft_version_range_supported: String by project
val neo_version: String by project
val neo_version_range_supported: String by project
val mappings_version: String by project
val mappings_minecraft_version: String by project
val modrinth_jei_version: String by project
val modrinth_jade_version: String by project

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
                property("mod_id", mod_id)
                property("mod_version", mod_version)
                property("minecraft_version_range_supported", minecraft_version_range_supported)
                property("neo_version_range_supported", neo_version_range_supported)
            }
        }
    }
}

neoForge {
    version = neo_version
    validateAccessTransformers = true

    parchment {
        mappingsVersion = mappings_version
        minecraftVersion = mappings_minecraft_version
    }

    runs {
        create("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }
        create("server") {
            server()
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }
        create("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }
        create("data") {
            clientData()
            programArguments.addAll(
                "--mod",
                mod_id,
                "--all",
                "--output",
                file("src/generated/resources/").getAbsolutePath(),
                "--existing",
                file("src/main/resources/").getAbsolutePath()
            )
        }
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        create("plonk") {
            sourceNames.forEach { name -> sourceSet(sourceSets[name]) }
        }
    }
}

dependencies {
    implementation("maven.modrinth:jei:${modrinth_jei_version}")
    implementation("maven.modrinth:jade:${modrinth_jade_version}")
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
