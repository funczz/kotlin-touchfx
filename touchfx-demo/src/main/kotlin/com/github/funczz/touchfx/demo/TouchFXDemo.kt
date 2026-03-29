package com.github.funczz.touchfx.demo

import com.github.funczz.touchfx.behavior.GestureBehavior
import com.github.funczz.touchfx.behavior.addGestureBehavior
import com.github.funczz.touchfx.controls.*
import javafx.application.Application
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import java.util.concurrent.CompletableFuture

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

        // Tab 3: Gestures
        val gesturesTab = Tab("Gestures").apply {
            isClosable = false
            content = createGesturesDemo()
        }

        // Tab 4: TouchFriendlyControls
        val controlsTab = Tab("Controls").apply {
            isClosable = false
            content = createControlsDemo()
        }

        tabPane.tabs.addAll(listViewTab, scrollPaneTab, gesturesTab, controlsTab)

        val root = BorderPane(tabPane)
        primaryStage.title = "TouchFX Demo"
        primaryStage.scene = Scene(root, 800.0, 600.0)
        primaryStage.show()
    }

    private fun createListViewDemo(): Node {
        val inertialListView = InertialListView<String>().apply {
            // アイテムの生成（定期的にヘッダーを挿入）
            val demoItems = mutableListOf<String>()
            for (i in 1..20) {
                demoItems.add("HEADER: Category $i")
                for (j in 1..10) {
                    demoItems.add("List Item #$i-$j")
                }
            }
            items.addAll(demoItems)

            isBounceEnabledY = true
            isSnapEnabled = true
            snapUnitY = 60.0
            isRippleEnabled = true
            
            // スティッキーヘッダー設定
            isHeader = { it.startsWith("HEADER:") }
            stickyHeaderEnabled = true

            // セルの見た目をカスタマイズ
            cellContentFactory = { item ->
                val isHeaderItem = isHeader(item)
                HBox(15.0).apply {
                    alignment = Pos.CENTER_LEFT
                    padding = Insets(10.0, 15.0, 10.0, 15.0)
                    prefHeight = 60.0
                    
                    if (isHeaderItem) {
                        style = "-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;"
                        val icon = Rectangle(10.0, 30.0, Color.web("#FF9800"))
                        val label = Label(item).apply { style = "-fx-font-weight: bold; -fx-font-size: 1.2em;" }
                        children.addAll(icon, label)
                    } else {
                        style = "-fx-background-color: white; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;"
                        val icon = Circle(18.0, Color.web("#673AB7", 0.8))
                        val textContainer = VBox(2.0).apply {
                            alignment = Pos.CENTER_LEFT
                            children.addAll(
                                Label(item).apply { style = "-fx-font-weight: bold; -fx-font-size: 1.1em;" },
                                Label("Tap or swipe this item to see effects").apply { style = "-fx-text-fill: gray; -fx-font-size: 0.9em;" }
                            )
                        }
                        children.addAll(icon, textContainer)
                    }
                    maxWidth = Double.MAX_VALUE
                }
            }

            // Pull-to-Refresh
            refreshIndicator = createIndicator()
            onRefresh = {
                val future = CompletableFuture<Unit>()
                CompletableFuture.runAsync {
                    Thread.sleep(2000)
                }.thenRun {
                    Platform.runLater {
                        items.add(0, "NEW Item (Refreshed)")
                        future.complete(Unit)
                    }
                }
                future
            }

            // Swipe Actions
            swipeLeftFactory = { item, container ->
                if (isHeader(item)) null else {
                    HBox().apply {
                        alignment = Pos.CENTER_LEFT
                        style = "-fx-background-color: #2196F3;"
                        prefWidth = 80.0
                        children.add(Button("Edit").apply {
                            style = "-fx-background-color: transparent; -fx-text-fill: white;"
                            maxWidth = Double.MAX_VALUE
                            maxHeight = Double.MAX_VALUE
                            HBox.setHgrow(this, Priority.ALWAYS)
                            setOnAction { 
                                println("Edit clicked: $item")
                                container.reset()
                            }
                        })
                    }
                }
            }
            swipeRightFactory = { item, container ->
                if (isHeader(item)) null else {
                    HBox().apply {
                        alignment = Pos.CENTER_RIGHT
                        style = "-fx-background-color: #F44336;"
                        prefWidth = 80.0
                        children.add(Button("Delete").apply {
                            style = "-fx-background-color: transparent; -fx-text-fill: white;"
                            maxWidth = Double.MAX_VALUE
                            maxHeight = Double.MAX_VALUE
                            HBox.setHgrow(this, Priority.ALWAYS)
                            setOnAction { 
                                println("Delete clicked: $item")
                                container.reset(animate = false)
                                items.remove(item)
                            }
                        })
                    }
                }
            }
        }

        val controlPanel = createControlPanel(
            onSensitivityXChange = { inertialListView.sensitivityX = it },
            onSensitivityYChange = { inertialListView.sensitivityY = it },
            onInertiaXChange = { inertialListView.inertiaX = it },
            onInertiaYChange = { inertialListView.inertiaY = it },
            onFrictionChange = { inertialListView.friction = it },
            onDirectionLockChange = { inertialListView.isDirectionLockEnabled = it },
            onDynamicVisibilityChange = { inertialListView.isDynamicScrollBarVisible = it },
            initialBounceEnabledX = inertialListView.isBounceEnabledX,
            onBounceChangeX = { inertialListView.isBounceEnabledX = it },
            initialBounceEnabledY = inertialListView.isBounceEnabledY,
            onBounceChangeY = { inertialListView.isBounceEnabledY = it },
            initialSnapEnabled = inertialListView.isSnapEnabled,
            onSnapEnabledChange = { inertialListView.isSnapEnabled = it },
            initialSnapUnitX = inertialListView.snapUnitX,
            onSnapUnitXChange = { inertialListView.snapUnitX = it },
            initialSnapUnitY = inertialListView.snapUnitY,
            onSnapUnitYChange = { inertialListView.snapUnitY = it },
            initialRefreshThreshold = inertialListView.refreshThreshold,
            onRefreshThresholdChange = { inertialListView.refreshThreshold = it },
            initialRippleEnabled = inertialListView.isRippleEnabled,
            onRippleEnabledChange = { inertialListView.isRippleEnabled = it },
            initialStickyHeaderEnabled = inertialListView.stickyHeaderEnabled,
            onStickyHeaderChange = { inertialListView.stickyHeaderEnabled = it }
        )

        return BorderPane().apply {
            center = StackPane(inertialListView.refreshIndicator, inertialListView.root).apply {
                alignment = Pos.TOP_CENTER
            }
            right = controlPanel
        }
    }

    private fun createScrollPaneDemo(): Node {
        val content = VBox(10.0).apply {
            padding = Insets(20.0)
            children.addAll((1..100).map {
                Label("ScrollPane Content Label #$it").apply {
                    style = "-fx-font-size: 18px; -fx-border-color: lightgray; -fx-padding: 10; -fx-background-color: white;"
                    minWidth = 1000.0
                }
            })
        }

        val inertialScrollPane = InertialScrollPane().apply {
            this.content = content
            isBounceEnabledY = true
            isRippleEnabled = true
            
            // Pull-to-Refresh
            refreshIndicator = createIndicator()
            onRefresh = {
                val future = CompletableFuture<Unit>()
                CompletableFuture.runAsync {
                    Thread.sleep(2000)
                }.thenRun {
                    Platform.runLater {
                        future.complete(Unit)
                    }
                }
                future
            }
        }

        val controlPanel = createControlPanel(
            onSensitivityXChange = { inertialScrollPane.sensitivityX = it },
            onSensitivityYChange = { inertialScrollPane.sensitivityY = it },
            onInertiaXChange = { inertialScrollPane.inertiaX = it },
            onInertiaYChange = { inertialScrollPane.inertiaY = it },
            onFrictionChange = { inertialScrollPane.friction = it },
            onDirectionLockChange = { inertialScrollPane.isDirectionLockEnabled = it },
            onDynamicVisibilityChange = { inertialScrollPane.isDynamicScrollBarVisible = it },
            initialBounceEnabledX = inertialScrollPane.isBounceEnabledX,
            onBounceChangeX = { inertialScrollPane.isBounceEnabledX = it },
            initialBounceEnabledY = inertialScrollPane.isBounceEnabledY,
            onBounceChangeY = { inertialScrollPane.isBounceEnabledY = it },
            initialSnapEnabled = inertialScrollPane.isSnapEnabled,
            onSnapEnabledChange = { inertialScrollPane.isSnapEnabled = it },
            initialSnapUnitX = inertialScrollPane.snapUnitX,
            onSnapUnitXChange = { inertialScrollPane.snapUnitX = it },
            initialSnapUnitY = inertialScrollPane.snapUnitY,
            onSnapUnitYChange = { inertialScrollPane.snapUnitY = it },
            initialRefreshThreshold = inertialScrollPane.refreshThreshold,
            onRefreshThresholdChange = { inertialScrollPane.refreshThreshold = it },
            initialRippleEnabled = inertialScrollPane.isRippleEnabled,
            onRippleEnabledChange = { inertialScrollPane.isRippleEnabled = it }
        )

        return BorderPane().apply {
            center = StackPane(inertialScrollPane.refreshIndicator, inertialScrollPane.scrollPane).apply {
                alignment = Pos.TOP_CENTER
            }
            right = controlPanel
        }
    }

    private fun createGesturesDemo(): Node {
        val statusLabel = Label("Gesture Status: None").apply {
            style = "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;"
        }

        val target = Rectangle(200.0, 200.0, Color.LIGHTBLUE).apply {
            stroke = Color.BLUE
            strokeWidth = 2.0
            arcWidth = 20.0
            arcHeight = 20.0
        }

        val behavior = target.addGestureBehavior {
            onPinch = { factor ->
                target.scaleX *= factor
                target.scaleY *= factor
                statusLabel.text = "Gesture Status: Pinch (factor=${"%.2f".format(factor)})"
            }
            onRotate = { delta ->
                target.rotate += delta
                statusLabel.text = "Gesture Status: Rotate (delta=${"%.2f".format(delta)})"
            }
            onLongPress = { _, _ ->
                target.fill = if (target.fill == Color.LIGHTBLUE) Color.LIGHTCORAL else Color.LIGHTBLUE
                statusLabel.text = "Gesture Status: Long Press detected!"
            }
        }

        val controlPanel = VBox(15.0).apply {
            padding = Insets(20.0)
            style = "-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 0 0 0 1;"
            prefWidth = 250.0
            
            children.addAll(
                Label("Simulation Settings").apply { style = "-fx-font-weight: bold;" },
                CheckBox("Enable Pinch Simulation").apply {
                    selectedProperty().addListener { _, _, newValue -> behavior.isPinchSimulationEnabled = newValue }
                },
                CheckBox("Enable Rotate Simulation").apply {
                    selectedProperty().addListener { _, _, newValue -> behavior.isRotateSimulationEnabled = newValue }
                },
                Separator(),
                Button("Reset Transform").apply {
                    maxWidth = Double.MAX_VALUE
                    setOnAction {
                        target.scaleX = 1.0
                        target.scaleY = 1.0
                        target.rotate = 0.0
                        target.fill = Color.LIGHTBLUE
                        statusLabel.text = "Gesture Status: None"
                    }
                }
            )
        }

        val instructionLabel = Label(
            "Instructions:\n" +
            "• Mouse/Keyboard Simulation:\n" +
            "  - Shift + Drag: Pinch & Rotate\n" +
            "  - Ctrl + Scroll: Zoom\n" +
            "  - Alt + Scroll: Rotate\n" +
            "  - Long Press: Hold mouse button\n" +
            "• Simulation Modes (Use CheckBoxes):\n" +
            "  - Normal Drag will perform the gesture"
        ).apply {
            style = "-fx-text-fill: #666; -fx-line-spacing: 5; -fx-background-color: #f9f9f9; -fx-padding: 15; -fx-border-color: #ddd; -fx-border-radius: 5;"
        }

        val mainArea = VBox(30.0).apply {
            alignment = Pos.CENTER
            padding = Insets(50.0)
            children.addAll(statusLabel, target, instructionLabel)
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        return HBox(mainArea, controlPanel)
    }

    private fun createControlsDemo(): Node {
        val vbox = VBox(30.0).apply {
            padding = Insets(40.0)
            alignment = Pos.TOP_LEFT
            
            val buttonSection = VBox(10.0).apply {
                children.addAll(
                    Label("TouchButton (Large Area & Ripple Effect)").apply { style = "-fx-font-weight: bold;" },
                    HBox(20.0).apply {
                        children.addAll(
                            TouchButton("Default Button"),
                            TouchButton("Primary Action").apply {
                                style = "-fx-base: #2196F3; -fx-text-fill: white;"
                            },
                            TouchButton("Danger Zone").apply {
                                style = "-fx-base: #F44336; -fx-text-fill: white;"
                            }
                        )
                    }
                )
            }

            val checkBoxSection = VBox(10.0).apply {
                children.addAll(
                    Label("TouchCheckBox (Large Hit Area)").apply { style = "-fx-font-weight: bold;" },
                    VBox(15.0).apply {
                        children.addAll(
                            TouchCheckBox("Enable Notifications"),
                            TouchCheckBox("Stay Logged In").apply { isSelected = true },
                            TouchCheckBox("Accept Terms and Conditions")
                        )
                    }
                )
            }

            val sliderSection = VBox(10.0).apply {
                val slider = TouchSlider(0.0, 100.0, 50.0)
                val valueLabel = Label("Current Value: 50.0")
                slider.valueProperty().addListener { _, _, newValue ->
                    valueLabel.text = "Current Value: ${"%.1f".format(newValue)}"
                }
                children.addAll(
                    Label("TouchSlider (Large Thumb)").apply { style = "-fx-font-weight: bold;" },
                    slider,
                    valueLabel
                )
            }

            val standardComparisonSection = VBox(10.0).apply {
                children.addAll(
                    Label("Standard Controls (for comparison)").apply { style = "-fx-font-weight: bold; -fx-text-fill: gray;" },
                    HBox(20.0).apply {
                        children.addAll(
                            Button("Standard Button"),
                            CheckBox("Standard CheckBox"),
                            Slider(0.0, 100.0, 50.0)
                        )
                    }
                )
            }

            children.addAll(buttonSection, checkBoxSection, sliderSection, Separator(), standardComparisonSection)
        }

        return ScrollPane(vbox).apply {
            isFitToWidth = true
        }
    }

    private fun createIndicator(): Node {
        return Label("Refreshing...").apply {
            style = "-fx-background-color: #e0e0e0; -fx-padding: 10 20; -fx-background-radius: 0 0 10 10;"
            textFill = Color.DARKBLUE
            isVisible = false
            maxWidth = Double.MAX_VALUE
            alignment = Pos.CENTER
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
        initialBounceEnabledX: Boolean,
        onBounceChangeX: (Boolean) -> Unit,
        initialBounceEnabledY: Boolean,
        onBounceChangeY: (Boolean) -> Unit,
        initialSnapEnabled: Boolean,
        onSnapEnabledChange: (Boolean) -> Unit,
        initialSnapUnitX: Double,
        onSnapUnitXChange: (Double) -> Unit,
        initialSnapUnitY: Double,
        onSnapUnitYChange: (Double) -> Unit,
        initialRefreshThreshold: Double,
        onRefreshThresholdChange: (Double) -> Unit,
        initialRippleEnabled: Boolean,
        onRippleEnabledChange: (Boolean) -> Unit,
        initialStickyHeaderEnabled: Boolean = false,
        onStickyHeaderChange: ((Boolean) -> Unit)? = null
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
                isSelected = false
                selectedProperty().addListener { _, _, newValue -> onDirectionLockChange(newValue) }
            },
            CheckBox("Dynamic ScrollBar").apply {
                isSelected = false
                selectedProperty().addListener { _, _, newValue -> onDynamicVisibilityChange(newValue) }
            },
            CheckBox("Bounce Effect X").apply {
                isSelected = initialBounceEnabledX
                selectedProperty().addListener { _, _, newValue -> onBounceChangeX(newValue) }
            },
            CheckBox("Bounce Effect Y").apply {
                isSelected = initialBounceEnabledY
                selectedProperty().addListener { _, _, newValue -> onBounceChangeY(newValue) }
            },
            CheckBox("Snapping").apply {
                isSelected = initialSnapEnabled
                selectedProperty().addListener { _, _, newValue -> onSnapEnabledChange(newValue) }
            },
            CheckBox("Ripple Effect").apply {
                isSelected = initialRippleEnabled
                selectedProperty().addListener { _, _, newValue -> onRippleEnabledChange(newValue) }
            },
            CheckBox("Sticky Header").apply {
                isSelected = initialStickyHeaderEnabled
                selectedProperty().addListener { _, _, newValue -> onStickyHeaderChange?.invoke(newValue) }
                disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding({ onStickyHeaderChange == null }, 
                    javafx.beans.property.SimpleObjectProperty(onStickyHeaderChange)))
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
            createSlider(0.5, 0.99, 0.92, onFrictionChange),
            Label("Snap Unit X"),
            createSlider(0.0, 100.0, initialSnapUnitX, onSnapUnitXChange),
            Label("Snap Unit Y"),
            createSlider(0.0, 100.0, initialSnapUnitY, onSnapUnitYChange),
            Label("Refresh Threshold"),
            createSlider(10.0, 200.0, initialRefreshThreshold, onRefreshThresholdChange)
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
