package com.github.funczz.touchfx.behavior

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.scene.control.ScrollBar
import javafx.scene.control.ScrollPane
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Region
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
 * [TouchBehavior] の基本動作を検証するテストクラス。
 */
@ExtendWith(ApplicationExtension::class)
class TouchBehaviorTest {

    private lateinit var listView: ListView<String>
    private lateinit var scrollPane: ScrollPane
    private lateinit var behavior: TouchBehavior

    /**
     * テスト用の UI をセットアップします。
     */
    @Start
    fun start(stage: Stage) {
        listView = ListView<String>().apply {
            items.addAll((1..100).map { "Item $it" })
        }
        scrollPane = ScrollPane().apply {
            content = Region().apply {
                minWidth = 1000.0
                minHeight = 1000.0
            }
        }
        val root = StackPane(listView, scrollPane)
        listView.isVisible = false
        scrollPane.isVisible = false
        stage.scene = Scene(root, 300.0, 400.0)
        stage.show()

        behavior = TouchBehavior(listView)
    }

    /**
     * ドラッグ操作によってスクロールバーの値が変化することを確認します。
     */
    @Test
    fun testDragScroll(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        Platform.runLater {
            listView.isVisible = true
            scrollPane.isVisible = false
            behavior.dispose()
            behavior = TouchBehavior(listView)
        }
        WaitForAsyncUtils.waitForFxEvents()

        var scrollBar: ScrollBar? = null
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS) {
            scrollBar = listView.lookupAll(".scroll-bar")
                .filterIsInstance<ScrollBar>()
                .find { it.orientation == Orientation.VERTICAL }
            scrollBar != null && scrollBar!!.visibleAmount < scrollBar!!.max
        }

        assertTrue(scrollBar != null, "ScrollBar should be present")
        val initialValue = scrollBar!!.value

        Platform.runLater {
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))

            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 200.0, 180.0, 200.0, 180.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))

            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 200.0, 180.0, 200.0, 180.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }

        WaitForAsyncUtils.waitForFxEvents()
        assertNotEquals(initialValue, scrollBar!!.value, "ScrollBar value should change after manual drag events")
    }

    /**
     * 水平ドラッグ操作によってスクロールバーの値が変化することを確認します。
     */
    @Test
    fun testHorizontalDragScroll(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        Platform.runLater {
            listView.isVisible = false
            scrollPane.isVisible = true
            behavior.dispose()
            behavior = TouchBehavior(scrollPane)
        }
        WaitForAsyncUtils.waitForFxEvents()

        var scrollBar: ScrollBar? = null
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS) {
            scrollBar = scrollPane.lookupAll(".scroll-bar")
                .filterIsInstance<ScrollBar>()
                .find { it.orientation == Orientation.HORIZONTAL }
            scrollBar != null
        }

        assertTrue(scrollBar != null, "Horizontal ScrollBar should be present")
        val initialValue = scrollBar!!.value

        Platform.runLater {
            javafx.event.Event.fireEvent(scrollPane, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))

            javafx.event.Event.fireEvent(scrollPane, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 180.0, 200.0, 180.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))

            javafx.event.Event.fireEvent(scrollPane, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 180.0, 200.0, 180.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }

        WaitForAsyncUtils.waitForFxEvents()
        assertNotEquals(initialValue, scrollBar!!.value, "Horizontal ScrollBar value should change after manual drag events")
    }

    /**
     * ドラッグ後の慣性によって垂直スクロールが続くことを確認します。
     */
    @Test
    fun testVerticalInertiaScroll(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        Platform.runLater {
            listView.isVisible = true
            scrollPane.isVisible = false
            behavior.dispose()
            behavior = TouchBehavior(listView)
        }
        WaitForAsyncUtils.waitForFxEvents()

        var scrollBar: ScrollBar? = null
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS) {
            scrollBar = listView.lookupAll(".scroll-bar")
                .filterIsInstance<ScrollBar>()
                .find { it.orientation == Orientation.VERTICAL }
            scrollBar != null && scrollBar!!.visibleAmount < scrollBar!!.max
        }

        assertTrue(scrollBar != null, "ScrollBar should be present")

        Platform.runLater { scrollBar!!.value = 0.5 }
        WaitForAsyncUtils.waitForFxEvents()

        behavior.sensitivity = 0.005
        behavior.inertia = 0.0005

        Platform.runLater {
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        Thread.sleep(20)

        Platform.runLater {
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 200.0, 190.0, 200.0, 190.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        Thread.sleep(20)

        Platform.runLater {
            javafx.event.Event.fireEvent(listView, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 200.0, 190.0, 200.0, 190.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }

        WaitForAsyncUtils.waitForFxEvents()
        val valueAfterRelease = scrollBar!!.value

        Thread.sleep(500)
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(
            Math.abs(scrollBar!!.value - valueAfterRelease) > 0.0,
            "Vertical Inertia should change scroll value (after release: $valueAfterRelease, current: ${scrollBar!!.value})"
        )
    }

    /**
     * ドラッグ後の慣性によって水平スクロールが続くことを確認します。
     */
    @Test
    fun testHorizontalInertiaScroll(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        Platform.runLater {
            listView.isVisible = false
            scrollPane.isVisible = true
            behavior.dispose()
            behavior = TouchBehavior(scrollPane)
        }
        WaitForAsyncUtils.waitForFxEvents()

        var scrollBar: ScrollBar? = null
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS) {
            scrollBar = scrollPane.lookupAll(".scroll-bar")
                .filterIsInstance<ScrollBar>()
                .find { it.orientation == Orientation.HORIZONTAL }
            scrollBar != null
        }

        assertTrue(scrollBar != null, "Horizontal ScrollBar should be present")

        Platform.runLater { scrollBar!!.value = 0.5 }
        WaitForAsyncUtils.waitForFxEvents()

        behavior.sensitivity = 0.005
        behavior.inertia = 0.0005

        Platform.runLater {
            javafx.event.Event.fireEvent(scrollPane, MouseEvent(
                MouseEvent.MOUSE_PRESSED, 200.0, 200.0, 200.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        Thread.sleep(20)

        Platform.runLater {
            javafx.event.Event.fireEvent(scrollPane, MouseEvent(
                MouseEvent.MOUSE_DRAGGED, 190.0, 200.0, 190.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }
        WaitForAsyncUtils.waitForFxEvents()
        Thread.sleep(20)

        Platform.runLater {
            javafx.event.Event.fireEvent(scrollPane, MouseEvent(
                MouseEvent.MOUSE_RELEASED, 190.0, 200.0, 190.0, 200.0,
                javafx.scene.input.MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null
            ))
        }

        WaitForAsyncUtils.waitForFxEvents()
        val valueAfterRelease = scrollBar!!.value

        Thread.sleep(500)
        WaitForAsyncUtils.waitForFxEvents()

        assertTrue(
            Math.abs(scrollBar!!.value - valueAfterRelease) > 0.0,
            "Horizontal Inertia should change scroll value (after release: $valueAfterRelease, current: ${scrollBar!!.value})"
        )
    }
}
