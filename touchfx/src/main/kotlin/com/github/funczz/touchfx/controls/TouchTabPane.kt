package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import javafx.scene.control.TabPane

/**
 * タッチ操作に適した大型のタブヘッダーを持つ TabPane です。
 *
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchTabPane(
    useDefaultStyle: Boolean = true
) : TabPane() {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-tab-pane")
    }

}
