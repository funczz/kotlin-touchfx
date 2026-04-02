package com.github.funczz.touchfx.behavior

import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ScrollBar
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
import java.util.concurrent.CompletableFuture
import kotlin.math.abs
import kotlin.math.round

/**
 * ノードに対してタッチ/ドラッグによる慣性スクロールの振る舞いを提供します。
 */
class TouchBehavior(private val node: Node) {

    var sensitivityX: Double = 0.005
    var sensitivityY: Double = 0.005
    var inertiaX: Double = 0.06
    var inertiaY: Double = 0.06
    var friction: Double = 0.92
    var isDirectionLockEnabled: Boolean = true
    var isDynamicScrollBarVisible: Boolean = false
        set(value) {
            field = value
            if (value) hideScrollBars() else showScrollBars()
        }

    var sensitivity: Double
        get() = (sensitivityX + sensitivityY) / 2.0
        set(value) { sensitivityX = value; sensitivityY = value }

    var inertia: Double
        get() = (inertiaX + inertiaY) / 2.0
        set(value) { inertiaX = value; inertiaY = value }

    var isBounceEnabledX: Boolean = false
        set(value) { field = value; if (!value) bounceX = 0.0; applyBounceTranslation() }
    var isBounceEnabledY: Boolean = false
        set(value) { field = value; if (!value) bounceY = 0.0; applyBounceTranslation() }
    var isBounceEnabled: Boolean
        get() = isBounceEnabledX && isBounceEnabledY
        set(value) { isBounceEnabledX = value; isBounceEnabledY = value }

    /** Bounce Effect の水平方向の最大移動距離（ピクセル）。デフォルトは無制限。 */
    var bounceMaxRangeX: Double = Double.MAX_VALUE
    /** Bounce Effect の垂直方向の最大移動距離（ピクセル）。デフォルトは無制限。 */
    var bounceMaxRangeY: Double = Double.MAX_VALUE

    var isSnapEnabled: Boolean = false
    var snapUnitX: Double = 0.0
    var snapUnitY: Double = 0.0
    var snapRestoration: Double = 0.1
    var onRefresh: (() -> CompletableFuture<Unit>)? = null
    var refreshThreshold: Double = 50.0
    var refreshIndicator: Node? = null
        set(value) { field?.isVisible = false; field = value; value?.isVisible = isRefreshing }

    var isRefreshing: Boolean = false
        private set(value) {
            field = value
            refreshIndicator?.let { 
                it.isVisible = value
                if (value) it.translateY = -it.layoutBounds.height
            }
        }

    var verticalScrollBar: ScrollBar? = null
    var horizontalScrollBar: ScrollBar? = null

    private var lastX: Double = 0.0
    private var lastY: Double = 0.0
    private var velocityX: Double = 0.0
    private var velocityY: Double = 0.0
    private var lastTime: Long = 0L
    private var startX: Double = 0.0
    private var startY: Double = 0.0
    private var lockOrientation: Orientation? = null
    private val lockThreshold: Double = 8.0
    private var bounceX: Double = 0.0
    private var bounceY: Double = 0.0
    private val bounceFriction: Double = 0.5
    private val bounceRestoration: Double = 0.2

    private val inertiaTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            val isRestoringX = abs(bounceX) > 0.05
            val isRestoringY = abs(bounceY) > 0.05
            val isMovingX = abs(velocityX) > 0.05
            val isMovingY = abs(velocityY) > 0.05
            val snapTargetX = if (isSnapEnabled && snapUnitX > 0.0 && !isMovingX) calculateSnapTarget(Orientation.HORIZONTAL) else null
            val snapTargetY = if (isSnapEnabled && snapUnitY > 0.0 && !isMovingY) calculateSnapTarget(Orientation.VERTICAL) else null
            val isSnappingX = snapTargetX != null && abs((findHorizontalScrollBarInternal()?.value ?: 0.0) - snapTargetX) > 0.0001
            val isSnappingY = snapTargetY != null && abs((findVerticalScrollBarInternal()?.value ?: 0.0) - snapTargetY) > 0.0001

            if (!isMovingX && !isMovingY && !isRestoringX && !isRestoringY && !isSnappingX && !isSnappingY) {
                if (isDynamicScrollBarVisible) hideScrollBars()
                stop()
                return
            }

            if (lockOrientation == null || lockOrientation == Orientation.VERTICAL) {
                findVerticalScrollBarInternal()?.let { scrollBar ->
                    if (isMovingY) {
                        val scale = getRawScale(scrollBar, Orientation.VERTICAL)
                        val scrollDelta = velocityY * inertiaY * scale
                        scrollBar.value = (scrollBar.value - scrollDelta).coerceIn(scrollBar.min, scrollBar.max)
                        if (isBounceEnabledY && (scrollBar.value <= scrollBar.min || scrollBar.value >= scrollBar.max)) {
                            // 上限・下限での蓄積（クランプ適用）
                            bounceY = (bounceY + velocityY * inertiaY * 100.0).coerceIn(-bounceMaxRangeY, bounceMaxRangeY)
                        }
                    } else if (snapTargetY != null) {
                        scrollBar.value += (snapTargetY - scrollBar.value) * snapRestoration
                    }
                }
            }

            if (lockOrientation == null || lockOrientation == Orientation.HORIZONTAL) {
                findHorizontalScrollBarInternal()?.let { scrollBar ->
                    if (isMovingX) {
                        val scale = getRawScale(scrollBar, Orientation.HORIZONTAL)
                        val scrollDelta = velocityX * inertiaX * scale
                        scrollBar.value = (scrollBar.value - scrollDelta).coerceIn(scrollBar.min, scrollBar.max)
                        if (isBounceEnabledX && (scrollBar.value <= scrollBar.min || scrollBar.value >= scrollBar.max)) {
                            // 左右端での蓄積（クランプ適用）
                            bounceX = (bounceX + velocityX * inertiaX * 100.0).coerceIn(-bounceMaxRangeX, bounceMaxRangeX)
                        }
                    } else if (snapTargetX != null) {
                        scrollBar.value += (snapTargetX - scrollBar.value) * snapRestoration
                    }
                }
            }

            if (!isRefreshing) {
                if (isRestoringX) bounceX *= (1.0 - bounceRestoration)
                if (isRestoringY) bounceY *= (1.0 - bounceRestoration)
                applyBounceTranslation()
            }
            velocityX *= friction
            velocityY *= friction
        }
    }

    init {
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, ::handleMousePressed)
        node.addEventFilter(MouseEvent.MOUSE_DRAGGED, ::handleMouseDragged)
        node.addEventFilter(MouseEvent.MOUSE_RELEASED, ::handleMouseReleased)
        if (isDynamicScrollBarVisible) hideScrollBars()
    }

    fun dispose() {
        inertiaTimer.stop()
        showScrollBars()
        resetBounce()
        node.removeEventFilter(MouseEvent.MOUSE_PRESSED, ::handleMousePressed)
        node.removeEventFilter(MouseEvent.MOUSE_DRAGGED, ::handleMouseDragged)
        node.removeEventFilter(MouseEvent.MOUSE_RELEASED, ::handleMouseReleased)
    }

    private fun isEventOnScrollBar(event: MouseEvent): Boolean {
        var current = event.target as? Node
        while (current != null) {
            if (current is ScrollBar) return true
            if (current == node) break
            current = current.parent
        }
        return false
    }

    private fun handleMousePressed(event: MouseEvent) {
        if (isEventOnScrollBar(event)) return
        inertiaTimer.stop()
        if (isDynamicScrollBarVisible) showScrollBars()
        lastX = event.sceneX
        lastY = event.sceneY
        startX = event.sceneX
        startY = event.sceneY
        lastTime = System.nanoTime()
        velocityX = 0.0
        velocityY = 0.0
        lockOrientation = null
    }

    private fun handleMouseDragged(event: MouseEvent) {
        if (isEventOnScrollBar(event)) return
        val now = System.nanoTime()
        val deltaX = event.sceneX - lastX
        val deltaY = event.sceneY - lastY
        val deltaTime = (now - lastTime) / 1_000_000_000.0

        if (isDirectionLockEnabled && lockOrientation == null) {
            val totalDeltaX = abs(event.sceneX - startX)
            val totalDeltaY = abs(event.sceneY - startY)
            if (totalDeltaX > lockThreshold || totalDeltaY > lockThreshold) {
                lockOrientation = if (totalDeltaX > totalDeltaY) Orientation.HORIZONTAL else Orientation.VERTICAL
            }
        }

        if (lockOrientation != null) event.consume()

        if (deltaTime > 0) {
            velocityX = if (lockOrientation == Orientation.VERTICAL) 0.0 else deltaX / deltaTime
            velocityY = if (lockOrientation == Orientation.HORIZONTAL) 0.0 else deltaY / deltaTime
        }

        lastX = event.sceneX
        lastY = event.sceneY
        lastTime = now

        if (lockOrientation == null || lockOrientation == Orientation.VERTICAL) {
            findVerticalScrollBarInternal()?.let { scrollBar ->
                val scale = getEffectiveScale(scrollBar, Orientation.VERTICAL)
                val scrollAmount = deltaY * sensitivityY * scale
                val newValue = scrollBar.value - scrollAmount
                if (isBounceEnabledY && (newValue < scrollBar.min || newValue > scrollBar.max)) {
                    // ドラッグ中の蓄積（クランプ適用）
                    bounceY = (bounceY + deltaY * bounceFriction).coerceIn(-bounceMaxRangeY, bounceMaxRangeY)
                    applyBounceTranslation()
                }
                scrollBar.value = newValue.coerceIn(scrollBar.min, scrollBar.max)
            }
        }

        if (lockOrientation == null || lockOrientation == Orientation.HORIZONTAL) {
            findHorizontalScrollBarInternal()?.let { scrollBar ->
                val scale = getEffectiveScale(scrollBar, Orientation.HORIZONTAL)
                val scrollAmount = deltaX * sensitivityX * scale
                val newValue = scrollBar.value - scrollAmount
                if (isBounceEnabledX && (newValue < scrollBar.min || newValue > scrollBar.max)) {
                    // ドラッグ中の蓄積（クランプ適用）
                    bounceX = (bounceX + deltaX * bounceFriction).coerceIn(-bounceMaxRangeX, bounceMaxRangeX)
                    applyBounceTranslation()
                }
                scrollBar.value = newValue.coerceIn(scrollBar.min, scrollBar.max)
            }
        }
    }

    private fun handleMouseReleased(event: MouseEvent) {
        if (isEventOnScrollBar(event)) return
        if (lockOrientation != null) event.consume()
        if (isBounceEnabledY && !isRefreshing && bounceY > refreshThreshold) {
            onRefresh?.let { callback ->
                isRefreshing = true
                bounceY = refreshThreshold
                applyBounceTranslation()
                callback().thenAccept {
                    Platform.runLater { isRefreshing = false; inertiaTimer.start() }
                }.exceptionally {
                    Platform.runLater { isRefreshing = false; inertiaTimer.start() }
                    null
                }
            }
        }
        inertiaTimer.start()
    }

    private fun getRawScale(scrollBar: ScrollBar, orientation: Orientation): Double {
        val viewportSize = if (orientation == Orientation.VERTICAL) (node as? Region)?.height ?: 1.0 else (node as? Region)?.width ?: 1.0
        val amount = if (scrollBar.visibleAmount > 0.0) scrollBar.visibleAmount else 1.0
        return amount / viewportSize
    }

    private fun getEffectiveScale(scrollBar: ScrollBar, orientation: Orientation): Double {
        return getRawScale(scrollBar, orientation) * 200.0
    }

    private fun findVerticalScrollBarInternal(): ScrollBar? {
        return verticalScrollBar ?: node.lookupAll(".scroll-bar").filterIsInstance<ScrollBar>().find { it.orientation == Orientation.VERTICAL }
    }

    private fun findHorizontalScrollBarInternal(): ScrollBar? {
        return horizontalScrollBar ?: node.lookupAll(".scroll-bar").filterIsInstance<ScrollBar>().find { it.orientation == Orientation.HORIZONTAL }
    }

    private fun showScrollBars() { if (node.styleClass.contains("hide-scroll-bar")) node.styleClass.remove("hide-scroll-bar") }
    private fun hideScrollBars() { if (!node.styleClass.contains("hide-scroll-bar")) node.styleClass.add("hide-scroll-bar") }

    private fun applyBounceTranslation() {
        node.translateX = bounceX
        node.translateY = bounceY
        refreshIndicator?.let { it.translateY = bounceY - it.layoutBounds.height }
    }

    private fun resetBounce() { bounceX = 0.0; bounceY = 0.0; applyBounceTranslation() }

    private fun calculateSnapTarget(orientation: Orientation): Double? {
        val scrollBar = if (orientation == Orientation.VERTICAL) findVerticalScrollBarInternal() else findHorizontalScrollBarInternal()
        val snapUnit = if (orientation == Orientation.VERTICAL) snapUnitY else snapUnitX
        if (scrollBar == null || snapUnit <= 0.0) return null
        val range = scrollBar.max - scrollBar.min
        if (range <= 0.0) return null
        val sensitivity = if (orientation == Orientation.VERTICAL) sensitivityY else sensitivityX
        val snapValueUnit = snapUnit * sensitivity * getEffectiveScale(scrollBar, orientation)
        val currentValue = scrollBar.value
        val snappedValue = round(currentValue / snapValueUnit) * snapValueUnit
        return snappedValue.coerceIn(scrollBar.min, scrollBar.max)
    }
}
