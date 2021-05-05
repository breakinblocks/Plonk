pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.toString()) {
                "net.minecraftforge.gradle" -> {
                    // TODO: Change when merged and released: https://github.com/MinecraftForge/ForgeGradle/pull/763
                    //useModule("net.minecraftforge.gradle:ForgeGradle:4.1.7")
                    useModule("tk.sciwhiz12.gradle:ForgeGradle:4.1.9")
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
            name = "sciwhiz12"
            url = uri("https://sciwhiz12.tk/maven")
            content {
                includeGroup("tk.sciwhiz12.gradle")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "plonk"
