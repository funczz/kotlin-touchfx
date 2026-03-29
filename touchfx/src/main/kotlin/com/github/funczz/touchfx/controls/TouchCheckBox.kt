package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.scene.control.CheckBox

/**
 * タッチ操作に適したサイズ感と視覚的フィードバックを持つチェックボックスです。
 *
 * @param text チェックボックスに表示するラベルテキスト
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchCheckBox(
    text: String? = null,
    useDefaultStyle: Boolean = true
) : CheckBox(text) {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-check-box")
        RippleEffect.apply(this)
    }

}
