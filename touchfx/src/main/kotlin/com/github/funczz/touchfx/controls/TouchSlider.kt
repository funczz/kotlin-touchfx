package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.scene.control.Slider

/**
 * タッチ操作に適したサイズ感を持つスライダーです。
 *
 * @param min 最小値
 * @param max 最大値
 * @param value 初期値
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchSlider(
    min: Double = 0.0,
    max: Double = 100.0,
    value: Double = 50.0,
    useDefaultStyle: Boolean = true
) : Slider(min, max, value) {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-slider")
        RippleEffect.apply(this)
    }

}
