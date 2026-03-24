package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.behavior.TouchBehavior
import javafx.collections.ObservableList
import javafx.scene.control.ListView

/**
 * 慣性スクロール機能を持つ [ListView] のラッパーコンポーネントです。
 *
 * @param T リストアイテムの型
 * @property listView ラップされた標準の [ListView]
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class InertialListView<T>(
    val listView: ListView<T> = ListView(),
    useDefaultStyle: Boolean = true
) {

    private val behavior = TouchBehavior(listView)

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                listView.stylesheets.add(it)
            }
            listView.styleClass.add("touch-fx")
        }
    }

    /**
     * リストアイテムのリスト。
     */
    var items: ObservableList<T>
        get() = listView.items
        set(value) {
            listView.items = value
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
