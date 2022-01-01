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
            }
        }
    }
    repositories {
        maven {
            name = "MinecraftForge"
            url = uri("https://maven.minecraftforge.net")
            content {
                includeGroup("net.minecraft")
                includeGroup("net.minecraftforge")
                includeGroup("net.minecraftforge.gradle")
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
