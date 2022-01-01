@file:Suppress("PropertyName")

import net.kyori.blossom.BlossomExtension
import net.minecraftforge.gradle.userdev.UserDevExtension

val mod_version: String by project
val mc_version: String by project
val mc_version_range_supported: String by project
val forge_version: String by project
val forge_version_range_supported: String by project
val mappings_channel: String by project
val mappings_version: String by project

plugins {
    id("net.kyori.blossom")
    id("net.minecraftforge.gradle")
}

version = mod_version
group = "com.breakinblocks.plonk"
base.archivesName.set("plonk-${mc_version}")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

configure<UserDevExtension> {
    mappings(mappings_channel, mappings_version)
    runs {
        create("client") {
            workingDirectory(file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
            args("--username", "Dev")
            mods {
                create("plonk") {
                    sources = listOf(sourceSets["main"])
                }
            }
        }
        create("server") {
            workingDirectory(file("run"))
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            property("forge.logging.console.level", "debug")
            mods {
                create("plonk") {
                    sources = listOf(sourceSets["main"])
                }
            }
        }
    }
}

dependencies {
    add("minecraft", "net.minecraftforge:forge:${mc_version}-${forge_version}")
}

configure<BlossomExtension> {
    replaceToken("version = \"\"", "version = \"${mod_version}\"")
    replaceToken("dependencies = \"\"", "dependencies = \"required-after:forge@${forge_version_range_supported};\"")
    replaceToken("acceptedMinecraftVersions = \"\"", "acceptedMinecraftVersions = \"${mc_version_range_supported}\"")
    replaceTokenIn("/Plonk.java")
}

tasks.named<ProcessResources>("processResources") {
    inputs.property("mod_version", mod_version)
    inputs.property("mc_version", mc_version)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets["main"].resources.srcDirs) {
        include("mcmod.info")
        expand(
            "mod_version" to mod_version,
            "mc_version" to mc_version
        )
    }
    from(sourceSets["main"].resources.srcDirs) {
        exclude("mcmod.info")
    }
}
