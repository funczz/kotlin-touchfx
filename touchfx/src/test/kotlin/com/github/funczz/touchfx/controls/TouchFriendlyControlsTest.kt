package com.github.funczz.touchfx.controls

import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.testfx.api.FxRobot
import org.testfx.framework.junit5.ApplicationExtension
import org.testfx.framework.junit5.Start

/**
 * [TouchButton], [TouchCheckBox], [TouchSlider] の基本動作を検証するテストクラス。
 */
@ExtendWith(ApplicationExtension::class)
class TouchFriendlyControlsTest {

    private lateinit var touchButton: TouchButton
    private lateinit var touchCheckBox: TouchCheckBox
    private lateinit var touchSlider: TouchSlider

    /**
     * テスト用の UI をセットアップします。
     */
    @Start
    fun start(stage: Stage) {
        touchButton = TouchButton("Test Button")
        touchCheckBox = TouchCheckBox("Test CheckBox")
        touchSlider = TouchSlider(0.0, 100.0, 50.0)

        val root = VBox(10.0, touchButton, touchCheckBox, touchSlider)
        stage.scene = Scene(root, 300.0, 300.0)
        stage.show()
    }

    /**
     * TouchButton が正しいスタイルクラスを持っていることを確認します。
     */
    @Test
    fun testTouchButtonStyle(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        assertTrue(touchButton.styleClass.contains("touch-button"), "TouchButton should have 'touch-button' style class")
    }

    /**
     * TouchCheckBox が正しいスタイルクラスを持っていることを確認します。
     */
    @Test
    fun testTouchCheckBoxStyle(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        assertTrue(touchCheckBox.styleClass.contains("touch-check-box"), "TouchCheckBox should have 'touch-check-box' style class")
    }

    /**
     * TouchSlider が正しいスタイルクラスを持っていることを確認します。
     */
    @Test
    fun testTouchSliderStyle(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        assertTrue(touchSlider.styleClass.contains("touch-slider"), "TouchSlider should have 'touch-slider' style class")
    }

    /**
     * TouchCheckBox の選択状態が変化することを確認します。
     */
    @Test
    fun testTouchCheckBoxSelection(robot: FxRobot) {
        val initialState = touchCheckBox.isSelected
        robot.clickOn(touchCheckBox)
        assertTrue(touchCheckBox.isSelected != initialState, "CheckBox selection state should change after click")
    }

    /**
     * TouchSlider の値が変更可能であることを確認します。
     */
    @Test
    fun testTouchSliderValue(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val newValue = 75.0
        
        javafx.application.Platform.runLater {
            touchSlider.value = newValue
        }
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents()
        
        assertEquals(newValue, touchSlider.value, 0.001, "Slider value should be updated")
    }
}
