package com.github.funczz.touchfx.behavior

import javafx.animation.AnimationTimer
import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.ScrollBar
import javafx.scene.input.MouseEvent
import java.util.concurrent.CompletableFuture
import kotlin.math.abs
import kotlin.math.round

/**
 * ノードに対してタッチ/ドラッグによる慣性スクロールの振る舞いを提供します。
 * 水平方向および垂直方向の両方に対応します。
 *
 * @property node 対象となる JavaFX ノード (ListView, ScrollPane など)
 */
class TouchBehavior(private val node: Node) {

    /**
     * スクロールの感度 (一括設定用)。
     */
    var sensitivity: Double
        get() = (sensitivityX + sensitivityY) / 2.0
        set(value) {
            sensitivityX = value
            sensitivityY = value
        }

    /**
     * 水平方向のスクロール感度。
     */
    var sensitivityX: Double = 0.005

    /**
     * 垂直方向のスクロール感度。
     */
    var sensitivityY: Double = 0.005

    /**
     * 慣性の強さ (一括設定用)。
     */
    var inertia: Double = 0.0005
        set(value) {
            field = value
            inertiaX = value
            inertiaY = value
        }

    /**
     * 水平方向の慣性の強さ。
     */
    var inertiaX: Double = 0.0005

    /**
     * 垂直方向の慣性の強さ。
     */
    var inertiaY: Double = 0.0005

    /**
     * 摩擦係数 (減速率)。0.0 から 1.0 の間で、小さいほど早く停止します。
     */
    var friction: Double = 0.92

    /**
     * スクロール方向のロックを有効にするかどうか。
     * true の場合、ドラッグ開始時の移動方向に基づいて水平または垂直に固定されます。
     */
    var isDirectionLockEnabled: Boolean = true

    /**
     * スクロールバーを動的に表示するかどうか。
     * true の場合、スクロール中のみ表示され、静止時は非表示になります。
     */
    var isDynamicScrollBarVisible: Boolean = false
        set(value) {
            field = value
            if (value) {
                hideScrollBars()
            } else {
                showScrollBars()
            }
        }

    /**
     * 境界での跳ね返り (Bounce) を有効にするかどうか。
     */
    var isBounceEnabled: Boolean = false
        set(value) {
            field = value
            if (!value) {
                resetBounce()
            }
        }

    /**
     * スナップ（吸着）機能を有効にするかどうか。
     */
    var isSnapEnabled: Boolean = false

    /**
     * 水平方向のスナップ単位（ピクセル）。
     */
    var snapUnitX: Double = 0.0

    /**
     * 垂直方向のスナップ単位（ピクセル）。
     */
    var snapUnitY: Double = 0.0

    /**
     * スナップ位置への復元速度。
     */
    var snapRestoration: Double = 0.1

    /**
     * Pull-to-Refresh 実行時のコールバック。
     * 完了時に返される CompletableFuture が完了すると、Bounce 状態が解除されます。
     */
    var onRefresh: (() -> CompletableFuture<Unit>)? = null

    /**
     * Pull-to-Refresh をキックするための Bounce しきい値（ピクセル）。
     */
    var refreshThreshold: Double = 50.0

    /**
     * リフレッシュ中に表示されるインジケータ。
     */
    var refreshIndicator: Node? = null
        set(value) {
            field?.isVisible = false
            field = value
            value?.isVisible = isRefreshing
        }

    /**
     * 現在リフレッシュ実行中かどうか。
     */
    var isRefreshing: Boolean = false
        private set(value) {
            field = value
            refreshIndicator?.let { indicator ->
                indicator.isVisible = value
                if (value) {
                    indicator.translateY = -indicator.layoutBounds.height // ノードの直上に配置
                }
            }
        }

    /**
     * 手動で設定された垂直スクロールバー。
     */
    var verticalScrollBar: ScrollBar? = null

    /**
     * 手動で設定された水平スクロールバー。
     */
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
            val isRestoringX = isBounceEnabled && abs(bounceX) > 0.1
            val isRestoringY = isBounceEnabled && abs(bounceY) > 0.1
            val isMovingX = abs(velocityX) > 0.1
            val isMovingY = abs(velocityY) > 0.1
            
            // スナップ位置への補正が必要かどうかの判定
            val snapTargetX = if (isSnapEnabled && snapUnitX > 0.0 && !isMovingX) calculateSnapTarget(Orientation.HORIZONTAL) else null
            val snapTargetY = if (isSnapEnabled && snapUnitY > 0.0 && !isMovingY) calculateSnapTarget(Orientation.VERTICAL) else null
            
            val isSnappingX = snapTargetX != null && abs((findHorizontalScrollBarInternal()?.value ?: 0.0) - snapTargetX) > 0.0001
            val isSnappingY = snapTargetY != null && abs((findVerticalScrollBarInternal()?.value ?: 0.0) - snapTargetY) > 0.0001

            if (!isMovingX && !isMovingY && !isRestoringX && !isRestoringY && !isSnappingX && !isSnappingY) {
                if (isDynamicScrollBarVisible) {
                    hideScrollBars()
                }
                stop()
                return
            }

            // 垂直スクロールの更新
            if (lockOrientation == null || lockOrientation == Orientation.VERTICAL) {
                findVerticalScrollBarInternal()?.let { scrollBar ->
                    if (isMovingY) {
                        val scrollDelta = velocityY * inertiaY
                        scrollBar.value = (scrollBar.value - scrollDelta).coerceIn(scrollBar.min, scrollBar.max)
                        if (isBounceEnabled && (scrollBar.value <= scrollBar.min || scrollBar.value >= scrollBar.max)) {
                            bounceY += (velocityY * inertiaY * 100.0)
                        }
                    } else if (snapTargetY != null) {
                        scrollBar.value += (snapTargetY - scrollBar.value) * snapRestoration
                    }
                }
            }

            // 水平スクロールの更新
            if (lockOrientation == null || lockOrientation == Orientation.HORIZONTAL) {
                findHorizontalScrollBarInternal()?.let { scrollBar ->
                    if (isMovingX) {
                        val scrollDelta = velocityX * inertiaX
                        scrollBar.value = (scrollBar.value - scrollDelta).coerceIn(scrollBar.min, scrollBar.max)
                        if (isBounceEnabled && (scrollBar.value <= scrollBar.min || scrollBar.value >= scrollBar.max)) {
                            bounceX += (velocityX * inertiaX * 100.0)
                        }
                    } else if (snapTargetX != null) {
                        scrollBar.value += (snapTargetX - scrollBar.value) * snapRestoration
                    }
                }
            }

            // Bounce の復元 (リフレッシュ中は復元を停止して位置を維持する)
            if (isBounceEnabled && !isRefreshing) {
                bounceX *= (1.0 - bounceRestoration)
                bounceY *= (1.0 - bounceRestoration)
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
        
        if (isDynamicScrollBarVisible) {
            hideScrollBars()
        }
    }

    /**
     * 振る舞いを解除します。
     */
    fun dispose() {
        inertiaTimer.stop()
        showScrollBars()
        resetBounce()
        node.removeEventFilter(MouseEvent.MOUSE_PRESSED, ::handleMousePressed)
        node.removeEventFilter(MouseEvent.MOUSE_DRAGGED, ::handleMouseDragged)
        node.removeEventFilter(MouseEvent.MOUSE_RELEASED, ::handleMouseReleased)
    }

    private fun handleMousePressed(event: MouseEvent) {
        event.consume()
        inertiaTimer.stop()
        if (isDynamicScrollBarVisible) {
            showScrollBars()
        }
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
        event.consume()
        val now = System.nanoTime()
        val deltaX = event.sceneX - lastX
        val deltaY = event.sceneY - lastY
        val deltaTime = (now - lastTime) / 1_000_000_000.0 // 秒単位

        // 方向ロックの判定
        if (isDirectionLockEnabled && lockOrientation == null) {
            val totalDeltaX = abs(event.sceneX - startX)
            val totalDeltaY = abs(event.sceneY - startY)
            if (totalDeltaX > lockThreshold || totalDeltaY > lockThreshold) {
                lockOrientation = if (totalDeltaX > totalDeltaY) Orientation.HORIZONTAL else Orientation.VERTICAL
            }
        }

        if (deltaTime > 0) {
            velocityX = if (lockOrientation == Orientation.VERTICAL) 0.0 else deltaX / deltaTime
            velocityY = if (lockOrientation == Orientation.HORIZONTAL) 0.0 else deltaY / deltaTime
        }

        lastX = event.sceneX
        lastY = event.sceneY
        lastTime = now

        // 垂直スクロール
        if (lockOrientation == null || lockOrientation == Orientation.VERTICAL) {
            findVerticalScrollBarInternal()?.let { scrollBar ->
                val scrollAmount = deltaY * sensitivityY
                val newValue = scrollBar.value - scrollAmount
                scrollBar.value = newValue.coerceIn(scrollBar.min, scrollBar.max)
                
                if (isBounceEnabled && (newValue < scrollBar.min || newValue > scrollBar.max)) {
                    bounceY += deltaY * bounceFriction
                    applyBounceTranslation()
                }
            }
        }

        // 水平スクロール
        if (lockOrientation == null || lockOrientation == Orientation.HORIZONTAL) {
            findHorizontalScrollBarInternal()?.let { scrollBar ->
                val scrollAmount = deltaX * sensitivityX
                val newValue = scrollBar.value - scrollAmount
                scrollBar.value = newValue.coerceIn(scrollBar.min, scrollBar.max)

                if (isBounceEnabled && (newValue < scrollBar.min || newValue > scrollBar.max)) {
                    bounceX += deltaX * bounceFriction
                    applyBounceTranslation()
                }
            }
        }
    }

    private fun handleMouseReleased(event: MouseEvent) {
        event.consume()
        
        // Pull-to-Refresh の判定 (上端で一定以上引っ張られているか)
        if (isBounceEnabled && !isRefreshing && bounceY > refreshThreshold) {
            onRefresh?.let { callback ->
                isRefreshing = true
                // 位置をリフレッシュしきい値付近で固定（少し戻す）
                bounceY = refreshThreshold
                applyBounceTranslation()
                
                callback().thenAccept {
                    Platform.runLater {
                        isRefreshing = false
                        // 復元アニメーションのためにタイマーを再開させる
                        inertiaTimer.start()
                    }
                }.exceptionally {
                    Platform.runLater { isRefreshing = false; inertiaTimer.start() }
                    null
                }
            }
        }
        
        inertiaTimer.start()
    }

    private fun findVerticalScrollBarInternal(): ScrollBar? {
        return verticalScrollBar ?: node.lookupAll(".scroll-bar")
            .filterIsInstance<ScrollBar>()
            .find { it.orientation == Orientation.VERTICAL }
    }

    private fun findHorizontalScrollBarInternal(): ScrollBar? {
        return horizontalScrollBar ?: node.lookupAll(".scroll-bar")
            .filterIsInstance<ScrollBar>()
            .find { it.orientation == Orientation.HORIZONTAL }
    }

    private fun showScrollBars() {
        if (node.styleClass.contains("hide-scroll-bar")) {
            node.styleClass.remove("hide-scroll-bar")
        }
    }

    private fun hideScrollBars() {
        if (!node.styleClass.contains("hide-scroll-bar")) {
            node.styleClass.add("hide-scroll-bar")
        }
    }

    private fun applyBounceTranslation() {
        node.translateX = bounceX
        node.translateY = bounceY
        
        // インジケータもコンテンツの移動に追従させる
        refreshIndicator?.let { indicator ->
            if (isRefreshing) {
                // リフレッシュ中は固定位置
                indicator.translateY = bounceY - indicator.layoutBounds.height
            } else {
                // ドラッグ中や復元中はコンテンツの上端に張り付く
                indicator.translateY = bounceY - indicator.layoutBounds.height
            }
        }
    }

    private fun resetBounce() {
        bounceX = 0.0
        bounceY = 0.0
        applyBounceTranslation()
    }

    private fun calculateSnapTarget(orientation: Orientation): Double? {
        val scrollBar = if (orientation == Orientation.VERTICAL) findVerticalScrollBarInternal() else findHorizontalScrollBarInternal()
        val snapUnit = if (orientation == Orientation.VERTICAL) snapUnitY else snapUnitX
        if (scrollBar == null || snapUnit <= 0.0) return null

        val range = scrollBar.max - scrollBar.min
        if (range <= 0.0) return null

        val sensitivity = if (orientation == Orientation.VERTICAL) sensitivityY else sensitivityX
        val snapValueUnit = snapUnit * sensitivity
        
        val currentValue = scrollBar.value
        val snappedValue = round(currentValue / snapValueUnit) * snapValueUnit
        
        return snappedValue.coerceIn(scrollBar.min, scrollBar.max)
    }
}
