package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import javafx.application.Platform
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell

/**
 * タッチ操作に適したサイズ感を持つコンボボックスです。
 *
 * @param T 選択項目の型
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
        if (cellFactory == null) {
            setCellFactory { _ ->
                object : ListCell<T>() {
                    init {
                        // セル自体にスタイルクラスを付与
                        styleClass.add("touch-combo-box-cell")
                    }
                    override fun updateItem(item: T, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (empty || item == null) {
                            text = null
                            graphic = null
                        } else {
                            text = item.toString()
                        }
                    }
                }
            }
        }
        
        // 選択された値を表示するボタン部分（buttonCell）にも同様のスタイルを適用
        if (buttonCell == null) {
            setButtonCell(object : ListCell<T>() {
                init {
                    styleClass.add("touch-combo-box-cell")
                }
                override fun updateItem(item: T, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        text = null
                        graphic = null
                    } else {
                        text = item.toString()
                    }
                }
            })
        }
    }

}
