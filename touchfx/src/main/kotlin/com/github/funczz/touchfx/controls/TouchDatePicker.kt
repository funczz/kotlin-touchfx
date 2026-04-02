package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.application.Platform
import javafx.scene.control.DatePicker
import java.time.LocalDate

/**
 * タッチ操作に適したサイズ感を持つ DatePicker です。
 *
 * @param value 初期値
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchDatePicker(
    value: LocalDate? = null,
    useDefaultStyle: Boolean = true
) : DatePicker(value) {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-date-picker")
        RippleEffect.apply(this)

        // ポップアップが表示された際に、ポップアップ側のスタイルを適用する
        showingProperty().addListener { _, _, isShowing ->
            if (isShowing) {
                Platform.runLater {
                    val popup = skin?.node?.lookup(".date-picker-popup") ?: return@runLater
                    popup.styleClass.add("touch-date-picker-popup")
                }
            }
        }
    }

}
