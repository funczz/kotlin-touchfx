package com.github.funczz.touchfx.demo

import com.github.funczz.touchfx.behavior.GestureBehavior
import com.github.funczz.touchfx.behavior.addGestureBehavior
import com.github.funczz.touchfx.controls.*
import com.github.funczz.touchfx.i18n.TouchFXI18n
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
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * TouchFX の機能を体験するためのデモアプリケーション。
 */
class TouchFXDemo : Application() {

    override fun start(primaryStage: Stage) {
        val tabPane = TouchTabPane()

        // 言語切り替えボタン
        val localeToggle = TouchButton("").apply {
            minHeight = 44.0
            
            // 初期状態の設定
            val currentLanguage = TouchFXI18n.locale.language
            if (currentLanguage == Locale.JAPANESE.language) {
                text = "Switch to English"
            } else {
                text = "Switch to Japanese"
            }

            setOnAction {
                if (TouchFXI18n.locale.language == Locale.ENGLISH.language) {
                    TouchFXI18n.locale = Locale.JAPANESE
                    text = "Switch to English"
                } else {
                    TouchFXI18n.locale = Locale.ENGLISH
                    text = "Switch to Japanese"
                }
            }
        }
        val header = HBox(localeToggle).apply {
            alignment = Pos.CENTER_RIGHT
            padding = Insets(10.0)
        }

        // Tab 1: InertialListView
        val listViewTab = Tab("InertialListView").apply {
            isClosable = false
            content = createListViewDemo()
        }

        // Tab 2: On-Demand Loading
        val onDemandTab = Tab("On-Demand").apply {
            isClosable = false
            content = createOnDemandDemo()
        }

        // Tab 3: InertialScrollPane
        val scrollPaneTab = Tab("InertialScrollPane").apply {
            isClosable = false
            content = createScrollPaneDemo()
        }

        // Tab 4: Gestures
        val gesturesTab = Tab("Gestures").apply {
            isClosable = false
            content = createGesturesDemo()
        }

        // Tab 5: TouchFriendlyControls
        val controlsTab = Tab("Controls").apply {
            isClosable = false
            content = createControlsDemo()
        }

        // Tab 6: AdaptiveLayouts
        val layoutsTab = Tab("Layouts").apply {
            isClosable = false
            content = createLayoutsDemo()
        }

        tabPane.tabs.addAll(listViewTab, onDemandTab, scrollPaneTab, gesturesTab, controlsTab, layoutsTab)

        val root = BorderPane().apply {
            top = header
            center = tabPane
        }
        primaryStage.title = "TouchFX Demo"
        primaryStage.scene = Scene(root, 1000.0, 800.0)
        primaryStage.show()
    }

    private fun createOnDemandDemo(): Node {
        val totalCount = 1_000
        val dataCache = ConcurrentHashMap<Int, String>()
        val loadingIndices = ConcurrentHashMap.newKeySet<Int>()

        val inertialListView = InertialListView<String>().apply {
            setVirtualItems(totalCount, "Loading...")
            isRippleEnabled = true
            isBounceEnabledY = true
            isDirectionLockEnabled = true
            
            onVisibleRangeChanged = { first, last ->
                val buffer = 10 
                val start = (first - buffer).coerceAtLeast(0)
                val end = (last + buffer).coerceAtMost(totalCount - 1)

                val toLoad = (start..end).filter { index ->
                    !dataCache.containsKey(index) && !loadingIndices.contains(index)
                }

                if (toLoad.isNotEmpty()) {
                    CompletableFuture.runAsync {
                        Thread.sleep(200) 
                        toLoad.forEach { index ->
                            dataCache[index] = "REAL DATA for Item #$index"
                        }
                    }.thenRun {
                        Platform.runLater {
                            toLoad.forEach { index ->
                                if (index < items.size) {
                                    items[index] = dataCache[index]
                                }
                                loadingIndices.remove(index)
                            }
                        }
                    }
                    loadingIndices.addAll(toLoad)
                }
            }

            cellContentFactory = { item ->
                HBox(15.0).apply {
                    alignment = Pos.CENTER_LEFT
                    padding = Insets(10.0, 20.0, 10.0, 20.0)
                    prefHeight = 60.0
                    style = "-fx-background-color: white; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;"
                    
                    val isPlaceholder = item == "Loading..."
                    val icon = Circle(18.0, if (isPlaceholder) Color.LIGHTGRAY else Color.web("#4CAF50"))
                    val label = Label(item).apply {
                        style = if (isPlaceholder) "-fx-text-fill: gray; -fx-font-style: italic;" else "-fx-font-weight: bold;"
                    }
                    children.addAll(icon, label)
                    maxWidth = Double.MAX_VALUE
                }
            }
        }

        val infoLabel = Label("Virtualized ListView with 1,000 items. Adjust parameters in the right panel.").apply {
            padding = Insets(10.0)
            style = "-fx-background-color: #fff9c4; -fx-border-color: #fbc02d; -fx-border-width: 0 0 1 0;"
            maxWidth = Double.MAX_VALUE
        }

        val controlPanel = createControlPanel(
            initialSensitivityX = inertialListView.sensitivityX,
            onSensitivityXChange = { inertialListView.sensitivityX = it },
            initialSensitivityY = inertialListView.sensitivityY,
            onSensitivityYChange = { inertialListView.sensitivityY = it },
            initialInertiaX = inertialListView.inertiaX,
            onInertiaXChange = { inertialListView.inertiaX = it },
            initialInertiaY = inertialListView.inertiaY,
            onInertiaYChange = { inertialListView.inertiaY = it },
            initialFriction = inertialListView.friction,
            onFrictionChange = { inertialListView.friction = it },
            initialDirectionLock = inertialListView.isDirectionLockEnabled,
            onDirectionLockChange = { inertialListView.isDirectionLockEnabled = it },
            initialDynamicVisibility = inertialListView.isDynamicScrollBarVisible,
            onDynamicVisibilityChange = { inertialListView.isDynamicScrollBarVisible = it },
            initialBounceEnabledX = inertialListView.isBounceEnabledX,
            onBounceChangeX = { inertialListView.isBounceEnabledX = it },
            initialBounceEnabledY = inertialListView.isBounceEnabledY,
            onBounceChangeY = { inertialListView.isBounceEnabledY = it },
            initialBounceMaxRangeX = inertialListView.bounceMaxRangeX,
            onBounceMaxRangeXChange = { inertialListView.bounceMaxRangeX = it },
            initialBounceMaxRangeY = inertialListView.bounceMaxRangeY,
            onBounceMaxRangeYChange = { inertialListView.bounceMaxRangeY = it },
            initialBounceRestorationX = inertialListView.bounceRestorationX,
            onBounceRestorationXChange = { inertialListView.bounceRestorationX = it },
            initialBounceRestorationY = inertialListView.bounceRestorationY,
            onBounceRestorationYChange = { inertialListView.bounceRestorationY = it },
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

        Platform.runLater {
            inertialListView.update()
        }

        return BorderPane().apply {
            top = infoLabel
            center = inertialListView.root
            right = controlPanel
        }
    }

    private fun createListViewDemo(): Node {
        val inertialListView = InertialListView<String>().apply {
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
            isDirectionLockEnabled = false

            isHeader = { it.startsWith("HEADER:") }
            stickyHeaderEnabled = true

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
            initialSensitivityX = inertialListView.sensitivityX,
            onSensitivityXChange = { inertialListView.sensitivityX = it },
            initialSensitivityY = inertialListView.sensitivityY,
            onSensitivityYChange = { inertialListView.sensitivityY = it },
            initialInertiaX = inertialListView.inertiaX,
            onInertiaXChange = { inertialListView.inertiaX = it },
            initialInertiaY = inertialListView.inertiaY,
            onInertiaYChange = { inertialListView.inertiaY = it },
            initialFriction = inertialListView.friction,
            onFrictionChange = { inertialListView.friction = it },
            initialDirectionLock = inertialListView.isDirectionLockEnabled,
            onDirectionLockChange = { inertialListView.isDirectionLockEnabled = it },
            initialDynamicVisibility = inertialListView.isDynamicScrollBarVisible,
            onDynamicVisibilityChange = { inertialListView.isDynamicScrollBarVisible = it },
            initialBounceEnabledX = inertialListView.isBounceEnabledX,
            onBounceChangeX = { inertialListView.isBounceEnabledX = it },
            initialBounceEnabledY = inertialListView.isBounceEnabledY,
            onBounceChangeY = { inertialListView.isBounceEnabledY = it },
            initialBounceMaxRangeX = inertialListView.bounceMaxRangeX,
            onBounceMaxRangeXChange = { inertialListView.bounceMaxRangeX = it },
            initialBounceMaxRangeY = inertialListView.bounceMaxRangeY,
            onBounceMaxRangeYChange = { inertialListView.bounceMaxRangeY = it },
            initialBounceRestorationX = inertialListView.bounceRestorationX,
            onBounceRestorationXChange = { inertialListView.bounceRestorationX = it },
            initialBounceRestorationY = inertialListView.bounceRestorationY,
            onBounceRestorationYChange = { inertialListView.bounceRestorationY = it },
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

        val indicator = inertialListView.refreshIndicator
        val stackPane = if (indicator != null) StackPane(indicator, inertialListView.root) else StackPane(inertialListView.root)
        stackPane.alignment = Pos.TOP_CENTER

        return BorderPane().apply {
            center = stackPane
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
            isDirectionLockEnabled = true
            refreshIndicator = createIndicator()
        }

        val controlPanel = createControlPanel(
            initialSensitivityX = inertialScrollPane.sensitivityX,
            onSensitivityXChange = { inertialScrollPane.sensitivityX = it },
            initialSensitivityY = inertialScrollPane.sensitivityY,
            onSensitivityYChange = { inertialScrollPane.sensitivityY = it },
            initialInertiaX = inertialScrollPane.inertiaX,
            onInertiaXChange = { inertialScrollPane.inertiaX = it },
            initialInertiaY = inertialScrollPane.inertiaY,
            onInertiaYChange = { inertialScrollPane.inertiaY = it },
            initialFriction = inertialScrollPane.friction,
            onFrictionChange = { inertialScrollPane.friction = it },
            initialDirectionLock = inertialScrollPane.isDirectionLockEnabled,
            onDirectionLockChange = { inertialScrollPane.isDirectionLockEnabled = it },
            initialDynamicVisibility = inertialScrollPane.isDynamicScrollBarVisible,
            onDynamicVisibilityChange = { inertialScrollPane.isDynamicScrollBarVisible = it },
            initialBounceEnabledX = inertialScrollPane.isBounceEnabledX,
            onBounceChangeX = { inertialScrollPane.isBounceEnabledX = it },
            initialBounceEnabledY = inertialScrollPane.isBounceEnabledY,
            onBounceChangeY = { inertialScrollPane.isBounceEnabledY = it },
            initialBounceMaxRangeX = inertialScrollPane.bounceMaxRangeX,
            onBounceMaxRangeXChange = { inertialScrollPane.bounceMaxRangeX = it },
            initialBounceMaxRangeY = inertialScrollPane.bounceMaxRangeY,
            onBounceMaxRangeYChange = { inertialScrollPane.bounceMaxRangeY = it },
            initialBounceRestorationX = inertialScrollPane.bounceRestorationX,
            onBounceRestorationXChange = { inertialScrollPane.bounceRestorationX = it },
            initialBounceRestorationY = inertialScrollPane.bounceRestorationY,
            onBounceRestorationYChange = { inertialScrollPane.bounceRestorationY = it },
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

        val indicator = inertialScrollPane.refreshIndicator
        val stackPane = if (indicator != null) StackPane(indicator, inertialScrollPane.scrollPane) else StackPane(inertialScrollPane.scrollPane)
        stackPane.alignment = Pos.TOP_CENTER

        return BorderPane().apply {
            center = stackPane
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

    private fun createLayoutsDemo(): Node {
        val vbox = VBox(40.0).apply {
            padding = Insets(40.0)
            alignment = Pos.TOP_LEFT
            
            // ResponsiveLayout Demo
            val responsiveLayoutSection = VBox(10.0).apply {
                val nav = HBox(20.0).apply {
                    alignment = Pos.CENTER
                    padding = Insets(10.0)
                    style = "-fx-background-color: #333333;"
                    children.addAll(
                        Label("Home").apply { style = "-fx-text-fill: white;" },
                        Label("Search").apply { style = "-fx-text-fill: white;" },
                        Label("Settings").apply { style = "-fx-text-fill: white;" }
                    )
                }
                val content = StackPane(Label("Main Content Area").apply { style = "-fx-font-size: 2em;" }).apply {
                    style = "-fx-background-color: #f9f9f9; -fx-border-color: #cccccc;"
                }
                val responsiveLayout = ResponsiveLayout(breakpoint = 600.0).apply {
                    this.navigation = nav
                    this.content = content
                    setPrefSize(Double.MAX_VALUE, 300.0)
                }
                
                val positionSelectors = HBox(20.0).apply {
                    alignment = Pos.CENTER_LEFT
                    val narrowCombo = ComboBox<ResponsiveLayout.Side>().apply {
                        items.addAll(ResponsiveLayout.Side.values())
                        value = responsiveLayout.narrowPosition
                        valueProperty().addListener { _, _, newValue -> responsiveLayout.narrowPosition = newValue }
                    }
                    val wideCombo = ComboBox<ResponsiveLayout.Side>().apply {
                        items.addAll(ResponsiveLayout.Side.values())
                        value = responsiveLayout.widePosition
                        valueProperty().addListener { _, _, newValue -> responsiveLayout.widePosition = newValue }
                    }
                    children.addAll(
                        Label("Narrow:"), narrowCombo,
                        Label("Wide:"), wideCombo
                    )
                }

                children.addAll(
                    Label("ResponsiveLayout (Breakpoint: 600px)").apply { style = "-fx-font-weight: bold;" },
                    Label("Narrow (< 600px): Bottom Navigation. Wide (>= 600px): Navigation Rail."),
                    positionSelectors,
                    responsiveLayout
                )
            }

            // AdaptivePane Demo
            val adaptivePaneSection = VBox(10.0).apply {
                val adaptivePane = AdaptivePane(breakpoint = 500.0).apply {
                    padding = Insets(10.0)
                    style = "-fx-border-color: #2196F3; -fx-border-width: 2px; -fx-border-radius: 5px;"
                    children.addAll(
                        Rectangle(100.0, 100.0, Color.LIGHTCORAL),
                        Rectangle(100.0, 100.0, Color.LIGHTBLUE),
                        Rectangle(100.0, 100.0, Color.LIGHTGREEN)
                    )
                }
                children.addAll(
                    Label("AdaptivePane (Breakpoint: 500px)").apply { style = "-fx-font-weight: bold;" },
                    Label("Try resizing the window width above/below 500px to see layout shift."),
                    adaptivePane
                )
            }

            // FluidGridPane Demo
            val fluidGridSection = VBox(10.0).apply {
                val fluidGrid = FluidGridPane(columnWidth = 150.0).apply {
                    padding = Insets(10.0)
                    style = "-fx-border-color: #4CAF50; -fx-border-width: 2px; -fx-border-radius: 5px;"
                    // 12個の要素を追加
                    children.addAll((1..12).map {
                        StackPane().apply {
                            prefWidth = 150.0
                            prefHeight = 80.0
                            style = "-fx-background-color: #e0e0e0; -fx-background-radius: 5px;"
                            children.add(Label("Item #$it"))
                        }
                    })
                }
                children.addAll(
                    Label("FluidGridPane (Column Width: 150px)").apply { style = "-fx-font-weight: bold;" },
                    Label("The number of columns automatically adjusts to the available width."),
                    fluidGrid
                )
            }

            children.addAll(responsiveLayoutSection, adaptivePaneSection, fluidGridSection)
        }

        return ScrollPane(vbox).apply {
            isFitToWidth = true
        }
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

            val dialogSection = VBox(10.0).apply {
                children.addAll(
                    Label("TouchDialog (Optimized for Touch)").apply { style = "-fx-font-weight: bold;" },
                    HBox(20.0).apply {
                        children.addAll(
                            TouchButton("Show Information").apply {
                                setOnAction {
                                    val dialog = TouchDialog.createAlert(
                                        Alert.AlertType.INFORMATION,
                                        "Operation Successful",
                                        "The task has been completed. Tap OK to continue."
                                    )
                                    dialog.showAndWait()
                                }
                            },
                            TouchButton("Show Confirmation").apply {
                                setOnAction {
                                    val dialog = TouchDialog.createAlert(
                                        Alert.AlertType.CONFIRMATION,
                                        "Confirm Action",
                                        "Are you sure you want to proceed with this operation?"
                                    )
                                    val result = dialog.showAndWait()
                                    println("Dialog Result: ${result.orElse(null)}")
                                }
                            }
                        )
                    }
                )
            }

            val fileChooserSection = VBox(10.0).apply {
                children.addAll(
                    Label("TouchFileChooser (Native-style File Browser)").apply { style = "-fx-font-weight: bold;" },
                    HBox(20.0).apply {
                        children.addAll(
                            TouchButton("Open File").apply {
                                setOnAction {
                                    val chooser = TouchFileChooser()
                                    chooser.title = "Select Image"
                                    val file = chooser.showOpenDialog(this.scene?.window)
                                    println("Selected File: $file")
                                }
                            },
                            TouchButton("Open Multiple Files").apply {
                                setOnAction {
                                    val chooser = TouchFileChooser()
                                    chooser.title = "Select Files"
                                    val files = chooser.showOpenMultipleDialog(this.scene?.window)
                                    println("Selected Files: $files")
                                }
                            },
                            TouchButton("Open Directory").apply {
                                setOnAction {
                                    val chooser = TouchFileChooser()
                                    chooser.title = "Select Folder"
                                    val dir = chooser.showDirectoryDialog(this.scene?.window)
                                    println("Selected Directory: $dir")
                                }
                            }
                        )
                    }
                )
            }

            val checkBoxSection = VBox(10.0).apply {
                children.addAll(
                    Label("Check & Switch (Selection)").apply { style = "-fx-font-weight: bold;" },
                    HBox(30.0).apply {
                        alignment = Pos.CENTER_LEFT
                        children.addAll(
                            TouchCheckBox("Normal Check"),
                            ToggleSwitch("Toggle Switch").apply { isSelected = true }
                        )
                    }
                )
            }

            val inputSection = VBox(10.0).apply {
                children.addAll(
                    Label("TouchTextField, TouchSpinner & DatePicker (Input)").apply { style = "-fx-font-weight: bold;" },
                    TouchTextField(null).apply { promptText = "Tap to enter text..." },
                    HBox(20.0).apply {
                        alignment = Pos.CENTER_LEFT
                        children.addAll(
                            Label("Adjust Value:"),
                            TouchSpinner(0.0, 100.0, 25.0, 5.0),
                            Label("Pick Date:"),
                            TouchDatePicker()
                        )
                    }
                )
            }

            val radioButtonSection = VBox(10.0).apply {
                val group = ToggleGroup()
                children.addAll(
                    Label("TouchRadioButton (Large Hit Area)").apply { style = "-fx-font-weight: bold;" },
                    VBox(15.0).apply {
                        children.addAll(
                            TouchRadioButton("Option A").apply { toggleGroup = group; isSelected = true },
                            TouchRadioButton("Option B").apply { toggleGroup = group },
                            TouchRadioButton("Option C").apply { toggleGroup = group }
                        )
                    }
                )
            }

            val comboBoxSection = VBox(10.0).apply {
                val comboBox = TouchComboBox<String>().apply {
                    items.addAll("Item 1 (Very long text for testing layout)", "Item 2", "Item 3", "Item 4", "Item 5")
                    value = "Item 1 (Very long text for testing layout)"
                    maxWidth = 300.0
                }
                children.addAll(
                    Label("TouchComboBox (Large Items)").apply { style = "-fx-font-weight: bold;" },
                    comboBox
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

            children.addAll(buttonSection, dialogSection, fileChooserSection, checkBoxSection, inputSection, radioButtonSection, comboBoxSection, sliderSection, Separator(), standardComparisonSection)
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
        initialSensitivityX: Double = 1.0,
        onSensitivityXChange: (Double) -> Unit,
        initialSensitivityY: Double = 1.0,
        onSensitivityYChange: (Double) -> Unit,
        initialInertiaX: Double = 0.05,
        onInertiaXChange: (Double) -> Unit,
        initialInertiaY: Double = 0.05,
        onInertiaYChange: (Double) -> Unit,
        initialFriction: Double = 0.92,
        onFrictionChange: (Double) -> Unit,
        initialDirectionLock: Boolean = false,
        onDirectionLockChange: (Boolean) -> Unit,
        initialDynamicVisibility: Boolean = false,
        onDynamicVisibilityChange: (Boolean) -> Unit,
        initialBounceEnabledX: Boolean = false,
        onBounceChangeX: (Boolean) -> Unit,
        initialBounceEnabledY: Boolean = false,
        onBounceChangeY: (Boolean) -> Unit,
        initialBounceMaxRangeX: Double = Double.MAX_VALUE,
        onBounceMaxRangeXChange: (Double) -> Unit,
        initialBounceMaxRangeY: Double = Double.MAX_VALUE,
        onBounceMaxRangeYChange: (Double) -> Unit,
        initialBounceRestorationX: Double = 0.15,
        onBounceRestorationXChange: (Double) -> Unit,
        initialBounceRestorationY: Double = 0.15,
        onBounceRestorationYChange: (Double) -> Unit,
        initialSnapEnabled: Boolean = false,
        onSnapEnabledChange: (Boolean) -> Unit,
        initialSnapUnitX: Double = 0.0,
        onSnapUnitXChange: (Double) -> Unit,
        initialSnapUnitY: Double = 0.0,
        onSnapUnitYChange: (Double) -> Unit,
        initialRefreshThreshold: Double = 50.0,
        onRefreshThresholdChange: (Double) -> Unit,
        initialRippleEnabled: Boolean = false,
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
                isSelected = initialDirectionLock
                selectedProperty().addListener { _, _, newValue -> onDirectionLockChange(newValue) }
            },
            CheckBox("Dynamic ScrollBar").apply {
                isSelected = initialDynamicVisibility
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
            createSlider(0.1, 2.0, initialSensitivityX, onSensitivityXChange),
            Label("Sensitivity Y"),
            createSlider(0.1, 2.0, initialSensitivityY, onSensitivityYChange),
            Label("Inertia X"),
            createSlider(0.001, 0.2, initialInertiaX, onInertiaXChange),
            Label("Inertia Y"),
            createSlider(0.001, 0.2, initialInertiaY, onInertiaYChange),
            Label("Friction"),
            createSlider(0.5, 0.99, initialFriction, onFrictionChange),
            
            Label("Bounce Max Range X"),
            createSlider(0.0, 500.0, if (initialBounceMaxRangeX == Double.MAX_VALUE) 500.0 else initialBounceMaxRangeX, onBounceMaxRangeXChange),
            Label("Bounce Max Range Y"),
            createSlider(0.0, 500.0, if (initialBounceMaxRangeY == Double.MAX_VALUE) 500.0 else initialBounceMaxRangeY, onBounceMaxRangeYChange),

            Label("Bounce Restoration X"),
            createSlider(0.01, 0.5, initialBounceRestorationX, onBounceRestorationXChange),
            Label("Bounce Restoration Y"),
            createSlider(0.01, 0.5, initialBounceRestorationY, onBounceRestorationYChange),

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
