@file:Suppress("PropertyName")

import net.kyori.blossom.BlossomExtension
import net.minecraftforge.gradle.tasks.user.SourceCopyTask
import net.minecraftforge.gradle.user.UserExtension

val mod_version: String by project
val mc_version: String by project
val mc_version_range_supported: String by project
val forge_version: String by project
val forge_version_range_supported: String by project

plugins {
    id("net.kyori.blossom")
    id("forge")
}

version = mod_version
group = "com.breakinblocks.plonk"
base.archivesName.set("plonk-${mc_version}")

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

configure<UserExtension> {
    version = "${mc_version}-${forge_version}-${mc_version}"
    runDir = "run"
}

dependencies {

}

// Use Blossom instead of FG source replacement
tasks.filterIsInstance(SourceCopyTask::class.java).forEach { it.enabled = false }

configure<BlossomExtension> {
    replaceToken("version = \"\"", "version = \"${mod_version}\"")
    replaceToken("dependencies = \"\"", "dependencies = \"required-after:Forge@${forge_version_range_supported};\"")
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

tasks.named<JavaExec>("runClient") {
    args("--username", "Dev")
}
