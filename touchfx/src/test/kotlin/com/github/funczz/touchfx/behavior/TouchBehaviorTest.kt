package com.github.funczz.touchfx.behavior

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.ScrollBar
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils
import javafx.scene.input.MouseEvent
import kotlin.math.abs

/**
 * [TouchBehavior] の基本動作を検証するテストクラス。

 * スクロールバーを直接注入することで、標準コントロールの干渉を排除してロジックを検証します。
 */
@ExtendWith(ApplicationExtension::class)
class TouchBehaviorTest {

    private lateinit var root: StackPane
    private lateinit var vScrollBar: ScrollBar
    private lateinit var hScrollBar: ScrollBar

    /**
     * テスト用の UI をセットアップします。
     */
    @Start
    fun start(stage: Stage) {
        vScrollBar = ScrollBar().apply {
            orientation = Orientation.VERTICAL
            min = 0.0
            max = 1.0
            value = 0.5
        }
        hScrollBar = ScrollBar().apply {
            orientation = Orientation.HORIZONTAL
            min = 0.0
            max = 1.0
            value = 0.5
        }
        root = StackPane().apply {
            children.addAll(vScrollBar, hScrollBar)
        }
        stage.scene = Scene(root, 400.0, 400.0)
        stage.show()
    }

    /**
     * ドラッグ操作によってスクロールバーの値が変化することを確認します。
     */
    @Test
    fun testDragScroll(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        var behavior: TouchBehavior? = null
        Platform.runLater {
            vScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                isDirectionLockEnabled = false
                verticalScrollBar = vScrollBar
                sensitivity = 0.01 // 10px = 0.1 value
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 200.0, 210.0, 200.0, 210.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 200.0, 210.0, 200.0, 210.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(vScrollBar.value < 0.5, "Value should decrease from 0.5. Current: ${vScrollBar.value}")
        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * 方向ロック機能を検証します。
     */
    @Test
    fun testDirectionLock(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        var behavior: TouchBehavior? = null
        Platform.runLater {
            vScrollBar.value = 0.5
            hScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                isDirectionLockEnabled = true
                verticalScrollBar = vScrollBar
                horizontalScrollBar = hScrollBar
                sensitivity = 0.01
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 199.0, 220.0, 199.0, 220.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 199.0, 220.0, 199.0, 220.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        
        assertTrue(vScrollBar.value < 0.5, "Vertical should scroll")
        assertEquals(0.5, hScrollBar.value, 0.001, "Horizontal should be locked at 0.5")
        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * スクロールバーの動的表示機能を検証します。
     */
    @Test
    fun testDynamicScrollBarVisibility(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        var behavior: TouchBehavior? = null
        Platform.runLater {
            behavior = TouchBehavior(root).apply {
                isDynamicScrollBarVisible = true
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(root.styleClass.contains("hide-scroll-bar"))

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertFalse(root.styleClass.contains("hide-scroll-bar"))

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        
        Thread.sleep(2000)
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(root.styleClass.contains("hide-scroll-bar"))
        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * 境界での跳ね返り (Bounce) 機能を検証します。
     */
    @Test
    fun testBounceEffect(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        var behavior: TouchBehavior? = null
        Platform.runLater {
            vScrollBar.value = 0.0
            behavior = TouchBehavior(root).apply {
                isDirectionLockEnabled = false
                isBounceEnabled = true
                verticalScrollBar = vScrollBar
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 200.0, 300.0, 200.0, 300.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        
        assertTrue(root.translateY > 0.0)

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 200.0, 300.0, 200.0, 300.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Thread.sleep(2500)
        WaitForAsyncUtils.waitForFxEvents()
        
        assertEquals(0.0, root.translateY, 0.5)
        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * 方向別の感度設定を検証します。
     */
    @Test
    fun testDirectionalSensitivity(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        var behavior: TouchBehavior? = null
        Platform.runLater {
            vScrollBar.value = 0.5
            hScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                isDirectionLockEnabled = false
                verticalScrollBar = vScrollBar
                horizontalScrollBar = hScrollBar
                sensitivityX = 0.01 // 10px = 0.1
                sensitivityY = 0.001 // 10px = 0.01
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 190.0, 190.0, 190.0, 190.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        val deltaH = Math.abs(hScrollBar.value - 0.5)
        val deltaV = Math.abs(vScrollBar.value - 0.5)
        
        assertTrue(deltaH > deltaV, "Horizontal scroll ($deltaH) should be larger than vertical ($deltaV)")

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 190.0, 190.0, 190.0, 190.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * ドラッグ後の慣性によってスクロールが続くことを確認します。
     */
    @Test
    fun testInertiaScroll(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        var behavior: TouchBehavior? = null
        Platform.runLater {
            vScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                isDirectionLockEnabled = false
                verticalScrollBar = vScrollBar
                sensitivity = 0.01
                inertia = 0.01
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 200.0, 210.0, 200.0, 210.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        val valueAtRelease = vScrollBar.value

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 200.0, 210.0, 200.0, 210.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        Thread.sleep(1000)
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(vScrollBar.value < valueAtRelease, "Inertia should continue scrolling. Released at: $valueAtRelease, Final: ${vScrollBar.value}")
        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * スナップ（吸着）機能を検証します。
     */
    @Test
    fun testSnapping(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        var behavior: TouchBehavior? = null
        Platform.runLater {
            vScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                isDirectionLockEnabled = false
                isSnapEnabled = true
                verticalScrollBar = vScrollBar
                snapUnitY = 10.0 // 10px 単位のスナップ
                sensitivityY = 0.01 // 10px = 0.1 value. よって 0.1 刻みでスナップされるはず
            }
        }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
            // 5px 下方向へドラッグ (deltaY = 5) -> 値は 0.5 - 0.05 = 0.45 になる
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 200.0, 205.0, 200.0, 205.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
            // 指を離す。速度が小さいため即座にスナップフェーズへ。
            // 0.45 から最も近い 0.1 刻みの位置は 0.4 または 0.5。
            // ここでは 0.45 なので四捨五入の挙動に依存するが、スナップが効くことを確認。
            javafx.event.Event.fireEvent(root, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 200.0, 205.0, 200.0, 205.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()

        // スナップアニメーションを待つ
        Thread.sleep(1000)
        WaitForAsyncUtils.waitForFxEvents()

        val finalValue = vScrollBar.value
        // 0.1 刻みになっていることを確認 (0.4 または 0.5)
        val remainder = abs(finalValue % 0.1)
        assertTrue(remainder < 0.001 || abs(remainder - 0.1) < 0.001, "Final value should be a multiple of 0.1. Current: $finalValue")
        
        Platform.runLater { behavior?.dispose() }
        WaitForAsyncUtils.waitForFxEvents()
    }
}
