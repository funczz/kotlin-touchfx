package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.scene.Node
import javafx.scene.control.Button

/**
 * タッチ操作に適したサイズ感と視覚的フィードバックを持つボタンです。
 *
 * @param text ボタンに表示するテキスト
 * @param graphic ボタンに表示するアイコン等のノード
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchButton(
    text: String? = null,
    graphic: Node? = null,
    useDefaultStyle: Boolean = true
) : Button(text, graphic) {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-button")
        RippleEffect.apply(this)
    }

}
