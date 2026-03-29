package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.scene.control.RadioButton

/**
 * タッチ操作に適したサイズ感と視覚的フィードバックを持つラジオボタンです。
 *
 * @param text ラジオボタンに表示するラベルテキスト
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchRadioButton(
    text: String? = null,
    useDefaultStyle: Boolean = true
) : RadioButton(text) {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-radio-button")
        RippleEffect.apply(this)
    }

}
