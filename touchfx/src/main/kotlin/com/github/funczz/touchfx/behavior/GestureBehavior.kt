package com.github.funczz.touchfx.behavior

import javafx.animation.AnimationTimer
import javafx.application.ConditionalFeature
import javafx.application.Platform
import javafx.geometry.Point2D
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.*
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * ノードに対してジェスチャー操作 (Pinch, Rotate, Long Press) の振る舞いを提供します。
 * マルチタッチ、高次ジェスチャーイベントに加え、高度なマウスシミュレーション（Shift+Drag、モード切替）をサポートします。
 */
class GestureBehavior(private val node: Node) {

    /** ピンチ（拡大・縮小）操作が発生した際のコールバック。 */
    var onPinch: ((Double) -> Unit)? = null
    /** 回転操作が発生した際のコールバック。 */
    var onRotate: ((Double) -> Unit)? = null
    /** 長押し操作が発生した際のコールバック。 */
    var onLongPress: ((Double, Double) -> Unit)? = null

    /** 長押しと判定されるまでの時間（ミリ秒）。 */
    var longPressDuration: Long = 500L
    /** 長押し判定中に許容される移動距離（ピクセル）。 */
    var longPressThreshold: Double = 10.0

    /** マウスドラッグによるピンチシミュレーションを強制的に有効にするかどうか。 */
    var isPinchSimulationEnabled: Boolean = false
    /** マウスドラッグによる回転シミュレーションを強制的に有効にするかどうか。 */
    var isRotateSimulationEnabled: Boolean = false

    private val activePoints = ConcurrentHashMap<Int, Point2D>()
    private var initialDistance: Double = 0.0
    private var initialAngle: Double = 0.0

    private var isVirtualMultiTouch: Boolean = false
    private var virtualP1: Point2D = Point2D.ZERO

    private var longPressStartTime: Long = 0L
    private var longPressStartX: Double = 0.0
    private var longPressStartY: Double = 0.0
    private var isLongPressFired: Boolean = false

    private val longPressTimer = object : AnimationTimer() {
        override fun handle(now: Long) {
            if (longPressStartTime == 0L || isLongPressFired) return
            val elapsed = (now - longPressStartTime) / 1_000_000L
            if (elapsed >= longPressDuration) {
                onLongPress?.invoke(longPressStartX, longPressStartY)
                isLongPressFired = true
                stop()
            }
        }
    }

    companion object {
        private val sceneFilters = WeakHashMap<Scene, Boolean>()
        private val registeredBehaviors = WeakHashMap<Node, GestureBehavior>()
        
        init {
            if (System.getProperty("os.name").lowercase().contains("linux")) {
                System.setProperty("com.sun.javafx.touch", "true")
            }
        }
    }

    init {
        node.properties["com.github.funczz.touchfx.behavior.GestureBehavior"] = this
        registeredBehaviors[node] = this
        
        if (node.scene != null) registerSceneFilters(node.scene)
        node.sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) registerSceneFilters(newScene)
        }

        node.addEventFilter(MouseEvent.MOUSE_PRESSED, ::handleMousePressed)
        node.addEventFilter(MouseEvent.MOUSE_DRAGGED, ::handleMouseDragged)
        node.addEventFilter(MouseEvent.MOUSE_RELEASED, ::handleMouseReleased)
        node.addEventFilter(ScrollEvent.SCROLL, ::handleScroll)
        
        println("GestureBehavior: Attached to $node. Touch support reported by OS: ${Platform.isSupported(ConditionalFeature.INPUT_TOUCH)}")
    }

    private fun registerSceneFilters(scene: Scene) {
        if (sceneFilters.containsKey(scene)) return
        sceneFilters[scene] = true

        scene.addEventFilter(TouchEvent.ANY) { event ->
            registeredBehaviors.values.forEach { it.handleGlobalTouchEvent(event) }
        }
        scene.addEventFilter(ZoomEvent.ZOOM) { event ->
            registeredBehaviors.values.forEach { 
                if (it.isHit(event.sceneX, event.sceneY)) it.onPinch?.invoke(event.zoomFactor)
            }
        }
        scene.addEventFilter(RotateEvent.ROTATE) { event ->
            registeredBehaviors.values.forEach { 
                if (it.isHit(event.sceneX, event.sceneY)) it.onRotate?.invoke(event.angle)
            }
        }
    }

    private fun handleGlobalTouchEvent(event: TouchEvent) {
        val points = event.touchPoints
        when (event.eventType) {
            TouchEvent.TOUCH_PRESSED, TouchEvent.TOUCH_MOVED -> {
                points.forEach { tp ->
                    if (isHit(tp.sceneX, tp.sceneY) || activePoints.containsKey(tp.id)) {
                        activePoints[tp.id] = Point2D(tp.sceneX, tp.sceneY)
                    }
                }
            }
            TouchEvent.TOUCH_RELEASED -> {
                points.forEach { tp -> activePoints.remove(tp.id) }
            }
        }

        if (activePoints.size >= 2) {
            val ids = activePoints.keys().toList()
            val p1 = activePoints[ids[0]]!!
            val p2 = activePoints[ids[1]]!!
            processTwoPointGesture(p1, p2, event.eventType == TouchEvent.TOUCH_PRESSED, true, true)
            if (isHit(event.touchPoint.sceneX, event.touchPoint.sceneY)) event.consume()
        } else if (activePoints.size == 1) {
            initialDistance = 0.0
            val tp = event.touchPoint
            if (isHit(tp.sceneX, tp.sceneY)) {
                if (event.eventType == TouchEvent.TOUCH_PRESSED) startLongPress(tp.x, tp.y)
                else if (event.eventType == TouchEvent.TOUCH_MOVED) checkLongPressMove(tp.x, tp.y)
            }
        } else {
            initialDistance = 0.0
            stopLongPress()
        }
    }

    private fun handleMousePressed(event: MouseEvent) {
        if (event.isSynthesized) return
        
        if (event.isShiftDown || isPinchSimulationEnabled || isRotateSimulationEnabled) {
            val bounds = node.localToScene(node.boundsInLocal)
            virtualP1 = Point2D(bounds.minX + bounds.width / 2.0, bounds.minY + bounds.height / 2.0)
            val p2 = Point2D(event.sceneX, event.sceneY)
            isVirtualMultiTouch = true
            processTwoPointGesture(virtualP1, p2, true, event.isShiftDown || isPinchSimulationEnabled, event.isShiftDown || isRotateSimulationEnabled)
            stopLongPress()
        } else {
            startLongPress(event.x, event.y)
        }
    }

    private fun handleMouseDragged(event: MouseEvent) {
        if (isVirtualMultiTouch) {
            val p2 = Point2D(event.sceneX, event.sceneY)
            processTwoPointGesture(virtualP1, p2, false, event.isShiftDown || isPinchSimulationEnabled, event.isShiftDown || isRotateSimulationEnabled)
            event.consume()
        } else {
            checkLongPressMove(event.x, event.y)
        }
    }

    private fun handleMouseReleased(event: MouseEvent) {
        isVirtualMultiTouch = false
        initialDistance = 0.0
        stopLongPress()
    }

    private fun handleScroll(event: ScrollEvent) {
        if (!isHit(event.sceneX, event.sceneY)) return
        if (event.isControlDown) {
            onPinch?.invoke(if (event.deltaY > 0) 1.1 else 0.9)
            event.consume()
        } else if (event.isAltDown) {
            onRotate?.invoke(if (event.deltaY > 0) 5.0 else -5.0)
            event.consume()
        } else if (event.isDirect) {
            onPinch?.invoke(1.0 + (event.deltaY / 100.0))
            event.consume()
        }
    }

    private fun processTwoPointGesture(p1: Point2D, p2: Point2D, isStart: Boolean, allowPinch: Boolean, allowRotate: Boolean) {
        val dist = p1.distance(p2)
        val angle = Math.toDegrees(atan2(p2.y - p1.y, p2.x - p1.x))

        if (isStart || initialDistance == 0.0) {
            initialDistance = dist
            initialAngle = angle
        } else {
            if (allowPinch && initialDistance > 0) {
                val factor = dist / initialDistance
                if (Math.abs(factor - 1.0) > 0.001) {
                    onPinch?.invoke(factor)
                    initialDistance = dist
                }
            }
            if (allowRotate) {
                var delta = angle - initialAngle
                if (delta > 180) delta -= 360
                if (delta < -180) delta += 360
                if (Math.abs(delta) > 0.05) {
                    onRotate?.invoke(delta)
                    initialAngle = angle
                }
            }
        }
    }

    private fun isHit(sceneX: Double, sceneY: Double): Boolean =
        node.localToScene(node.boundsInLocal).contains(sceneX, sceneY)

    private fun startLongPress(x: Double, y: Double) {
        longPressStartX = x
        longPressStartY = y
        longPressStartTime = System.nanoTime()
        isLongPressFired = false
        longPressTimer.start()
    }

    private fun checkLongPressMove(x: Double, y: Double) {
        val d = sqrt(Math.pow(x - longPressStartX, 2.0) + Math.pow(y - longPressStartY, 2.0))
        if (d > longPressThreshold) stopLongPress()
    }

    private fun stopLongPress() {
        longPressTimer.stop()
        longPressStartTime = 0L
    }

    fun dispose() {
        longPressTimer.stop()
        registeredBehaviors.remove(node)
        node.properties.remove("com.github.funczz.touchfx.behavior.GestureBehavior")
    }
}
