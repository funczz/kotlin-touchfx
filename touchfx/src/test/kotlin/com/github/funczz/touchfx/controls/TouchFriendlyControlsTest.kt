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
 * [TouchButton], [TouchCheckBox], [TouchRadioButton], [TouchComboBox], [TouchSlider] の基本動作を検証するテストクラス。
 */
@ExtendWith(ApplicationExtension::class)
class TouchFriendlyControlsTest {

    private lateinit var touchButton: TouchButton
    private lateinit var touchCheckBox: TouchCheckBox
    private lateinit var touchRadioButton: TouchRadioButton
    private lateinit var touchComboBox: TouchComboBox<String>
    private lateinit var touchSlider: TouchSlider

    /**
     * テスト用の UI をセットアップします。
     */
    @Start
    fun start(stage: Stage) {
        touchButton = TouchButton("Test Button")
        touchCheckBox = TouchCheckBox("Test CheckBox")
        touchRadioButton = TouchRadioButton("Test RadioButton")
        touchComboBox = TouchComboBox<String>().apply {
            items.addAll("Item 1", "Item 2")
        }
        touchSlider = TouchSlider(0.0, 100.0, 50.0)

        val root = VBox(10.0, touchButton, touchCheckBox, touchRadioButton, touchComboBox, touchSlider)
        stage.scene = Scene(root, 300.0, 400.0)
        stage.show()
    }

    /**
     * 各コントロールが正しいスタイルクラスを持っていることを確認します。
     */
    @Test
    fun testStyles(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        assertTrue(touchButton.styleClass.contains("touch-button"))
        assertTrue(touchCheckBox.styleClass.contains("touch-check-box"))
        assertTrue(touchRadioButton.styleClass.contains("touch-radio-button"))
        assertTrue(touchComboBox.styleClass.contains("touch-combo-box"))
        assertTrue(touchSlider.styleClass.contains("touch-slider"))
    }

    /**
     * useDefaultStyle = false の時にスタイルシートが適用されないことを確認します。
     */
    @Test
    fun testNoDefaultStyle(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        val noStyleButton = TouchButton("No Style", useDefaultStyle = false)
        assertTrue(noStyleButton.stylesheets.isEmpty())
        
        val noStyleRadio = TouchRadioButton("No Style", useDefaultStyle = false)
        assertTrue(noStyleRadio.stylesheets.isEmpty())

        val noStyleCombo = TouchComboBox<String>(useDefaultStyle = false)
        assertTrue(noStyleCombo.stylesheets.isEmpty())
    }

    /**
     * TouchCheckBox の選択状態が変化することを確認します。
     */
    @Test
    fun testTouchCheckBoxSelection(robot: FxRobot) {
        val initialState = touchCheckBox.isSelected
        robot.clickOn(touchCheckBox)
        assertTrue(touchCheckBox.isSelected != initialState)
    }

    /**
     * TouchRadioButton の選択状態が変化することを確認します。
     */
    @Test
    fun testTouchRadioButtonSelection(robot: FxRobot) {
        robot.clickOn(touchRadioButton)
        assertTrue(touchRadioButton.isSelected)
    }

    /**
     * TouchComboBox の値が変更可能であることを確認します。
     */
    @Test
    fun testTouchComboBoxValue(@Suppress("UNUSED_PARAMETER") robot: FxRobot) {
        javafx.application.Platform.runLater {
            touchComboBox.value = "Item 2"
        }
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents()
        assertEquals("Item 2", touchComboBox.value)
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
        assertEquals(newValue, touchSlider.value, 0.001)
    }
}
