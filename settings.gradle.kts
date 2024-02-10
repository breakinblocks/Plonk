pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.toString()) {
                "net.kyori.blossom" -> {
                    // https://github.com/KyoriPowered/blossom
                    useModule("net.kyori:blossom:2.1.0")
                }

                "net.minecraftforge.gradle" -> {
                    // https://files.minecraftforge.net/net/minecraftforge/gradle/ForgeGradle/
                    useModule("net.minecraftforge.gradle:ForgeGradle:6.0.16")
                }

                "forge" -> {
                    // https://github.com/anatawa12/ForgeGradle-1.2
                    useModule("com.anatawa12.forge:ForgeGradle:1.2-1.1.1")
                }

                "org.parchmentmc.librarian.forgegradle" -> {
                    // https://ldtteam.jfrog.io/ui/native/parchmentmc-public/org/parchmentmc/librarian/
                    useModule("org.parchmentmc:librarian:1.2.0")
                }
            }
        }
    }
    repositories {
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net")
            content {
                includeGroupAndSubgroups("de.oceanlabs.mcp")
                includeGroupAndSubgroups("net.minecraft")
                includeGroupAndSubgroups("net.minecraftforge")
            }
        }
        maven {
            url = uri("https://maven.parchmentmc.org")
            content {
                includeGroupAndSubgroups("org.parchmentmc")
            }
        }
        gradlePluginPortal {
            content {
                includeGroupAndSubgroups("net.kyori")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "plonk"

include("projects:minecraft:latest:forge")

include("projects:minecraft:v1.19.4:forge")

include("projects:minecraft:v1.19.2:forge")

include("projects:minecraft:v1.18.2:forge")

include("projects:minecraft:v1.16.5:forge")

include("projects:minecraft:v1.15.2:forge")

include("projects:minecraft:v1.14.4:forge")

include("projects:minecraft:v1.12.2:forge")

include("projects:minecraft:v1.7.10:forge")
