package com.github.funczz.touchfx.behavior

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.input.*
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@ExtendWith(ApplicationExtension::class)
class GestureBehaviorTest {

    private lateinit var root: StackPane

    @Start
    fun start(stage: Stage) {
        root = StackPane().apply {
            prefWidth = 400.0
            prefHeight = 400.0
        }
        stage.scene = Scene(root, 400.0, 400.0)
        stage.show()
    }

    /**
     * マウスによる長押し判定を検証します。
     */
    @Test
    fun testLongPressMouse(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val fired = AtomicBoolean(false)
        val pos = AtomicReference<Pair<Double, Double>>()
        
        var behavior: GestureBehavior? = null
        Platform.runLater {
            behavior = GestureBehavior(root).apply {
                longPressDuration = 500L
                onLongPress = { x, y ->
                    fired.set(true)
                    pos.set(x to y)
                }
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 100.0, 100.0, 100.0, 100.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        // 判定時間(500ms)より短く待機
        Thread.sleep(200)
        assertFalse(fired.get(), "Should not fire before duration")

        // 判定時間経過を待つ
        Thread.sleep(500)
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(fired.get(), "Should fire after duration")
        assertEquals(100.0, pos.get().first)
        assertEquals(100.0, pos.get().second)

        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * Ctrl + Scroll によるズームシミュレーションを検証します。
     */
    @Test
    fun testPinchSimulation(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val scaleFactor = AtomicReference<Double>(1.0)
        
        var behavior: GestureBehavior? = null
        Platform.runLater {
            behavior = GestureBehavior(root).apply {
                onPinch = { f ->
                    scaleFactor.set(f)
                }
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            // Ctrlキーを押しながらスクロール
            javafx.event.Event.fireEvent(root, ScrollEvent(
                ScrollEvent.SCROLL, 0.0, 0.0, 0.0, 0.0,
                false, true, false, false, true, false,
                0.0, 10.0, 0.0, 10.0,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0.0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0.0,
                0, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(scaleFactor.get() > 1.0, "Should zoom in (scale > 1.0). Current: ${scaleFactor.get()}")

        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * 実際の TouchEvent によるピンチ操作をシミュレーションします。
     */
    @Test
    fun testActualPinch(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val factors = mutableListOf<Double>()
        
        var behavior: GestureBehavior? = null
        Platform.runLater {
            behavior = GestureBehavior(root).apply {
                onPinch = { f ->
                    factors.add(f)
                }
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            // ポイント1: (100, 100), ポイント2: (200, 200) -> 距離 141.42
            val tp1 = TouchPoint(1, TouchPoint.State.PRESSED, 100.0, 100.0, 100.0, 100.0, root, null)
            val tp2 = TouchPoint(2, TouchPoint.State.PRESSED, 200.0, 200.0, 200.0, 200.0, root, null)
            val list = listOf(tp1, tp2)
            
            javafx.event.Event.fireEvent(root, TouchEvent(
                TouchEvent.TOUCH_PRESSED, tp1, list, 0,
                false, false, false, false
            ))
            
            // ポイント1: (100, 100), ポイント2: (250, 250) -> 距離 212.13 -> 拡大
            val tp1m = TouchPoint(1, TouchPoint.State.MOVED, 100.0, 100.0, 100.0, 100.0, root, null)
            val tp2m = TouchPoint(2, TouchPoint.State.MOVED, 250.0, 250.0, 250.0, 250.0, root, null)
            val listM = listOf(tp1m, tp2m)
            
            javafx.event.Event.fireEvent(root, TouchEvent(
                TouchEvent.TOUCH_MOVED, tp1m, listM, 0,
                false, false, false, false
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(factors.isEmpty(), "onPinch should be called")
        assertTrue(factors.all { it > 1.0 }, "All factors should be > 1.0 (Zoom in). Factors: $factors")

        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * 実際の TouchEvent による回転操作をシミュレーションします。
     */
    @Test
    fun testActualRotate(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val angles = mutableListOf<Double>()
        
        var behavior: GestureBehavior? = null
        Platform.runLater {
            behavior = GestureBehavior(root).apply {
                onRotate = { d ->
                    angles.add(d)
                }
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            // (100, 100) と (200, 100) -> 角度 0度
            val tp1 = TouchPoint(1, TouchPoint.State.PRESSED, 100.0, 100.0, 100.0, 100.0, root, null)
            val tp2 = TouchPoint(2, TouchPoint.State.PRESSED, 200.0, 100.0, 200.0, 100.0, root, null)
            val list = listOf(tp1, tp2)
            
            javafx.event.Event.fireEvent(root, TouchEvent(
                TouchEvent.TOUCH_PRESSED, tp1, list, 0,
                false, false, false, false
            ))
            
            // (100, 100) と (100, 200) -> 角度 90度
            val tp1m = TouchPoint(1, TouchPoint.State.MOVED, 100.0, 100.0, 100.0, 100.0, root, null)
            val tp2m = TouchPoint(2, TouchPoint.State.MOVED, 100.0, 200.0, 100.0, 200.0, root, null)
            val listM = listOf(tp1m, tp2m)
            
            javafx.event.Event.fireEvent(root, TouchEvent(
                TouchEvent.TOUCH_MOVED, tp1m, listM, 0,
                false, false, false, false
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertFalse(angles.isEmpty(), "onRotate should be called")
        // 0 -> 90 への変化なので 90 に近いはず
        assertTrue(angles.any { it > 45.0 }, "Should detect rotation. Angles: $angles")

        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }
}
