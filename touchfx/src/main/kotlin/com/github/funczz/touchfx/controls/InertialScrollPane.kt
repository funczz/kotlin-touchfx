package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.behavior.TouchBehavior
import javafx.scene.Node
import javafx.scene.control.ScrollPane

/**
 * 慣性スクロール機能を持つ [ScrollPane] のラッパーコンポーネントです。
 *
 * @property scrollPane ラップされた標準の [ScrollPane]
 */
class InertialScrollPane(val scrollPane: ScrollPane = ScrollPane()) {

    private val behavior = TouchBehavior(scrollPane)

    /**
     * スクロールペインの内容。
     */
    var content: Node?
        get() = scrollPane.content
        set(value) {
            scrollPane.content = value
        }

    /**
     * スクロールの感度。
     */
    var sensitivity: Double
        get() = behavior.sensitivity
        set(value) {
            behavior.sensitivity = value
        }

    /**
     * 慣性の強さ。
     */
    var inertia: Double
        get() = behavior.inertia
        set(value) {
            behavior.inertia = value
        }

    /**
     * 摩擦係数。
     */
    var friction: Double
        get() = behavior.friction
        set(value) {
            behavior.friction = value
        }

    /**
     * 振る舞いを解除し、リソースを解放します。
     */
    fun dispose() {
        behavior.dispose()
    }
}
