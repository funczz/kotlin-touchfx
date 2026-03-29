package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import javafx.scene.control.Slider

/**
 * タッチ操作に適した大型のつまみを持つスライダーです。
 *
 * @param min 最小値
 * @param max 最大値
 * @param value 初期値
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchSlider(
    min: Double = 0.0,
    max: Double = 100.0,
    value: Double = 0.0,
    useDefaultStyle: Boolean = true
) : Slider(min, max, value) {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-slider")
    }

}
