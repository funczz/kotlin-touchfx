package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.skin.RippleEffect
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.DialogPane
import javafx.scene.layout.Pane

/**
 * タッチ操作に最適化したダイアログペイン。
 * 標準の DialogPane を継承し、ボタンに RippleEffect を適用し、
 * タッチデバイスに適したパディングやボタンサイズを設定します。
 */
open class TouchDialogPane : DialogPane() {

    init {
        styleClass.add("touch-dialog-pane")
    }

    override fun createButton(buttonType: ButtonType): Node {
        val result = super.createButton(buttonType)
        val button = result as? Button ?: return result
        
        // タッチ操作に適した最小サイズ
        button.minHeight = 44.0
        button.minWidth = 80.0
        button.styleClass.add("touch-dialog-button")
        
        // RippleEffect の適用
        // DialogPane の内部構造において、ボタンの親が Pane であることを期待
        button.parentProperty().addListener { _, _, newParent ->
            if (newParent is Pane) {
                RippleEffect.apply(button)
            }
        }
        
        return button
    }
}
