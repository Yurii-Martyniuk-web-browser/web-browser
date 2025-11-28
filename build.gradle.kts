plugins {
    java
    application
    id("org.javamodularity.moduleplugin") version "1.8.15"
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

group = "com.web-browser"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

jlink {
    options.addAll(listOf(
        "--strip-debug",
        "--compress", "2",
        "--no-header-files",
        "--no-man-pages",
        "--bind-services"
    ))
    launcher {
        name = "app"
    }
    mergedModule {
        addOptions("--add-modules", "javafx.controls,javafx.fxml")
    }
}


tasks.register<Exec>("packageApp") {
    dependsOn("build")

    val jarFile = tasks.named<Jar>("jar").get().archiveFile.get().asFile
    val distDir = project.projectDir.resolve("dist")

    doFirst {
        distDir.mkdirs()
    }

    commandLine(
        "jpackage",
        "--name", "BrowserApp",
        "--input", jarFile.parentFile.absolutePath,
        "--dest", distDir.absolutePath,
        "--main-jar", jarFile.name,
        "--main-class", "com.webbrowser.webbrowser.BrowserApp",
        "--win-console",
        "--type", "app-image",
        "--add-modules", "javafx.controls,javafx.fxml"
    )

    doLast {
        val dbTarget = distDir.resolve("browser_client.db")

        if (!dbTarget.exists()) {
            println("📦 Creating SQLite DB with schema...")
            dbTarget.createNewFile()
        }
    }
}

application {
    mainModule.set("com.webbrowser.webbrowser")
    mainClass.set("com.webbrowser.webbrowser.BrowserApp")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.controlsfx:controlsfx:11.2.1")
    implementation("org.kordamp.bootstrapfx:bootstrapfx-core:0.4.0")
    implementation("org.openjfx:javafx-controls:21.0.6")
    implementation("org.openjfx:javafx-fxml:21.0.6")

    implementation("com.google.code.gson:gson:2.13.1")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to "com.webbrowser.webbrowser.BrowserApp"
        )
    }

    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
}

