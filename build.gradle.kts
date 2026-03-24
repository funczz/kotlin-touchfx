import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * plugins
 */
plugins {
    kotlin("jvm") version "1.9.23" apply false
    id("nebula.release") version "19.0.10"
}

/**
 * plugin: nebula release, nebula maven-publish
 */
tasks {
    "release" {
        dependsOn(
            //":feature:publish", ":demo:publish"
            ":touchfx:publish"
        )
    }
}

/**
 * all projects
 */
allprojects {
    /**
     * build script
     */
    buildscript {
        /**
         * repositories
         */
        repositories {
            mavenLocal()
            mavenCentral()
        }
    }

    /**
     * repositories
     */
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

/**
 * sub projects
 */
subprojects {
    /**
     * plugins
     */
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "jacoco")

    /**
     * repositories
     */
    repositories {
    }

    /**
     * dependencies
     */
    dependencies {
        /**
         * dependencies: libs Directory
         */
        "implementation"(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

        "testImplementation"("org.junit.jupiter:junit-jupiter:5.10.2")
        "testImplementation"("org.junit.platform:junit-platform-launcher:1.9.3")
    }

    /**
     * task: JavaCompile
     */
    Action<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    /**
     * task: KotlinCompile
     */
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    /**
     * task: Test
     */
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
