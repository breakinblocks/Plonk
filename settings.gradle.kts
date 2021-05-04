pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.toString()) {
                "net.kyori.blossom" -> {
                    useModule("net.kyori:blossom:1.2.0")
                }
                "forge" -> {
                    useModule("com.anatawa12.forge:ForgeGradle:1.2-1.0.6")
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
                includeGroup("net.minecraftforge")
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
