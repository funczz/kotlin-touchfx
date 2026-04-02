package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.scene.control.TextField

/**
 * タッチ操作に適したサイズ感を持つ TextField です。
 *
 * @param text 初期テキスト
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchTextField(
    text: String? = null,
    useDefaultStyle: Boolean = true
) : TextField(text) {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-text-field")
        RippleEffect.apply(this)

        // スキンが適用された後にクリアボタンを配置するなどの処理（簡易版）
        // 実際には Skin 内で管理するのが理想的だが、ここでは CSS と組み合わせて
        // 疑似的にクリアボタンを表現するための構造（後述）を検討
    }

}
