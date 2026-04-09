@file:Suppress("PropertyName")

val mod_version: String by project

allprojects {
    group = "com.breakinblocks.plonk"
    version = mod_version

    repositories {
        maven {
            url = uri("https://api.modrinth.com/maven")
            content {
                includeGroupAndSubgroups("maven.modrinth")
            }
        }
    }
}
