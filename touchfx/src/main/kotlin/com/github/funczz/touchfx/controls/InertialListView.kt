package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.behavior.TouchBehavior
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.collections.ObservableList
import javafx.scene.control.ListCell
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

    /**
     * リストアイテムのクリック時に波紋効果 (Ripple Effect) を表示するかどうか。
     */
    var isRippleEnabled: Boolean = false
        set(value) {
            field = value
            updateCellFactory()
        }

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                listView.stylesheets.add(it)
            }
            listView.styleClass.add("touch-fx")
        }
        updateCellFactory()
    }

    private fun updateCellFactory() {
        listView.setCellFactory { _ ->
            object : ListCell<T>() {
                override fun updateItem(item: T, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                        graphic = null
                    } else {
                        text = item.toString()
                        if (isRippleEnabled) {
                            RippleEffect.apply(this)
                        }
                    }
                }
            }
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
     * スナップ（吸着）機能を有効にするかどうか。
     */
    var isSnapEnabled: Boolean
        get() = behavior.isSnapEnabled
        set(value) {
            behavior.isSnapEnabled = value
        }

    /**
     * 水平方向のスナップ単位（ピクセル）。
     */
    var snapUnitX: Double
        get() = behavior.snapUnitX
        set(value) {
            behavior.snapUnitX = value
        }

    /**
     * 垂直方向のスナップ単位（ピクセル）。
     */
    var snapUnitY: Double
        get() = behavior.snapUnitY
        set(value) {
            behavior.snapUnitY = value
        }

    /**
     * スナップ位置への復元速度。
     */
    var snapRestoration: Double
        get() = behavior.snapRestoration
        set(value) {
            behavior.snapRestoration = value
        }

    /**
     * Pull-to-Refresh 実行時のコールバック。
     */
    var onRefresh: (() -> java.util.concurrent.CompletableFuture<Unit>)?
        get() = behavior.onRefresh
        set(value) {
            behavior.onRefresh = value
        }

    /**
     * Pull-to-Refresh をキックするための Bounce しきい値（ピクセル）。
     */
    var refreshThreshold: Double
        get() = behavior.refreshThreshold
        set(value) {
            behavior.refreshThreshold = value
        }

    /**
     * 現在リフレッシュ実行中かどうか。
     */
    val isRefreshing: Boolean
        get() = behavior.isRefreshing

    /**
     * リフレッシュ中に表示されるインジケータ。
     */
    var refreshIndicator: javafx.scene.Node?
        get() = behavior.refreshIndicator
        set(value) {
            behavior.refreshIndicator = value
        }

    /**
     * 振る舞いを解除し、リソースを解放します。
     */
    fun dispose() {
        behavior.dispose()
    }
}
