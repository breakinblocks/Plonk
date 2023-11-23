@file:Suppress("PropertyName")

import net.minecraftforge.gradle.userdev.UserDevExtension

val mod_version: String by project
val minecraft_version: String by project
val minecraft_version_range_supported: String by project
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
base.archivesName.set("plonk-${minecraft_version}")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

sourceSets {
    main {
        blossom {
            javaSources {
                property("mod_version", mod_version)
                property("mod_dependencies", "required-after:Forge@${forge_version_range_supported};")
                property("mod_accepted_minecraft_versions", minecraft_version_range_supported)
            }
            resources {
                property("mod_version", mod_version)
                property("minecraft_version", minecraft_version)
            }
        }
    }
}

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
    add("minecraft", "net.minecraftforge:forge:${minecraft_version}-${forge_version}")
}
