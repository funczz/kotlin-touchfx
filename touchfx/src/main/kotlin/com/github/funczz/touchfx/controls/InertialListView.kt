package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.behavior.TouchBehavior
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane

/**
 * 慣性スクロール機能を持つ [ListView] のラッパーコンポーネントです。
 * リストアイテムのスワイプアクションにも対応しています。
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
            if (field != value) {
                field = value
                updateCellFactory()
            }
        }

    /**
     * 右側にスワイプした際に表示されるノードを生成するファクトリ。
     * 引数: (アイテム, SwipeableContainer)
     */
    var swipeLeftFactory: ((T, SwipeableContainer) -> Node)? = null
        set(value) {
            field = value
            updateCellFactory()
        }

    /**
     * 左側にスワイプした際に表示されるノードを生成するファクトリ。
     * 引数: (アイテム, SwipeableContainer)
     */
    var swipeRightFactory: ((T, SwipeableContainer) -> Node)? = null
        set(value) {
            field = value
            updateCellFactory()
        }

    /**
     * スワイプアクションを確定させるしきい値（ピクセル）。
     */
    var swipeThreshold: Double = 50.0
        set(value) {
            field = value
            updateCellFactory()
        }

    /**
     * セルのメインコンテンツを生成するファクトリ。
     * デフォルトでは Label によるテキスト表示が行われます。
     * スワイプ機能や Ripple 効果はこのカスタムノードを包む形で適用されます。
     */
    var cellContentFactory: ((T) -> Node)? = null
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
                private var swipeContainer: SwipeableContainer? = null
                private var currentContent: Node? = null

                override fun updateItem(item: T, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                        graphic = null
                        swipeContainer = null
                        currentContent = null
                    } else {
                        // コンテンツの取得
                        val content = cellContentFactory?.invoke(item) ?: Label(item.toString()).apply {
                            maxWidth = Double.MAX_VALUE
                            styleClass.add("label")
                        }
                        currentContent = content

                        // スワイプ機能が有効な場合、SwipeableContainer でラップする
                        if (swipeLeftFactory != null || swipeRightFactory != null) {
                            val wrapper = AnchorPane(content).apply {
                                style = "-fx-background-color: white;" // セルの背景を不透明に
                                AnchorPane.setTopAnchor(content, 0.0)
                                AnchorPane.setBottomAnchor(content, 0.0)
                                AnchorPane.setLeftAnchor(content, 10.0)
                                AnchorPane.setRightAnchor(content, 10.0)
                            }

                            swipeContainer = SwipeableContainer(wrapper).apply {
                                threshold = swipeThreshold
                                leftBackgroundNode = swipeLeftFactory?.invoke(item, this)
                                rightBackgroundNode = swipeRightFactory?.invoke(item, this)
                            }
                            graphic = swipeContainer
                            text = null
                        } else {
                            graphic = content
                            text = null
                        }

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
