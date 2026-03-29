package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.skin.RippleEffect
import javafx.scene.control.CheckBox

/**
 * タッチ操作に適したサイズ感と視覚的フィードバックを持つチェックボックスです。
 *
 * @param text チェックボックスに表示するラベルテキスト
 */
class TouchCheckBox(text: String? = null) : CheckBox(text) {

    init {
        styleClass.add("touch-check-box")
        RippleEffect.apply(this)
    }

}
