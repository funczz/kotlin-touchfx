package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority

/**
 * 左右に大型ボタンを配置した、タッチ操作に適した数値入力コントロールです。
 *
 * @param min 最小値
 * @param max 最大値
 * @param initial 初期値
 * @param step 増減幅
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchSpinner(
    val min: Double = 0.0,
    val max: Double = 100.0,
    initial: Double = 0.0,
    var step: Double = 1.0,
    useDefaultStyle: Boolean = true
) : HBox() {

    private val valueProperty = SimpleDoubleProperty(initial)
    var value: Double
        get() = valueProperty.get()
        set(v) = valueProperty.set(v.coerceIn(min, max))

    fun valueProperty() = valueProperty

    private val textField = TouchTextField(initial.toString(), useDefaultStyle).apply {
        isEditable = false
        alignment = Pos.CENTER
        maxWidth = 100.0
        HBox.setHgrow(this, Priority.ALWAYS)
    }

    private val decrementButton = TouchButton("-", useDefaultStyle = useDefaultStyle).apply {
        setOnAction { value -= step }
    }

    private val incrementButton = TouchButton("+", useDefaultStyle = useDefaultStyle).apply {
        setOnAction { value += step }
    }

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-spinner")
        alignment = Pos.CENTER
        spacing = 5.0

        children.addAll(decrementButton, textField, incrementButton)

        valueProperty.addListener { _, _, newValue ->
            textField.text = newValue.toString()
        }
    }
}
