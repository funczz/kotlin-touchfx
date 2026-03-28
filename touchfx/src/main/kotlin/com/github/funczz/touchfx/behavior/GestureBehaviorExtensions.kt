package com.github.funczz.touchfx.behavior

import javafx.scene.Node

/**
 * ノードにジェスチャー操作 (Pinch, Rotate, Long Press) の振る舞いを追加します。
 *
 * @param config 振る舞いの設定を行うラムダ
 * @return 作成された [GestureBehavior] インスタンス
 */
fun Node.addGestureBehavior(config: GestureBehavior.() -> Unit): GestureBehavior {
    val behavior = GestureBehavior(this)
    behavior.config()
    return behavior
}
