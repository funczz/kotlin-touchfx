/**
 * plugins
 */
plugins {
    id("nebula.maven-publish") version "18.4.0"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

/**
 * buildscript
 */
buildscript {
    dependencies {
    }
}

/**
 * javafx
 */
javafx {
    version = "17.0.10"
    modules = listOf("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics")
}

/**
 * dependencies
 */
dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.testfx:testfx-core:4.0.17")
    testImplementation("org.testfx:testfx-junit5:4.0.17")
}

/**
 * task: Test
 */
tasks.withType<Test> {
    doFirst {
        println("Test JVM Args: ${allJvmArgs}")
    }
    jvmArgs(
        "--add-exports", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.glass.ui=ALL-UNNAMED",
        "--add-exports", "javafx.graphics/com.sun.glass.ui.monocle=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.glass.ui=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/com.sun.glass.ui.monocle=ALL-UNNAMED",
        "--add-opens", "javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED",
        "--add-opens", "javafx.base/com.sun.javafx.binding=ALL-UNNAMED",
        "--add-opens", "javafx.base/com.sun.javafx.event=ALL-UNNAMED",
        "-Dtestfx.robot=glass",
        "-Dtestfx.headless=true",
        "-Dglass.platform=Monocle",
        "-Dmonocle.platform=Headless",
        "-Dprism.order=sw"
    )
}

/**
 * plugin: nebula.maven-publish
 */
publishing {
    publications {
        group = "com.github.funczz"
    }

    repositories {
        maven {
            url = uri(
                PublishMavenRepository.url(
                    version = version.toString(),
                    baseUrl = "${project.layout.buildDirectory.get().asFile.absolutePath}/mvn-repos"
                )
            )
        }
    }
}
