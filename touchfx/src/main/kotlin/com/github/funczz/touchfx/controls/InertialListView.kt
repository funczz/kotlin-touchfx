package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.behavior.TouchBehavior
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.ScrollBar
import javafx.scene.control.skin.ListViewSkin
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane

/**
 * 慣性スクロール機能を持つ [ListView] のラッパーコンポーネントです。
 */
class InertialListView<T>(
    val listView: ListView<T> = ListView(),
    useDefaultStyle: Boolean = true
) {

    val root: StackPane = StackPane(listView)
    private val behavior = TouchBehavior(listView)

    var isRippleEnabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateCellFactory()
            }
        }

    var swipeLeftFactory: ((T, SwipeableContainer) -> Node?)? = null
        set(value) {
            field = value
            updateCellFactory()
        }

    var swipeRightFactory: ((T, SwipeableContainer) -> Node?)? = null
        set(value) {
            field = value
            updateCellFactory()
        }

    var swipeThreshold: Double = 50.0
        set(value) {
            field = value
            updateCellFactory()
        }

    var cellContentFactory: ((T) -> Node)? = null
        set(value) {
            field = value
            updateCellFactory()
        }

    var isHeader: (T) -> Boolean = { false }
        set(value) {
            field = value
            updateStickyHeader()
        }

    var stickyHeaderEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                if (!root.children.contains(floatingHeaderContainer)) {
                    root.children.add(floatingHeaderContainer)
                }
                updateStickyHeader()
            } else {
                root.children.remove(floatingHeaderContainer)
            }
        }

    var onVisibleRangeChanged: ((Int, Int) -> Unit)? = null

    private val floatingHeaderContainer = AnchorPane().apply {
        isMouseTransparent = true
        StackPane.setAlignment(this, Pos.TOP_LEFT)
    }

    private var lastFirstVisible: Int = -1
    private var lastLastVisible: Int = -1
    private var lastUpdateTime: Long = 0L
    private val updateIntervalThreshold = 50_000_000L // 50ms

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                listView.stylesheets.add(it)
            }
            listView.styleClass.add("touch-fx")
        }
        updateCellFactory()

        Platform.runLater {
            findVerticalScrollBar()?.let { scrollBar ->
                scrollBar.valueProperty().addListener { _, _, _ ->
                    throttleUpdate()
                }
            }
            update()
        }
    }

    private fun throttleUpdate() {
        val now = System.nanoTime()
        if (now - lastUpdateTime > updateIntervalThreshold) {
            lastUpdateTime = now
            updateVisibleRange()
            if (stickyHeaderEnabled) updateStickyHeader()
        }
    }

    fun setVirtualItems(count: Int, placeholder: T? = null) {
        val list = ArrayList<T?>(count)
        for (i in 0 until count) {
            list.add(placeholder)
        }
        @Suppress("UNCHECKED_CAST")
        items = FXCollections.observableList(list as List<T>)
    }

    fun update() {
        updateVisibleRange()
        if (stickyHeaderEnabled) updateStickyHeader()
    }

    fun updateVisibleRange() {
        val skin = listView.skin as? ListViewSkin<T> ?: return
        val flow = skin.children.find { it is VirtualFlow<*> } as? VirtualFlow<*> ?: return

        val firstCell = flow.firstVisibleCell as? ListCell<*>
        val lastCell = flow.lastVisibleCell as? ListCell<*>

        if (firstCell != null && lastCell != null) {
            val first = firstCell.index
            val last = lastCell.index

            if (first != lastFirstVisible || last != lastLastVisible) {
                lastFirstVisible = first
                lastLastVisible = last
                onVisibleRangeChanged?.invoke(first, last)
            }
        }
    }

    private fun updateCellFactory() {
        listView.setCellFactory { _ ->
            object : ListCell<T>() {
                override fun updateItem(item: T, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty) {
                        text = null
                        graphic = null
                    } else {
                        val content = cellContentFactory?.invoke(item) ?: Label(item?.toString() ?: "").apply {
                            maxWidth = Double.MAX_VALUE
                            styleClass.add("label")
                        }

                        val rippleWrapper = StackPane(content).apply {
                            maxWidth = Double.MAX_VALUE
                            maxHeight = Region.USE_PREF_SIZE
                            alignment = Pos.CENTER_LEFT
                            style = "-fx-background-color: transparent;"
                            isPickOnBounds = true
                        }

                        if (swipeLeftFactory != null || swipeRightFactory != null) {
                            val swipeContent = AnchorPane(rippleWrapper).apply {
                                style = "-fx-background-color: white;"
                                AnchorPane.setTopAnchor(rippleWrapper, 0.0)
                                AnchorPane.setLeftAnchor(rippleWrapper, 10.0)
                                AnchorPane.setRightAnchor(rippleWrapper, 10.0)
                            }
                            val sContainer = SwipeableContainer(swipeContent).apply {
                                threshold = swipeThreshold
                            }
                            val leftNode = swipeLeftFactory?.invoke(item, sContainer)
                            val rightNode = swipeRightFactory?.invoke(item, sContainer)
                            if (leftNode != null || rightNode != null) {
                                sContainer.leftBackgroundNode = leftNode
                                sContainer.rightBackgroundNode = rightNode
                                graphic = sContainer
                                text = null
                            } else {
                                graphic = swipeContent
                                text = null
                            }
                        } else {
                            graphic = rippleWrapper
                            text = null
                        }

                        if (isRippleEnabled) {
                            RippleEffect.apply(rippleWrapper)
                        }
                    }
                }
            }
        }
    }

    private fun updateStickyHeader() {
        val skin = listView.skin as? ListViewSkin<T> ?: return
        val flow = skin.children.find { it is VirtualFlow<*> } as? VirtualFlow<*> ?: return
        
        val firstCell = flow.firstVisibleCell as? ListCell<T> ?: return
        val firstIndex = firstCell.index
        
        var currentHeaderItem: T? = null
        for (i in firstIndex downTo 0) {
            val item = listView.items.getOrNull(i)
            if (item != null && isHeader(item)) {
                currentHeaderItem = item
                break
            }
        }

        if (currentHeaderItem == null) {
            floatingHeaderContainer.children.clear()
            return
        }

        val headerNode = cellContentFactory?.invoke(currentHeaderItem) 
            ?: Label(currentHeaderItem.toString()).apply { styleClass.add("label") }
        
        val headerWrapper = AnchorPane(headerNode).apply {
            style = "-fx-background-color: white; -fx-border-color: #eeeeee; -fx-border-width: 0 0 1 0;"
            AnchorPane.setTopAnchor(headerNode, 0.0)
            AnchorPane.setBottomAnchor(headerNode, 0.0)
            AnchorPane.setLeftAnchor(headerNode, 10.0)
            AnchorPane.setRightAnchor(headerNode, 10.0)
            prefWidthProperty().bind(listView.widthProperty())
        }

        floatingHeaderContainer.children.setAll(headerWrapper)

        val visibleCells = listView.lookupAll(".list-cell")
            .filterIsInstance<ListCell<T>>()
            .filter { it.item != null && it.isVisible }
            .sortedBy { it.layoutY }

        val nextHeaderCell = visibleCells.drop(1).find { isHeader(it.item) }
        if (nextHeaderCell != null) {
            val nextHeaderTop = nextHeaderCell.localToScene(0.0, 0.0).y
            val containerTop = listView.localToScene(0.0, 0.0).y
            val distance = nextHeaderTop - containerTop
            val headerHeight = headerWrapper.boundsInLocal.height
            headerWrapper.translateY = if (distance < headerHeight) distance - headerHeight else 0.0
        } else {
            headerWrapper.translateY = 0.0
        }
    }

    private fun findVerticalScrollBar(): ScrollBar? {
        return listView.lookupAll(".scroll-bar")
            .filterIsInstance<ScrollBar>()
            .find { it.orientation == javafx.geometry.Orientation.VERTICAL }
    }

    var items: ObservableList<T>
        get() = listView.items
        set(value) {
            listView.items = value
        }

    var sensitivity: Double
        get() = behavior.sensitivity
        set(value) {
            behavior.sensitivity = value
        }

    var sensitivityX: Double
        get() = behavior.sensitivityX
        set(value) {
            behavior.sensitivityX = value
        }

    var sensitivityY: Double
        get() = behavior.sensitivityY
        set(value) {
            behavior.sensitivityY = value
        }

    var inertia: Double
        get() = behavior.inertia
        set(value) {
            behavior.inertia = value
        }

    var inertiaX: Double
        get() = behavior.inertiaX
        set(value) {
            behavior.inertiaX = value
        }

    var inertiaY: Double
        get() = behavior.inertiaY
        set(value) {
            behavior.inertiaY = value
        }

    var friction: Double
        get() = behavior.friction
        set(value) {
            behavior.friction = value
        }

    var isDirectionLockEnabled: Boolean
        get() = behavior.isDirectionLockEnabled
        set(value) {
            behavior.isDirectionLockEnabled = value
        }

    var isDynamicScrollBarVisible: Boolean
        get() = behavior.isDynamicScrollBarVisible
        set(value) {
            behavior.isDynamicScrollBarVisible = value
        }

    var isBounceEnabled: Boolean
        get() = behavior.isBounceEnabled
        set(value) {
            behavior.isBounceEnabled = value
        }

    var isBounceEnabledX: Boolean
        get() = behavior.isBounceEnabledX
        set(value) {
            behavior.isBounceEnabledX = value
        }

    var isBounceEnabledY: Boolean
        get() = behavior.isBounceEnabledY
        set(value) {
            behavior.isBounceEnabledY = value
        }

    var bounceMaxRangeX: Double
        get() = behavior.bounceMaxRangeX
        set(value) {
            behavior.bounceMaxRangeX = value
        }

    var bounceMaxRangeY: Double
        get() = behavior.bounceMaxRangeY
        set(value) {
            behavior.bounceMaxRangeY = value
        }

    var bounceRestorationX: Double
        get() = behavior.bounceRestorationX
        set(value) {
            behavior.bounceRestorationX = value
        }

    var bounceRestorationY: Double
        get() = behavior.bounceRestorationY
        set(value) {
            behavior.bounceRestorationY = value
        }

    var isSnapEnabled: Boolean
        get() = behavior.isSnapEnabled
        set(value) {
            behavior.isSnapEnabled = value
        }

    var snapUnitX: Double
        get() = behavior.snapUnitX
        set(value) {
            behavior.snapUnitX = value
        }

    var snapUnitY: Double
        get() = behavior.snapUnitY
        set(value) {
            behavior.snapUnitY = value
        }

    var snapRestoration: Double
        get() = behavior.snapRestoration
        set(value) {
            behavior.snapRestoration = value
        }

    var onRefresh: (() -> java.util.concurrent.CompletableFuture<Unit>)?
        get() = behavior.onRefresh
        set(value) {
            behavior.onRefresh = value
        }

    var refreshThreshold: Double
        get() = behavior.refreshThreshold
        set(value) {
            behavior.refreshThreshold = value
        }

    val isRefreshing: Boolean
        get() = behavior.isRefreshing

    var refreshIndicator: javafx.scene.Node?
        get() = behavior.refreshIndicator
        set(value) {
            behavior.refreshIndicator = value
        }

    fun dispose() {
        behavior.dispose()
    }
}
