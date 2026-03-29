package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane

/**
 * タッチ操作に適したサイズ感と、クリアボタンを持つテキストフィールドです。
 *
 * @param text 初期テキスト
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchTextField(
    text: String? = null,
    useDefaultStyle: Boolean = true
) : TextField(text ?: "") {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-text-field")

        // スキンが適用された後にクリアボタンを配置するなどの処理（簡易版）
        // 実際には Skin 内で管理するのが理想的だが、ここでは CSS と組み合わせて
        // 疑似的にクリアボタンを表現するための構造（後述）を検討
    }

    /**
     * 注意: 現在の実装では、クリアボタンの完全な統合には
     * カスタム Skin (TouchTextFieldSkin) が必要です。
     * フェーズ1では、まず基本サイズとスタイルのみを提供します。
     */
}
