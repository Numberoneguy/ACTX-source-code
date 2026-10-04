import javax.inject.Inject

plugins {
    java
}

repositories {
    mavenCentral()
    flatDir {
        dirs("../desktopRuntime")
    }
}

sourceSets {
    main {
        java {
            srcDir("../src/lwjgl/java")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "--add-modules", "java.desktop",
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED"
        )
    )
}

dependencies {
    implementation(project(":"))
    implementation(fileTree(mapOf("dir" to "../desktopRuntime", "include" to listOf("*.jar"))))
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
    implementation("dev.onvoid.webrtc:webrtc-java:0.8.0")
    implementation(platform("org.lwjgl:lwjgl-bom:3.4.2"))
    implementation("org.lwjgl:lwjgl")
    implementation("org.lwjgl:lwjgl-glfw")
    implementation("org.lwjgl:lwjgl-opengl")
    implementation("org.lwjgl:lwjgl-opengles")
    implementation("org.lwjgl:lwjgl-openal")
    implementation("org.lwjgl:lwjgl-stb")
    implementation("org.lwjgl:lwjgl-jemalloc")
    implementation("org.lwjgl:lwjgl-egl")
}

abstract class CompileEPKTask @Inject constructor(
    private val fs: FileSystemOperations
) : Exec() {
    init {
        group = "build"
        description = "Compiles raw assets into assets.epk"
        
        workingDir = project.file("..")
        commandLine(
            "java", "-jar", "target_teavm_javascript/buildtools/CompileEPK.jar", 
            "desktopRuntime/resources/assets", 
            "desktopRuntime/resources/assets.epk"
        )
        
        inputs.dir("../desktopRuntime/resources/assets")
        outputs.file("../desktopRuntime/resources/assets.epk")

        val epkSource = project.layout.projectDirectory.file("../desktopRuntime/resources/assets.epk")
        val epkTargetDir = project.layout.projectDirectory.dir("../src/main/resources")

        doLast {
            fs.copy {
                from(epkSource)
                into(epkTargetDir)
            }
        }
    }
}

val compileEPK = tasks.register<CompileEPKTask>("compileEPK")

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "yes"
    archiveClassifier.set("all")

    dependsOn(tasks.named("compileJava"))
    dependsOn(compileEPK)

    manifest {
        attributes["Main-Class"] = "net.lax1dude.eaglercraft.v1_8.internal.lwjgl.MainClass"
    }

    from(sourceSets.main.get().output)
    from(rootProject.sourceSets.main.get().output)

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }

    from("src/main/resources")
    from("../src/main/resources")

    from("../desktopRuntime/resources") {
        include("assets.epk")
        include("*.epk")
        include("*.png")
        include("*.json")
    }

    from("../desktopRuntime") {
        into("natives/linux-x86_64")
        include("**/*.so")
    }

    from("../desktopRuntime") {
        into("natives/windows-x86_64")
        include("**/*.dll")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}