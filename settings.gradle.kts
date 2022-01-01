pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.toString()) {
                "net.minecraftforge.gradle" -> {
                    useModule("net.minecraftforge.gradle:ForgeGradle:5.1.27")
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
        mavenCentral()
    }
}

rootProject.name = "plonk"
