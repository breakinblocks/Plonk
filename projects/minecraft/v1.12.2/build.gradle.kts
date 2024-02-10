@file:Suppress("PropertyName")

val minecraft_version: String by project

subprojects {
    apply(plugin = "java")

    configure<BasePluginExtension> {
        archivesName.set("plonk-${minecraft_version}")
    }

    configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        withSourcesJar()
    }

    afterEvaluate {
        tasks.withType<Jar> {
            if (!archiveClassifier.isPresent || archiveClassifier.get().isEmpty()) {
                archiveClassifier.set(this@subprojects.name)
            } else {
                archiveClassifier.set(this@subprojects.name + "-" + archiveClassifier.get())
            }
        }
    }
}
