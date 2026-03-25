/**
 * plugins
 */
plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

/**
 * javafx
 */
javafx {
    version = "17.0.10"
    modules = listOf("javafx.base", "javafx.controls", "javafx.fxml", "javafx.graphics")
}

/**
 * application
 */
application {
    mainClass.set("com.github.funczz.touchfx.demo.TouchFXDemoKt")
    mainModule.set("com.github.funczz.touchfx.demo")
}

/**
 * dependencies
 */
dependencies {
    implementation(project(":touchfx"))
    implementation(kotlin("stdlib"))
}
