@file:Suppress("PropertyName")

import net.minecraftforge.gradle.userdev.UserDevExtension

val mod_version: String by project
val minecraft_version: String by project
val minecraft_version_range_supported: String by project
val forge_loader_version_range_supported: String by project
val forge_version: String by project
val forge_version_range_supported: String by project
val mappings_channel: String by project
val mappings_version: String by project

plugins {
    id("net.kyori.blossom")
    id("net.minecraftforge.gradle")
    id("org.parchmentmc.librarian.forgegradle")
}

version = mod_version
group = "com.breakinblocks.plonk"
base.archivesName.set("plonk-${minecraft_version}")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

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
                property("forge_loader_version_range_supported", forge_loader_version_range_supported)
                property("forge_version_range_supported", forge_version_range_supported)
            }
        }
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
    add("minecraft", "net.minecraftforge:forge:${minecraft_version}-${forge_version}")
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
