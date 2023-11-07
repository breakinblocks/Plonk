@file:Suppress("PropertyName")

import com.google.common.collect.ImmutableMap
import net.minecraftforge.gradle.delayed.DelayedFile
import net.minecraftforge.gradle.tasks.CreateStartTask
import net.minecraftforge.gradle.tasks.user.SourceCopyTask
import net.minecraftforge.gradle.user.UserExtension
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.nio.file.Files

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

// Workaround for the source and target compatibility being set by something...
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
configure<JavaPluginExtension> {
    sourceCompatibility = null
    targetCompatibility = null
}

tasks.withType<JavaCompile> {
    val languageVersionString = java.toolchain.languageVersion.get().toString()
    sourceCompatibility = languageVersionString
    targetCompatibility = languageVersionString
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", mod_version)
                property("dependencies", "required-after:Forge@${forge_version_range_supported};")
                property("acceptedMinecraftVersions", mc_version_range_supported)
            }
        }
    }
}

configure<UserExtension> {
    version = "${mc_version}-${forge_version}-${mc_version}"
    runDir = "run"
}

dependencies {

}

// Use Blossom instead of FG source replacement
tasks.filterIsInstance(SourceCopyTask::class.java).forEach { it.enabled = false }

// Undo modifications the main java compile task (by ForgeGradle) that redirected the sources to the copy task output.
// This has the benefit of errors linking to the actual source files!
tasks.named<JavaCompile>(sourceSets.main.get().compileJavaTaskName) {
    this.source = sourceSets.main.get().java
}

tasks.processResources {
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

tasks.runClient {
    args("--username", "Dev")
}

// Make the CreateStartTask compile using java 8, not sure why it's not defaulting to the project one...
// Since replacing tasks is deprecated, disabling and using a finalizer task is the best we can do.
tasks.filterIsInstance(CreateStartTask::class.java).forEach {
    it.enabled = false
    val makeStartFixed = tasks.create(it.name + "Fixed", CreateStartFixedTask::class.java)
    makeStartFixed.createStartTask = it
    it.finalizedBy(makeStartFixed)
}

abstract class CreateStartFixedTask : DefaultTask() {
    companion object {
        lateinit var startOutField: Field
        lateinit var compileField: Field
        lateinit var classpathField: Field
        lateinit var resolveStringMethod: Method

        init {
            val baseClass: Class<CreateStartTask> = CreateStartTask::class.java

            for (field in baseClass.declaredFields) {
                when (field.name) {
                    "startOut" -> startOutField = makeAccessible(field)
                    "compile" -> compileField = makeAccessible(field)
                    "classpath" -> classpathField = makeAccessible(field)
                }
            }

            for (method in baseClass.declaredMethods) {
                when (method.name) {
                    "resolveString" -> resolveStringMethod = makeAccessible(method)
                }
            }
        }

        private fun makeAccessible(field: Field): Field {
            field.isAccessible = true
            return field
        }

        private fun makeAccessible(method: Method): Method {
            method.isAccessible = true
            return method
        }
    }

    @Internal
    var createStartTask: CreateStartTask? = null

    val resources: HashMap<String, String>
        @Input
        get() {
            return createStartTask!!.resources
        }

    val startOut: DelayedFile
        @OutputDirectory
        get() {
            return startOutField.get(createStartTask) as DelayedFile
        }

    @TaskAction
    @Throws(IOException::class)
    fun doStuff() {
        // resolve the replacements
        for ((key, value) in createStartTask!!.replacements) {
            createStartTask!!.replacements[key] = resolveStringMethod.invoke(createStartTask, value) as String
        }

        // set the output of the files
        val resourceDir =
            if (compileField.getBoolean(createStartTask)) File(
                temporaryDir,
                "extracted"
            ) else createStartTask!!.startOut

        // replace and extract
        for (resEntry in createStartTask!!.resources.entries) {
            var out = resEntry.value
            for ((key, value) in createStartTask!!.replacements) {
                out = out.replace(key!!, (value as String))
            }

            // write file
            val outFile = File(resourceDir, resEntry.key)
            outFile.parentFile.mkdirs()
            Files.write(outFile.toPath(), out.toByteArray(StandardCharsets.UTF_8))
        }

        // now compile, if im compiling.
        if (compileField.getBoolean(createStartTask)) {
            val compiled = createStartTask!!.startOut
            compiled.mkdirs()
            this.ant.invokeMethod(
                "javac", ImmutableMap.builder<Any, Any>()
                    .put("srcDir", resourceDir.canonicalPath)
                    .put("destDir", compiled.canonicalPath)
                    .put("failonerror", true)
                    .put("includeantruntime", false)
                    .put(
                        "classpath",
                        project.configurations.getByName(classpathField.get(createStartTask) as String).asPath
                    )
                    .put("encoding", StandardCharsets.UTF_8)
                    // The only real change is to make these 1.8 instead of 1.6.
                    .put("source", "1.8")
                    .put("target", "1.8")
                    .build()
            )
        }
    }
}
