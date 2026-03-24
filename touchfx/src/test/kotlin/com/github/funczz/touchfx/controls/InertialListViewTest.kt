package com.github.funczz.touchfx.controls

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.ScrollBar
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils

/**
 * [InertialListView] の動作を検証するテストクラス。
 */
@ExtendWith(ApplicationExtension::class)
class InertialListViewTest {

    private lateinit var inertialListView: InertialListView<String>

    /**
     * テスト用の UI をセットアップします。
     */
    @Start
    fun start(stage: Stage) {
        inertialListView = InertialListView<String>().apply {
            items.addAll((1..100).map { "Item $it" })
        }
        val root = StackPane(inertialListView.listView)
        stage.scene = Scene(root, 300.0, 400.0)
        stage.show()
    }

    /**
     * ドラッグ操作によって内部の ListView がスクロールすることを確認します。
     */
    @Test
    fun testDragScroll(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        WaitForAsyncUtils.waitForFxEvents()
        val listView = inertialListView.listView
        var scrollBar: ScrollBar? = null
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS) {
            scrollBar = listView.lookupAll(".scroll-bar")
                .filterIsInstance<ScrollBar>()
                .find { it.orientation == javafx.geometry.Orientation.VERTICAL }
            scrollBar != null && scrollBar!!.visibleAmount < scrollBar!!.max
        }
        
        assertTrue(scrollBar != null, "ScrollBar should be present")
        val initialValue = scrollBar!!.value

        Platform.runLater {
            // MousePressed
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 0.0, 200.0, 0.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))

            // MouseDragged
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 0.0, 180.0, 0.0, 180.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))

            // MouseReleased
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 0.0, 180.0, 0.0, 180.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        
        WaitForAsyncUtils.waitForFxEvents()
        assertNotEquals(initialValue, scrollBar!!.value, "ScrollBar value should change after manual drag events")
    }

    /**
     * 慣性によってスクロールが続くことを確認します。
     */
    @Test
    fun testInertiaScroll(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        WaitForAsyncUtils.waitForFxEvents()
        val listView = inertialListView.listView
        var scrollBar: ScrollBar? = null
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS) {
            scrollBar = listView.lookupAll(".scroll-bar")
                .filterIsInstance<ScrollBar>()
                .find { it.orientation == javafx.geometry.Orientation.VERTICAL }
            scrollBar != null && scrollBar!!.visibleAmount < scrollBar!!.max
        }
        
        assertTrue(scrollBar != null, "ScrollBar should be present")
        
        Platform.runLater { scrollBar!!.value = 0.5 }
        WaitForAsyncUtils.waitForFxEvents()

        Platform.runLater {
            // MousePressed
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 0.0, 200.0, 0.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        Thread.sleep(10)

        Platform.runLater {
            // MouseDragged
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 0.0, 190.0, 0.0, 190.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        Thread.sleep(10)

        Platform.runLater {
            // MouseReleased
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 0.0, 190.0, 0.0, 190.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        
        WaitForAsyncUtils.waitForFxEvents()
        val valueAfterRelease = scrollBar!!.value
        
        // 慣性移動を待機
        Thread.sleep(500)
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(Math.abs(scrollBar!!.value - valueAfterRelease) > 0.0, "Inertia should change scroll value")
    }

    /**
     * デフォルトスタイルが正しく適用されていることを確認します。
     */
    @Test
    fun testDefaultStyle(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val listView = inertialListView.listView
        assertTrue(listView.styleClass.contains("touch-fx"), "ListView should have 'touch-fx' style class")
        assertTrue(listView.stylesheets.isNotEmpty(), "ListView should have stylesheets applied")
    }
}
