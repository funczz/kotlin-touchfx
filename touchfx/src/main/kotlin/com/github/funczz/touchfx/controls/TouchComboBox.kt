package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.application.Platform
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView

/**
 * タッチ操作に適したサイズ感を持つ ComboBox です。
 *
 * @param T リストアイテムの型
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchComboBox<T>(
    useDefaultStyle: Boolean = true
) : ComboBox<T>() {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-combo-box")
        RippleEffect.apply(this)

        // 初期状態でセルファクトリを設定し、リストアイテムのサイズを制御する
        applyPopupStyle()

        // ポップアップが表示される際にも再確認（動的な変更に対応）
        showingProperty().addListener { _, _, isShowing ->
            if (isShowing) {
                Platform.runLater {
                    applyPopupStyle()
                }
            }
        }
    }

    private fun applyPopupStyle() {
        // リストアイテムのスタイル設定
        if (cellFactory == null) {
            setCellFactory { _ ->
                object : ListCell<T>() {
                    override fun updateItem(item: T, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (empty || item == null) {
                            text = null
                            graphic = null
                        } else {
                            text = converter?.toString(item) ?: item.toString()
                            if (!styleClass.contains("toolbar-combo-box-cell")) {
                                styleClass.add("touch-combo-box-cell")
                            }
                        }
                    }
                }
            }
        }

        // 選択後の表示部分（Button Cell）のスタイル設定
        if (buttonCell == null) {
            setButtonCell(object : ListCell<T>() {
                override fun updateItem(item: T, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                        graphic = null
                    } else {
                        text = converter?.toString(item) ?: item.toString()
                        if (!styleClass.contains("toolbar-combo-box-cell")) {
                            styleClass.add("touch-combo-box-cell")
                        }
                    }
                }
            })
        }

        // ポップアップ内の ListView へのアクセスを試みる
        val skin = skin ?: return
        val popup = (skin as? javafx.scene.control.skin.ComboBoxListViewSkin<*>)?.popupContent ?: return
        if (popup is ListView<*>) {
            popup.styleClass.add("touch-combo-box-popup")
        }
    }

}
