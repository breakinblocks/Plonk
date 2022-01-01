pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.toString()) {
                "net.kyori.blossom" -> {
                    useModule("net.kyori:blossom:1.3.0")
                }
                "net.minecraftforge.gradle" -> {
                    useModule("net.minecraftforge.gradle:ForgeGradle:5.1.27")
                }
                "forge" -> {
                    useModule("com.anatawa12.forge:ForgeGradle:1.2-1.0.7")
                }
                "org.parchmentmc.librarian.forgegradle" -> {
                    useModule("org.parchmentmc.librarian.forgegradle:org.parchmentmc.librarian.forgegradle.gradle.plugin:1.1.4.0-dev-SNAPSHOT")
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

include("plonk-forge-1.16.5")
include("plonk-forge-1.15.2")
include("plonk-forge-1.14.4")
include("plonk-forge-1.12.2")
include("plonk-forge-1.7.10")
