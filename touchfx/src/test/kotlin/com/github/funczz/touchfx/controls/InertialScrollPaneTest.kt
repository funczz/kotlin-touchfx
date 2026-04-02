package com.github.funczz.touchfx.controls

import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ScrollBar
import javafx.scene.input.MouseButton
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.api.FxRobot
import org.testfx.util.WaitForAsyncUtils

@ExtendWith(ApplicationExtension::class)
class InertialScrollPaneTest : ApplicationTest() {

    private lateinit var inertialScrollPane: InertialScrollPane
    private var scrollBar: ScrollBar? = null
    private lateinit var contentArea: VBox

    override fun start(stage: Stage) {
        contentArea = VBox().apply {
            children.addAll((1..100).map { Label("Label $it") })
            setPrefSize(1000.0, 1000.0)
            style = "-fx-background-color: lightblue;"
        }
        inertialScrollPane = InertialScrollPane().apply {
            this.content = contentArea
        }
        stage.scene = Scene(inertialScrollPane.scrollPane, 200.0, 200.0)
        stage.show()

        scrollBar = inertialScrollPane.scrollPane.lookupAll(".scroll-bar")
            .filterIsInstance<ScrollBar>()
            .find { it.orientation == Orientation.VERTICAL }
        
        // 慣性移動の余地を作るためレンジを広げる
        interact {
            scrollBar?.max = 100.0
            scrollBar?.visibleAmount = 10.0
        }
    }

    @Test
    fun testDragScroll(robot: FxRobot) {
        robot.drag(contentArea).moveBy(0.0, -100.0).release(MouseButton.PRIMARY)
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            assertTrue(scrollBar!!.value > 0.0, "Drag scroll should increase scroll value")
        }
    }

    @Test
    fun testHorizontalDragScroll(robot: FxRobot) {
        val hBar = inertialScrollPane.scrollPane.lookupAll(".scroll-bar")
            .filterIsInstance<ScrollBar>()
            .find { it.orientation == Orientation.HORIZONTAL }
        
        robot.drag(contentArea).moveBy(-100.0, 0.0).release(MouseButton.PRIMARY)
        WaitForAsyncUtils.waitForFxEvents()

        interact {
            assertTrue(hBar!!.value > 0.0, "Horizontal drag scroll should increase value")
        }
    }

    @Test
    fun testInertiaScroll(robot: FxRobot) {
        interact {
            scrollBar!!.value = 0.0
            inertialScrollPane.sensitivityY = 0.01
            inertialScrollPane.inertiaY = 0.5 // 明示的な慣性設定
            inertialScrollPane.friction = 0.99
            inertialScrollPane.isDirectionLockEnabled = false
        }

        // 小刻みに移動して確実に速度を乗せる
        robot.drag(contentArea).moveBy(0.0, -50.0).moveBy(0.0, -50.0).moveBy(0.0, -50.0)
        WaitForAsyncUtils.waitForFxEvents()
        
        val valueAfterDrag = scrollBar!!.value
        robot.release(MouseButton.PRIMARY)
        
        repeat(10) { 
            WaitForAsyncUtils.waitForFxEvents()
            Thread.sleep(100) 
        }

        interact {
            assertTrue(scrollBar!!.value > valueAfterDrag, "Inertia failed. Before: $valueAfterDrag, Final: ${scrollBar!!.value}")
        }
    }

    @Test
    fun testDefaultStyle(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val scrollPane = inertialScrollPane.scrollPane
        assertTrue(scrollPane.styleClass.contains("touch-fx"), "ScrollPane should have 'touch-fx' style class")
        assertTrue(scrollPane.stylesheets.isNotEmpty(), "ScrollPane should have stylesheets applied")
    }
}
