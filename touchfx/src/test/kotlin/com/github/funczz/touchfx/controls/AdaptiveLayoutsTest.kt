package com.github.funczz.touchfx.controls

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start
import org.testfx.util.WaitForAsyncUtils

/**
 * [AdaptivePane], [FluidGridPane], [ResponsiveLayout] の動作を検証するテストクラス。
 */
@ExtendWith(ApplicationExtension::class)
class AdaptiveLayoutsTest {

    private lateinit var adaptivePane: AdaptivePane
    private lateinit var fluidGridPane: FluidGridPane
    private lateinit var responsiveLayout: ResponsiveLayout

    @Start
    fun start(stage: Stage) {
        adaptivePane = AdaptivePane(breakpoint = 300.0).apply {
            children.addAll(
                Rectangle(100.0, 50.0),
                Rectangle(100.0, 50.0)
            )
        }

        fluidGridPane = FluidGridPane(columnWidth = 100.0).apply {
            hgap = 10.0
            children.addAll(
                Rectangle(100.0, 50.0),
                Rectangle(100.0, 50.0),
                Rectangle(100.0, 50.0)
            )
        }

        responsiveLayout = ResponsiveLayout(breakpoint = 400.0).apply {
            navigation = Rectangle(50.0, 50.0).apply { id = "nav" }
            content = Rectangle(100.0, 100.0).apply { id = "content" }
        }

        val root = VBox(adaptivePane, fluidGridPane, responsiveLayout)
        stage.scene = Scene(root, 600.0, 800.0)
        stage.show()
    }

    /**
     * AdaptivePane がブレークポイントに応じて水平/垂直配置を切り替えることを確認します。
     */
    @Test
    fun testAdaptivePaneSwitch(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        Platform.runLater {
            adaptivePane.minWidth = 400.0
            adaptivePane.prefWidth = 400.0
            adaptivePane.maxWidth = 400.0
            adaptivePane.layout()
        }
        WaitForAsyncUtils.waitForFxEvents()
        
        val child1 = adaptivePane.children[0]
        val child2 = adaptivePane.children[1]
        assertEquals(child1.layoutY, child2.layoutY, 0.001)

        Platform.runLater {
            adaptivePane.minWidth = 200.0
            adaptivePane.prefWidth = 200.0
            adaptivePane.maxWidth = 200.0
            adaptivePane.layout()
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(child1.layoutX, child2.layoutX, 0.001)
    }

    /**
     * FluidGridPane が幅に応じて列数を変更することを確認します。
     */
    @Test
    fun testFluidGridPaneColumns(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        Platform.runLater {
            fluidGridPane.minWidth = 350.0
            fluidGridPane.prefWidth = 350.0
            fluidGridPane.maxWidth = 350.0
            fluidGridPane.layout()
        }
        WaitForAsyncUtils.waitForFxEvents()

        val child1 = fluidGridPane.children[0]
        val child2 = fluidGridPane.children[1]
        val child3 = fluidGridPane.children[2]
        assertEquals(child1.layoutY, child2.layoutY, 0.001)
        assertEquals(child1.layoutY, child3.layoutY, 0.001)

        Platform.runLater {
            fluidGridPane.minWidth = 150.0
            fluidGridPane.prefWidth = 150.0
            fluidGridPane.maxWidth = 150.0
            fluidGridPane.layout()
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(child1.layoutX, child2.layoutX, 0.001)
        assertTrue(child2.layoutY > child1.layoutY)
    }

    /**
     * ResponsiveLayout が設定された位置にナビゲーションを配置することを確認します。
     */
    @Test
    fun testResponsiveLayoutPositions(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val nav = responsiveLayout.navigation!!
        val content = responsiveLayout.content!!

        // Case 1: Wide screen (>= 400px), Side.LEFT
        Platform.runLater {
            responsiveLayout.minWidth = 500.0
            responsiveLayout.prefWidth = 500.0
            responsiveLayout.maxWidth = 500.0
            responsiveLayout.minHeight = 500.0
            responsiveLayout.prefHeight = 500.0
            responsiveLayout.widePosition = ResponsiveLayout.Side.LEFT
            responsiveLayout.layout()
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertEquals(0.0, nav.layoutX, 0.001)
        assertTrue(content.layoutX >= nav.prefWidth(-1.0))

        // Case 2: Narrow screen (< 400px), Side.BOTTOM
        Platform.runLater {
            responsiveLayout.minWidth = 300.0
            responsiveLayout.prefWidth = 300.0
            responsiveLayout.maxWidth = 300.0
            responsiveLayout.narrowPosition = ResponsiveLayout.Side.BOTTOM
            responsiveLayout.layout()
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(nav.layoutY > content.layoutY)

        // Case 3: Wide screen, Side.RIGHT
        Platform.runLater {
            responsiveLayout.minWidth = 500.0
            responsiveLayout.prefWidth = 500.0
            responsiveLayout.maxWidth = 500.0
            responsiveLayout.widePosition = ResponsiveLayout.Side.RIGHT
            responsiveLayout.layout()
        }
        WaitForAsyncUtils.waitForFxEvents()
        assertTrue(nav.layoutX > content.layoutX)

        // Case 4: Narrow screen, Side.TOP
        Platform.runLater {
            responsiveLayout.minWidth = 300.0
            responsiveLayout.prefWidth = 300.0
            responsiveLayout.maxWidth = 300.0
            // 親 VBox の制約を避けるため、高さを十分に取る
            responsiveLayout.minHeight = 600.0
            responsiveLayout.prefHeight = 600.0
            responsiveLayout.narrowPosition = ResponsiveLayout.Side.TOP
            responsiveLayout.layout()
        }
        WaitForAsyncUtils.waitForFxEvents()
        
        // 確実にレイアウトが完了しているか確認
        assertEquals(0.0, nav.layoutY, 0.001, "Nav should be at top")
        assertTrue(content.layoutY > 0.0, "Content should be shifted down by Nav. ContentY: ${content.layoutY}")
    }
}
