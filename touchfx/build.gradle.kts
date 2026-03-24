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
