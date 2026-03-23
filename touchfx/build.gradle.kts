/**
 * plugins
 */
plugins {
    id("nebula.maven-publish") version "18.4.0"
}

/**
 * buildscript
 */
buildscript {
    dependencies {
    }
}

/**
 * dependencies
 */
dependencies {
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
                    baseUrl = project.layout.buildDirectory.dir("mvn-repos").toString()
                )
            )
        }
    }
}
