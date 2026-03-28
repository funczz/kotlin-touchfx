package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.behavior.TouchBehavior
import com.github.funczz.touchfx.skin.RippleEffect
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

    /**
     * クリック時に波紋効果 (Ripple Effect) を表示するかどうか。
     */
    var isRippleEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                content?.let { RippleEffect.apply(it) }
            }
        }

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
            if (isRippleEnabled && value != null) {
                RippleEffect.apply(value)
            }
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
     * 水平方向の境界での跳ね返り (Bounce) を有効にするかどうか。
     */
    var isBounceEnabledX: Boolean
        get() = behavior.isBounceEnabledX
        set(value) {
            behavior.isBounceEnabledX = value
        }

    /**
     * 垂直方向の境界での跳ね返り (Bounce) を有効にするかどうか。
     */
    var isBounceEnabledY: Boolean
        get() = behavior.isBounceEnabledY
        set(value) {
            behavior.isBounceEnabledY = value
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
