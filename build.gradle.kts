plugins {
    java
    id("xyz.wagyourtail.unimined") version "1.1.0-SNAPSHOT"
    `maven-publish`
}

group = "moe.nea"
version = "1.0.1"

repositories {
    mavenCentral()
}


java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}


unimined.minecraft {
    version("1.8.9")
    mappings {
        searge()
        mcp("stable", "22-1.8.9")
    }
    minecraftForge {
        loader("11.15.1.2318-1.8.9")
    }
    runs {
        config("client") {
            args.addAll(
                listOf(
                    "--tweakClass",
                    "moe.nea.hotswapagentforge.launch.Tweaker"
                )
            )
        }
    }
}

java.withSourcesJar()

tasks.withType(Jar::class) {
    exclude("mcmod.info", "moe/nea/hotswapagentforge/forge/TestMod.class")
    manifest { attributes(mapOf("FMLCorePlugin" to "moe.nea.hotswapagentforge.launch.HotswapAgentLoadingPlugin")) }
}

dependencies {
    compileOnly("org.hotswapagent:hotswap-agent-core:1.4.0")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}


publishing.publications {
    create<MavenPublication>("maven") {
        artifact(tasks["remapJar"])
        artifact(tasks["sourcesJar"]) { classifier = "sources" }
        artifactId = "hotswapagent-forge"
        pom {
            description.set("Hotswapagent plugin for Minecraft Forge")
            licenses {
                license {
                    name.set("LGPL-3.0 or later")
                }
            }
            developers {
                developer {
                    name.set("Linnea Gräf")
                }
            }
            scm {
                url.set("https://github.com/nea89/Hotswapagent-forge")
            }
        }
    }
}


