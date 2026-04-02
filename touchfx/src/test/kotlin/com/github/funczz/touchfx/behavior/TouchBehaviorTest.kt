package com.github.funczz.touchfx.behavior

import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.ScrollBar
import javafx.scene.input.MouseButton
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.api.FxRobot
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.CompletableFuture
import kotlin.math.abs

@ExtendWith(ApplicationExtension::class)
class TouchBehaviorTest : ApplicationTest() {

    private lateinit var root: Region
    private lateinit var vScrollBar: ScrollBar
    private lateinit var hScrollBar: ScrollBar

    override fun start(stage: Stage) {
        root = Region().apply {
            setPrefSize(400.0, 400.0)
            style = "-fx-background-color: white;"
        }
        
        vScrollBar = ScrollBar().apply {
            orientation = Orientation.VERTICAL
            min = 0.0
            max = 1.0
            value = 0.0
            visibleAmount = 0.1
        }
        
        hScrollBar = ScrollBar().apply {
            orientation = Orientation.HORIZONTAL
            min = 0.0
            max = 1.0
            value = 0.0
            visibleAmount = 0.1
        }

        val container = StackPane(root)
        stage.scene = Scene(container, 400.0, 400.0)
        stage.show()
    }

    @Test
    fun testDragScroll(robot: FxRobot) {
        var behavior: TouchBehavior? = null
        interact {
            vScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                verticalScrollBar = vScrollBar
                isDirectionLockEnabled = false
                sensitivityY = 0.005
            }
        }
        
        robot.drag(root).moveBy(0.0, -100.0)
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            assertTrue(vScrollBar.value > 0.5, "ScrollBar value should increase")
        }
        
        robot.release(MouseButton.PRIMARY)
        interact { behavior?.dispose() }
    }

    @Test
    fun testInertiaScroll(robot: FxRobot) {
        var behavior: TouchBehavior? = null
        interact {
            vScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                verticalScrollBar = vScrollBar
                isDirectionLockEnabled = false
                inertiaY = 0.5 // 明示的な慣性設定
                friction = 0.99
            }
        }

        robot.drag(root).moveBy(0.0, -50.0).moveBy(0.0, -50.0).moveBy(0.0, -50.0)
        WaitForAsyncUtils.waitForFxEvents()
        
        val valueAfterDrag = vScrollBar.value
        robot.release(MouseButton.PRIMARY)
        
        repeat(20) { 
            WaitForAsyncUtils.waitForFxEvents()
            Thread.sleep(50) 
        }

        interact {
            assertTrue(vScrollBar.value > valueAfterDrag, "Inertia failed. AfterDrag: $valueAfterDrag, Final: ${vScrollBar.value}")
            behavior?.dispose()
        }
    }

    @Test
    fun testDirectionLock(robot: FxRobot) {
        var behavior: TouchBehavior? = null
        interact {
            vScrollBar.value = 0.5
            hScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                verticalScrollBar = vScrollBar
                horizontalScrollBar = hScrollBar
                isDirectionLockEnabled = true
            }
        }

        robot.drag(root).moveBy(0.0, -100.0)
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            assertTrue(vScrollBar.value != 0.5, "Vertical change expected")
            assertEquals(0.5, hScrollBar.value, 0.0001, "Horizontal should be locked")
            behavior?.dispose()
        }
        robot.release(MouseButton.PRIMARY)
    }

    @Test
    fun testBounceEffect(robot: FxRobot) {
        var behavior: TouchBehavior? = null
        interact {
            vScrollBar.value = 0.0
            behavior = TouchBehavior(root).apply {
                verticalScrollBar = vScrollBar
                isBounceEnabledY = true
                isDirectionLockEnabled = false
            }
        }

        robot.drag(root).moveBy(0.0, 150.0)
        WaitForAsyncUtils.waitForFxEvents()
        
        interact {
            assertTrue(root.translateY > 0.0, "translateY should be positive. Current: ${root.translateY}")
        }

        robot.release(MouseButton.PRIMARY)
        
        repeat(30) { 
            WaitForAsyncUtils.waitForFxEvents()
            Thread.sleep(50) 
        }
        
        interact {
            assertEquals(0.0, root.translateY, 2.0, "Should return to near 0")
            behavior?.dispose()
        }
    }

    @Test
    fun testDirectionalBounce(robot: FxRobot) {
        var behavior: TouchBehavior? = null
        interact {
            behavior = TouchBehavior(root).apply {
                verticalScrollBar = vScrollBar
                horizontalScrollBar = hScrollBar
                isBounceEnabledX = true
                isBounceEnabledY = false
                isDirectionLockEnabled = false
            }
        }

        robot.drag(root).moveBy(150.0, 150.0)
        WaitForAsyncUtils.waitForFxEvents()
        
        interact {
            assertTrue(root.translateX > 0.0, "translateX should be positive. Current: ${root.translateX}")
            assertEquals(0.0, root.translateY, 0.0001, "translateY should be 0")
        }

        robot.release(MouseButton.PRIMARY)
        
        repeat(30) { 
            WaitForAsyncUtils.waitForFxEvents()
            Thread.sleep(50) 
        }

        interact {
            assertEquals(0.0, root.translateX, 2.0, "translateX should return to near 0")
            behavior?.dispose()
        }
    }

    @Test
    fun testSnapping(robot: FxRobot) {
        var behavior: TouchBehavior? = null
        interact {
            vScrollBar.value = 0.5
            behavior = TouchBehavior(root).apply {
                isDirectionLockEnabled = false
                isSnapEnabled = true
                verticalScrollBar = vScrollBar
                snapUnitY = 20.0 
                sensitivityY = 0.005 
            }
        }

        robot.drag(root).moveBy(0.0, -15.0).release(MouseButton.PRIMARY)
        
        repeat(15) { 
            WaitForAsyncUtils.waitForFxEvents()
            Thread.sleep(100) 
        }

        interact {
            val finalValue = vScrollBar.value
            val remainder = abs(finalValue % 0.005)
            assertTrue(remainder < 0.0001 || abs(remainder - 0.005) < 0.0001, "Snap mismatch. Value: $finalValue")
            behavior?.dispose()
        }
    }

    @Test
    fun testPullToRefresh(robot: FxRobot) {
        var behavior: TouchBehavior? = null
        val refreshFuture = CompletableFuture<Unit>()
        var refreshCalled = false

        interact {
            vScrollBar.value = 0.0
            behavior = TouchBehavior(root).apply {
                isBounceEnabled = true
                verticalScrollBar = vScrollBar
                refreshThreshold = 20.0 
                isDirectionLockEnabled = false
                onRefresh = {
                    refreshCalled = true
                    refreshFuture
                }
            }
        }

        robot.drag(root).moveBy(0.0, 200.0).release(MouseButton.PRIMARY)
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            assertTrue(refreshCalled, "onRefresh failed")
            assertTrue(behavior!!.isRefreshing, "isRefreshing failed")
            refreshFuture.complete(Unit)
        }

        repeat(15) { 
            WaitForAsyncUtils.waitForFxEvents()
            Thread.sleep(100) 
        }

        interact {
            assertFalse(behavior!!.isRefreshing, "Should stop refreshing")
            behavior?.dispose()
        }
    }
}
