pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.toString()) {
                "net.kyori.blossom" -> {
                    useModule("net.kyori:blossom:2.1.0")
                }

                "net.minecraftforge.gradle" -> {
                    // https://files.minecraftforge.net/net/minecraftforge/gradle/ForgeGradle/
                    useModule("net.minecraftforge.gradle:ForgeGradle:6.0.14")
                }

                "forge" -> {
                    // https://github.com/anatawa12/ForgeGradle-1.2
                    useModule("com.anatawa12.forge:ForgeGradle:1.2-1.1.1")
                }

                "org.parchmentmc.librarian.forgegradle" -> {
                    useModule("org.parchmentmc.librarian.forgegradle:org.parchmentmc.librarian.forgegradle.gradle.plugin:1.2.0")
                }
            }
        }
    }
    repositories {
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net")
            content {
                includeGroup("de.oceanlabs.mcp")
                includeGroup("net.minecraft")
                includeGroup("net.minecraftforge")
                includeGroup("net.minecraftforge.gradle")
            }
        }
        maven {
            url = uri("https://maven.parchmentmc.org")
            content {
                includeGroup("org.parchmentmc")
                includeGroup("org.parchmentmc.feather")
                includeGroup("org.parchmentmc.librarian.forgegradle")
            }
        }
        gradlePluginPortal {
            content {
                includeGroup("net.kyori")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "plonk"

include("plonk-forge")
include("plonk-forge-1.18.2")
include("plonk-forge-1.16.5")
include("plonk-forge-1.15.2")
include("plonk-forge-1.14.4")
include("plonk-forge-1.12.2")
include("plonk-forge-1.7.10")
