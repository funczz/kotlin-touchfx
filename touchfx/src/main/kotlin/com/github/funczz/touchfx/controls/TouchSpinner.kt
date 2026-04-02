package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.util.converter.DoubleStringConverter

/**
 * タッチ操作に適したサイズ感を持つ Spinner (数値入力) です。
 *
 * @param min 最小値
 * @param max 最大値
 * @param initial 初期値
 * @param step ステップ量
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchSpinner(
    val min: Double = 0.0,
    val max: Double = 100.0,
    val initial: Double = 0.0,
    val step: Double = 1.0,
    useDefaultStyle: Boolean = true
) : HBox() {

    private val textField = TextField(initial.toString()).apply {
        styleClass.add("touch-spinner-field")
        alignment = Pos.CENTER
        HBox.setHgrow(this, Priority.ALWAYS)
        
        // 数値のみ入力を許可するフォーマッタ
        textFormatter = TextFormatter(DoubleStringConverter())
    }

    private val incrementButton = Button("+").apply {
        styleClass.addAll("touch-spinner-button", "increment")
        setOnAction { updateValue(step) }
    }

    private val decrementButton = Button("-").apply {
        styleClass.addAll("touch-spinner-button", "decrement")
        setOnAction { updateValue(-step) }
    }

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-spinner")
        RippleEffect.apply(this)

        alignment = Pos.CENTER
        spacing = 5.0

        children.addAll(decrementButton, textField, incrementButton)
    }

    private fun updateValue(delta: Double) {
        val current = textField.text.toDoubleOrNull() ?: initial
        val next = (current + delta).coerceIn(min, max)
        textField.text = next.toString()
    }

    /** 現在の値を取得します。 */
    var value: Double
        get() = textField.text.toDoubleOrNull() ?: initial
        set(value) { textField.text = value.coerceIn(min, max).toString() }

}
