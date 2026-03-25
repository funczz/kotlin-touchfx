package com.github.funczz.touchfx.demo

import com.github.funczz.touchfx.controls.InertialListView
import com.github.funczz.touchfx.controls.InertialScrollPane
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage

/**
 * TouchFX の機能を体験するためのデモアプリケーション。
 */
class TouchFXDemo : Application() {

    override fun start(primaryStage: Stage) {
        val tabPane = TabPane()

        // Tab 1: InertialListView
        val listViewTab = Tab("InertialListView").apply {
            isClosable = false
            content = createListViewDemo()
        }

        // Tab 2: InertialScrollPane
        val scrollPaneTab = Tab("InertialScrollPane").apply {
            isClosable = false
            content = createScrollPaneDemo()
        }

        tabPane.tabs.addAll(listViewTab, scrollPaneTab)

        val root = BorderPane(tabPane)
        primaryStage.title = "TouchFX Demo"
        primaryStage.scene = Scene(root, 600.0, 500.0)
        primaryStage.show()
    }

    private fun createListViewDemo(): Node {
        val inertialListView = InertialListView<String>().apply {
            items.addAll((1..200).map { "List Item #$it" })
        }

        val controlPanel = createControlPanel(
            onSensitivityChange = { inertialListView.sensitivity = it },
            onInertiaChange = { inertialListView.inertia = it },
            onFrictionChange = { inertialListView.friction = it }
        )

        return BorderPane().apply {
            center = inertialListView.listView
            right = controlPanel
        }
    }

    private fun createScrollPaneDemo(): Node {
        val content = VBox(10.0).apply {
            padding = Insets(20.0)
            children.addAll((1..50).map {
                Label("ScrollPane Content Label #$it").apply {
                    style = "-fx-font-size: 18px; -fx-border-color: lightgray; -fx-padding: 10;"
                    maxWidth = Double.MAX_VALUE
                }
            })
        }

        val inertialScrollPane = InertialScrollPane().apply {
            this.content = content
        }

        val controlPanel = createControlPanel(
            onSensitivityChange = { inertialScrollPane.sensitivity = it },
            onInertiaChange = { inertialScrollPane.inertia = it },
            onFrictionChange = { inertialScrollPane.friction = it }
        )

        return BorderPane().apply {
            center = inertialScrollPane.scrollPane
            right = controlPanel
        }
    }

    private fun createControlPanel(
        onSensitivityChange: (Double) -> Unit,
        onInertiaChange: (Double) -> Unit,
        onFrictionChange: (Double) -> Unit
    ): Node {
        val panel = VBox(10.0).apply {
            padding = Insets(15.0)
            style = "-fx-background-color: #f4f4f4; -fx-border-color: lightgray; -fx-border-width: 0 0 0 1;"
            prefWidth = 200.0
        }

        panel.children.addAll(
            Label("Settings").apply { style = "-fx-font-weight: bold;" },
            Separator(),
            Label("Sensitivity"),
            createSlider(0.0001, 0.05, 0.005, onSensitivityChange),
            Label("Inertia"),
            createSlider(0.0001, 0.01, 0.0005, onInertiaChange),
            Label("Friction"),
            createSlider(0.5, 0.99, 0.92, onFrictionChange)
        )

        return panel
    }

    private fun createSlider(min: Double, max: Double, initial: Double, onChange: (Double) -> Unit): Slider {
        return Slider(min, max, initial).apply {
            valueProperty().addListener { _, _, newValue -> onChange(newValue.toDouble()) }
        }
    }
}

/**
 * デモのメインエントリポイント。
 */
fun main(args: Array<String>) {
    Application.launch(TouchFXDemo::class.java, *args)
}
