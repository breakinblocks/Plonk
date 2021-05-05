@file:Suppress("PropertyName")

import net.minecraftforge.gradle.userdev.UserDevExtension

val mod_version: String by project
val mc_version: String by project
val mc_version_range_supported: String by project
val loader_version_range_supported: String by project
val forge_version: String by project
val forge_version_range_supported: String by project
val mappings_channel: String by project
val mappings_version: String by project

plugins {
    id("net.minecraftforge.gradle")
}

version = mod_version
group = "com.breakinblocks.plonk"
base.archivesBaseName = "plonk-${mc_version}"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val sourceNames = listOf("main", "generated")

sourceSets {
    val generated = create("generated")
    main {
        compileClasspath += generated.output
        runtimeClasspath += generated.output
    }
}

configure<UserDevExtension> {
    mappings(mappings_channel, mappings_version)
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))
    val combinedSources = sourceNames.map { sourceSets[it] }
    runs {
        create("client") {
            workingDirectory(file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
            mods {
                create("plonk") {
                    sources = combinedSources
                }
            }
        }
        create("server") {
            workingDirectory(file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
            mods {
                create("plonk") {
                    sources = combinedSources
                }
            }
        }
        create("data") {
            workingDirectory(file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
            args(
                "--mod", "plonk", "--all",
                "--existing", file("src/main/resources/"),
                "--output", file("src/generated/resources/")
            )
            mods {
                create("plonk") {
                    sources = combinedSources
                }
            }
        }
    }
}

dependencies {
    add("minecraft", "net.minecraftforge:forge:${mc_version}-${forge_version}")
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("mod_version", mod_version)
    inputs.property("mc_version_range_supported", mc_version_range_supported)
    inputs.property("loader_version_range_supported", loader_version_range_supported)
    inputs.property("forge_version_range_supported", forge_version_range_supported)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets["main"].resources.srcDirs) {
        include("META-INF/mods.toml")
        expand(
            "mod_version" to mod_version,
            "mc_version_range_supported" to mc_version_range_supported,
            "loader_version_range_supported" to loader_version_range_supported,
            "forge_version_range_supported" to forge_version_range_supported
        )
    }
    from(sourceSets["main"].resources.srcDirs) {
        exclude("META-INF/mods.toml")
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Specification-Title" to "Plonk",
            "Specification-Vendor" to "Breakin' Blocks",
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Breakin' Blocks"
        )
    }
    val combinedSources = sourceNames.map { sourceSets[it] }
    from(combinedSources.map { it.output }.toTypedArray())
}
