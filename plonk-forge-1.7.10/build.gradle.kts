@file:Suppress("PropertyName")

import com.google.common.collect.ImmutableMap
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

sourceSets {
    main {
        blossom {
            javaSources {
//                replaceToken("version = \"\"", "version = \"${mod_version}\"")
//                replaceToken("dependencies = \"\"", "dependencies = \"required-after:Forge@${forge_version_range_supported};\"")
//                replaceToken("acceptedMinecraftVersions = \"\"", "acceptedMinecraftVersions = \"${mc_version_range_supported}\"")
//                replaceTokenIn("/Plonk.java")
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
// Since replacing tasks is deprecated, disabling and copying over the values to a finalizer task is the best we can do.
tasks.filterIsInstance(CreateStartTask::class.java).forEach {
    if (it is CreateStartFixedTask) {
        return@forEach
    }
    it.enabled = false
    val makeStartFixed = tasks.create(it.name + "Fixed", CreateStartFixedTask::class.java)
    it.finalizedBy(makeStartFixed)
    for ((resource, outName) in it.resources) {
        makeStartFixed.addResource(resource, outName)
    }
    for ((token, replacement) in it.replacements) {
        makeStartFixed.addReplacement(token, replacement)
    }
    makeStartFixed.setStartOut(it.startOut) // Ahhhhh the getter resolves the delayed file...
    // We're gonna need a wrapper instead.
}

abstract class CreateStartFixedTask : CreateStartTask() {
    companion object {
        lateinit var compileField: Field
        lateinit var classpathField: Field
        lateinit var resolveStringMethod: Method

        init {
            val baseClass: Class<CreateStartTask> = CreateStartTask::class.java

            for (field in baseClass.declaredFields) {
                when (field.name) {
                    "compile" -> compileField = field
                    "classpath" -> classpathField = field
                }
            }

            for (method in baseClass.declaredMethods) {
                when (method.name) {
                    "resolveString" -> resolveStringMethod = method
                }
            }
        }
    }

    @TaskAction
    @Throws(IOException::class)
    override fun doStuff() {
        // resolve the replacements
        for ((key, value) in replacements) {
            replacements[key] = resolveStringMethod.invoke(this, value) as String
        }

        // set the output of the files
        val resourceDir = if (compileField.getBoolean(this)) File(temporaryDir, "extracted") else startOut

        // replace and extract
        for (resEntry in resources.entries) {
            var out = resEntry.value
            for ((key, value) in replacements) {
                out = out.replace(key!!, (value as String))
            }

            // write file
            val outFile = File(resourceDir, resEntry.key)
            outFile.parentFile.mkdirs()
            Files.write(outFile.toPath(), out.toByteArray(StandardCharsets.UTF_8))
        }

        // now compile, if im compiling.
        if (compileField.getBoolean(this)) {
            val compiled = startOut
            compiled.mkdirs()
            this.ant.invokeMethod(
                "javac", ImmutableMap.builder<Any, Any>()
                    .put("srcDir", resourceDir.canonicalPath)
                    .put("destDir", compiled.canonicalPath)
                    .put("failonerror", true)
                    .put("includeantruntime", false)
                    .put("classpath", project.configurations.getByName(classpathField.get(this) as String).asPath)
                    .put("encoding", StandardCharsets.UTF_8)
                    // The only real change is to make these 1.8 instead of 1.6.
                    .put("source", "1.8")
                    .put("target", "1.8")
                    .build()
            )
        }
    }
}
