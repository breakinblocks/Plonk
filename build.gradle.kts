buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven {
            name = "forge"
            url = "http://files.minecraftforge.net/maven"
        }
        maven {
            name = "sonatype"
            url = "https://oss.sonatype.org/content/repositories/snapshots/"
        }
    }
    dependencies {
        classpath 'net.minecraftforge.gradle:ForgeGradle:1.2-SNAPSHOT'
    }
}

apply plugin: 'forge'

version = "${mc_version}-${mod_version}"
group = "com.breakinblocks.plonk" // http://maven.apache.org/guides/mini/guide-naming-conventions.html
archivesBaseName = "plonk"

sourceCompatibility = targetCompatibility = '1.8' // Need this here so eclipse task generates correctly.
compileJava {
    sourceCompatibility = targetCompatibility = '1.8'
}

minecraft {
    version = "${mc_version}-${forge_version}-${mc_version}"
    runDir = "run"
    
    replace "@VERSION@", project.version
    replaceIn "Plonk.java"
}

dependencies {

}

processResources {
    // this will ensure that this task is redone when the versions change.
    inputs.property "version", project.version
    inputs.property "mcversion", project.minecraft.version

    // replace stuff in mcmod.info, nothing else
    from(sourceSets.main.resources.srcDirs) {
        include 'mcmod.info'
                
        // replace version and mcversion
        expand 'version':project.version, 'mcversion':project.minecraft.version
    }
        
    // copy everything else, thats not the mcmod.info
    from(sourceSets.main.resources.srcDirs) {
        exclude 'mcmod.info'
    }
}

runClient {
    args '--username', 'Dev'
}
