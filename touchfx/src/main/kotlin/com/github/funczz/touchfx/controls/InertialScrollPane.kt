package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.behavior.TouchBehavior
import javafx.scene.Node
import javafx.scene.control.ScrollPane

/**
 * 慣性スクロール機能を持つ [ScrollPane] のラッパーコンポーネントです。
 *
 * @property scrollPane ラップされた標準の [ScrollPane]
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class InertialScrollPane(
    val scrollPane: ScrollPane = ScrollPane(),
    useDefaultStyle: Boolean = true
) {

    private val behavior = TouchBehavior(scrollPane)

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                scrollPane.stylesheets.add(it)
            }
            scrollPane.styleClass.add("touch-fx")
        }
    }

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
     * 水平方向のスクロール感度。
     */
    var sensitivityX: Double
        get() = behavior.sensitivityX
        set(value) {
            behavior.sensitivityX = value
        }

    /**
     * 垂直方向のスクロール感度。
     */
    var sensitivityY: Double
        get() = behavior.sensitivityY
        set(value) {
            behavior.sensitivityY = value
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
     * 水平方向の慣性の強さ。
     */
    var inertiaX: Double
        get() = behavior.inertiaX
        set(value) {
            behavior.inertiaX = value
        }

    /**
     * 垂直方向の慣性の強さ。
     */
    var inertiaY: Double
        get() = behavior.inertiaY
        set(value) {
            behavior.inertiaY = value
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
     * スクロール方向のロックを有効にするかどうか。
     */
    var isDirectionLockEnabled: Boolean
        get() = behavior.isDirectionLockEnabled
        set(value) {
            behavior.isDirectionLockEnabled = value
        }

    /**
     * スクロールバーを動的に表示するかどうか。
     */
    var isDynamicScrollBarVisible: Boolean
        get() = behavior.isDynamicScrollBarVisible
        set(value) {
            behavior.isDynamicScrollBarVisible = value
        }

    /**
     * 境界での跳ね返り (Bounce) を有効にするかどうか。
     */
    var isBounceEnabled: Boolean
        get() = behavior.isBounceEnabled
        set(value) {
            behavior.isBounceEnabled = value
        }

    /**
     * 振る舞いを解除し、リソースを解放します。
     */
    fun dispose() {
        behavior.dispose()
    }
}
