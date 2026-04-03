package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.behavior.TouchBehavior
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.control.ScrollPane
import java.util.concurrent.CompletableFuture

/**
 * 慣性スクロール機能を持つ [ScrollPane] のラッパーコンポーネントです。
 */
class InertialScrollPane(
    val scrollPane: ScrollPane = ScrollPane(),
    useDefaultStyle: Boolean = true
) {

    private val behavior = TouchBehavior(scrollPane)

    var content: Node?
        get() = scrollPane.content
        set(value) {
            scrollPane.content = value
            if (isRippleEnabled) {
                value?.let { RippleEffect.apply(it) }
            }
        }

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
        
        Platform.runLater {
            scrollPane.viewportBoundsProperty().addListener { _, _, _ ->
                // ビューポートサイズ変更時に必要であれば behavior を更新
            }
        }
    }

    var sensitivity: Double
        get() = behavior.sensitivity
        set(value) {
            behavior.sensitivity = value
        }

    var sensitivityX: Double
        get() = behavior.sensitivityX
        set(value) {
            behavior.sensitivityX = value
        }

    var sensitivityY: Double
        get() = behavior.sensitivityY
        set(value) {
            behavior.sensitivityY = value
        }

    var inertia: Double
        get() = behavior.inertia
        set(value) {
            behavior.inertia = value
        }

    var inertiaX: Double
        get() = behavior.inertiaX
        set(value) {
            behavior.inertiaX = value
        }

    var inertiaY: Double
        get() = behavior.inertiaY
        set(value) {
            behavior.inertiaY = value
        }

    var friction: Double
        get() = behavior.friction
        set(value) {
            behavior.friction = value
        }

    var isDirectionLockEnabled: Boolean
        get() = behavior.isDirectionLockEnabled
        set(value) {
            behavior.isDirectionLockEnabled = value
        }

    var isDynamicScrollBarVisible: Boolean
        get() = behavior.isDynamicScrollBarVisible
        set(value) {
            behavior.isDynamicScrollBarVisible = value
        }

    var isBounceEnabled: Boolean
        get() = behavior.isBounceEnabled
        set(value) {
            behavior.isBounceEnabled = value
        }

    var isBounceEnabledX: Boolean
        get() = behavior.isBounceEnabledX
        set(value) {
            behavior.isBounceEnabledX = value
        }

    var isBounceEnabledY: Boolean
        get() = behavior.isBounceEnabledY
        set(value) {
            behavior.isBounceEnabledY = value
        }

    var bounceMaxRangeX: Double
        get() = behavior.bounceMaxRangeX
        set(value) {
            behavior.bounceMaxRangeX = value
        }

    var bounceMaxRangeY: Double
        get() = behavior.bounceMaxRangeY
        set(value) {
            behavior.bounceMaxRangeY = value
        }

    var bounceRestorationX: Double
        get() = behavior.bounceRestorationX
        set(value) {
            behavior.bounceRestorationX = value
        }

    var bounceRestorationY: Double
        get() = behavior.bounceRestorationY
        set(value) {
            behavior.bounceRestorationY = value
        }

    var isSnapEnabled: Boolean
        get() = behavior.isSnapEnabled
        set(value) {
            behavior.isSnapEnabled = value
        }

    var snapUnitX: Double
        get() = behavior.snapUnitX
        set(value) {
            behavior.snapUnitX = value
        }

    var snapUnitY: Double
        get() = behavior.snapUnitY
        set(value) {
            behavior.snapUnitY = value
        }

    var snapRestoration: Double
        get() = behavior.snapRestoration
        set(value) {
            behavior.snapRestoration = value
        }

    var onRefresh: (() -> CompletableFuture<Unit>)?
        get() = behavior.onRefresh
        set(value) {
            behavior.onRefresh = value
        }

    var refreshThreshold: Double
        get() = behavior.refreshThreshold
        set(value) {
            behavior.refreshThreshold = value
        }

    val isRefreshing: Boolean
        get() = behavior.isRefreshing

    var refreshIndicator: Node?
        get() = behavior.refreshIndicator
        set(value) {
            behavior.refreshIndicator = value
        }

    fun dispose() {
        behavior.dispose()
    }
}
