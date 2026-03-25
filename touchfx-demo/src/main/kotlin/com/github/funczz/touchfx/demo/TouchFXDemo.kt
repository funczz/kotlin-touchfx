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
        primaryStage.scene = Scene(root, 800.0, 600.0) // 幅を少し広げる
        primaryStage.show()
    }

    private fun createListViewDemo(): Node {
        val inertialListView = InertialListView<String>().apply {
            items.addAll((1..500).map { "List Item #$it" })
        }

        val controlPanel = createControlPanel(
            onSensitivityXChange = { inertialListView.sensitivityX = it },
            onSensitivityYChange = { inertialListView.sensitivityY = it },
            onInertiaXChange = { inertialListView.inertiaX = it },
            onInertiaYChange = { inertialListView.inertiaY = it },
            onFrictionChange = { inertialListView.friction = it },
            onDirectionLockChange = { inertialListView.isDirectionLockEnabled = it },
            onDynamicVisibilityChange = { inertialListView.isDynamicScrollBarVisible = it },
            onBounceChange = { inertialListView.isBounceEnabled = it }
        )

        return BorderPane().apply {
            center = inertialListView.listView
            right = controlPanel
        }
    }

    private fun createScrollPaneDemo(): Node {
        val content = VBox(10.0).apply {
            padding = Insets(20.0)
            children.addAll((1..100).map {
                Label("ScrollPane Content Label #$it").apply {
                    style = "-fx-font-size: 18px; -fx-border-color: lightgray; -fx-padding: 10;"
                    minWidth = 1000.0
                }
            })
        }

        val inertialScrollPane = InertialScrollPane().apply {
            this.content = content
        }

        val controlPanel = createControlPanel(
            onSensitivityXChange = { inertialScrollPane.sensitivityX = it },
            onSensitivityYChange = { inertialScrollPane.sensitivityY = it },
            onInertiaXChange = { inertialScrollPane.inertiaX = it },
            onInertiaYChange = { inertialScrollPane.inertiaY = it },
            onFrictionChange = { inertialScrollPane.friction = it },
            onDirectionLockChange = { inertialScrollPane.isDirectionLockEnabled = it },
            onDynamicVisibilityChange = { inertialScrollPane.isDynamicScrollBarVisible = it },
            onBounceChange = { inertialScrollPane.isBounceEnabled = it }
        )

        return BorderPane().apply {
            center = inertialScrollPane.scrollPane
            right = controlPanel
        }
    }

    private fun createControlPanel(
        onSensitivityXChange: (Double) -> Unit,
        onSensitivityYChange: (Double) -> Unit,
        onInertiaXChange: (Double) -> Unit,
        onInertiaYChange: (Double) -> Unit,
        onFrictionChange: (Double) -> Unit,
        onDirectionLockChange: (Boolean) -> Unit,
        onDynamicVisibilityChange: (Boolean) -> Unit,
        onBounceChange: (Boolean) -> Unit
    ): Node {
        val panel = VBox(10.0).apply {
            padding = Insets(15.0)
            style = "-fx-background-color: #f4f4f4; -fx-border-color: lightgray; -fx-border-width: 0 0 0 1;"
            prefWidth = 250.0
        }

        val scrollWrapper = ScrollPane(panel).apply {
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            isFitToWidth = true
        }

        panel.children.addAll(
            Label("Settings").apply { style = "-fx-font-weight: bold; -fx-font-size: 14px;" },
            Separator(),
            
            Label("Features").apply { style = "-fx-font-weight: bold;" },
            CheckBox("Direction Lock").apply {
                isSelected = true
                selectedProperty().addListener { _, _, newValue -> onDirectionLockChange(newValue) }
            },
            CheckBox("Dynamic ScrollBar").apply {
                isSelected = false
                selectedProperty().addListener { _, _, newValue -> onDynamicVisibilityChange(newValue) }
            },
            CheckBox("Bounce Effect").apply {
                isSelected = false
                selectedProperty().addListener { _, _, newValue -> onBounceChange(newValue) }
            },
            
            Separator(),
            Label("Parameters").apply { style = "-fx-font-weight: bold;" },
            
            Label("Sensitivity X"),
            createSlider(0.0001, 0.05, 0.005, onSensitivityXChange),
            
            Label("Sensitivity Y"),
            createSlider(0.0001, 0.05, 0.005, onSensitivityYChange),
            
            Label("Inertia X"),
            createSlider(0.0001, 0.01, 0.0005, onInertiaXChange),
            
            Label("Inertia Y"),
            createSlider(0.0001, 0.01, 0.0005, onInertiaYChange),
            
            Label("Friction"),
            createSlider(0.5, 0.99, 0.92, onFrictionChange)
        )

        return scrollWrapper
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
