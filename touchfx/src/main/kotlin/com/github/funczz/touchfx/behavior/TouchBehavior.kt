package com.github.funczz.touchfx.behavior

import javafx.animation.AnimationTimer
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ScrollBar
import javafx.scene.input.MouseEvent
import kotlin.math.abs

/**
 * ノードに対してタッチ/ドラッグによる慣性スクロールの振る舞いを提供します。
 * 水平方向および垂直方向の両方に対応します。
 *
 * @property node 対象となる JavaFX ノード (ListView, ScrollPane など)
 */
class TouchBehavior(private val node: Node) {

    /**
     * スクロールの感度。
     */
    var sensitivity: Double = 0.005

    /**
     * 慣性の強さ。
     */
    var inertia: Double = 0.0005

    /**
     * 摩擦係数 (減速率)。0.0 から 1.0 の間で、小さいほど早く停止します。
     */
    var friction: Double = 0.92

    private var lastX: Double = 0.0
    private var lastY: Double = 0.0
    private var velocityX: Double = 0.0
    private var velocityY: Double = 0.0
    private var lastTime: Long = 0L

    private val inertiaTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            if (abs(velocityX) < 0.1 && abs(velocityY) < 0.1) {
                stop()
                return
            }

            // 垂直スクロールの更新
            findVerticalScrollBar()?.let { scrollBar ->
                val scrollDelta = velocityY * inertia
                scrollBar.value = (scrollBar.value - scrollDelta).coerceIn(scrollBar.min, scrollBar.max)
            }

            // 水平スクロールの更新
            findHorizontalScrollBar()?.let { scrollBar ->
                val scrollDelta = velocityX * inertia
                scrollBar.value = (scrollBar.value - scrollDelta).coerceIn(scrollBar.min, scrollBar.max)
            }

            velocityX *= friction
            velocityY *= friction
        }
    }

    init {
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, ::handleMousePressed)
        node.addEventFilter(MouseEvent.MOUSE_DRAGGED, ::handleMouseDragged)
        node.addEventFilter(MouseEvent.MOUSE_RELEASED, ::handleMouseReleased)
    }

    /**
     * 振る舞いを解除します。
     */
    fun dispose() {
        inertiaTimer.stop()
        node.removeEventFilter(MouseEvent.MOUSE_PRESSED, ::handleMousePressed)
        node.removeEventFilter(MouseEvent.MOUSE_DRAGGED, ::handleMouseDragged)
        node.removeEventFilter(MouseEvent.MOUSE_RELEASED, ::handleMouseReleased)
    }

    private fun handleMousePressed(event: MouseEvent) {
        inertiaTimer.stop()
        lastX = event.sceneX
        lastY = event.sceneY
        lastTime = System.nanoTime()
        velocityX = 0.0
        velocityY = 0.0
    }

    private fun handleMouseDragged(event: MouseEvent) {
        val now = System.nanoTime()
        val deltaX = event.sceneX - lastX
        val deltaY = event.sceneY - lastY
        val deltaTime = (now - lastTime) / 1_000_000_000.0 // 秒単位

        if (deltaTime > 0) {
            velocityX = deltaX / deltaTime
            velocityY = deltaY / deltaTime
        }

        lastX = event.sceneX
        lastY = event.sceneY
        lastTime = now

        // 垂直スクロールの即時反映
        findVerticalScrollBar()?.let { scrollBar ->
            val newValue = scrollBar.value - (deltaY * sensitivity)
            scrollBar.value = newValue.coerceIn(scrollBar.min, scrollBar.max)
        }

        // 水平スクロールの即時反映
        findHorizontalScrollBar()?.let { scrollBar ->
            val newValue = scrollBar.value - (deltaX * sensitivity)
            scrollBar.value = newValue.coerceIn(scrollBar.min, scrollBar.max)
        }
    }

    private fun handleMouseReleased(@Suppress("UNUSED_PARAMETER") event: MouseEvent) {
        inertiaTimer.start()
    }

    private fun findVerticalScrollBar(): ScrollBar? {
        return node.lookupAll(".scroll-bar")
            .filterIsInstance<ScrollBar>()
            .find { it.orientation == Orientation.VERTICAL }
    }

    private fun findHorizontalScrollBar(): ScrollBar? {
        return node.lookupAll(".scroll-bar")
            .filterIsInstance<ScrollBar>()
            .find { it.orientation == Orientation.HORIZONTAL }
    }
}
