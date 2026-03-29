package com.github.funczz.touchfx.controls

import javafx.scene.control.Slider

/**
 * タッチ操作に適した大型のつまみを持つスライダーです。
 *
 * @param min 最小値
 * @param max 最大値
 * @param value 初期値
 */
class TouchSlider(min: Double = 0.0, max: Double = 100.0, value: Double = 0.0) : Slider(min, max, value) {

    init {
        styleClass.add("touch-slider")
    }

}
