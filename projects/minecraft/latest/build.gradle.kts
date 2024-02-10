@file:Suppress("PropertyName")

val minecraft_version: String by project

subprojects {
    apply(plugin = "java")

    configure<BasePluginExtension> {
        archivesName.set("plonk-${minecraft_version}")
    }

    tasks.named<Jar>("jar") {
        archiveClassifier.set(this@subprojects.name)
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()
    }
}
