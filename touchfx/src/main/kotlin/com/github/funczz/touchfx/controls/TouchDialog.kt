package com.github.funczz.touchfx.controls

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.stage.Modality
import javafx.stage.StageStyle

/**
 * タッチ操作に最適化したダイアログ。
 * JavaFX 標準の [Dialog] を継承し、[TouchDialogPane] を使用するように構成します。
 */
open class TouchDialog<R> : Dialog<R>() {

    init {
        // 標準で TouchDialogPane を設定
        dialogPane = TouchDialogPane()
        
        // ヘッダーテキストやコンテンツがない場合のレイアウト崩れを防ぐためのデフォルト設定
        initModality(Modality.APPLICATION_MODAL)
        initStyle(StageStyle.DECORATED) // OS 標準の枠を活かすか、独自のオーバーレイを作成するかは後のフェーズで検討
    }

    /**
     * 指定されたメッセージを表示するアラート。
     */
    companion object {
        fun createAlert(alertType: Alert.AlertType, header: String?, content: String?): TouchDialog<ButtonType> {
            val dialog = TouchDialog<ButtonType>()
            dialog.title = alertType.name
            dialog.headerText = header
            dialog.contentText = content
            
            val types = when (alertType) {
                Alert.AlertType.CONFIRMATION -> listOf(ButtonType.OK, ButtonType.CANCEL)
                Alert.AlertType.ERROR, Alert.AlertType.INFORMATION, Alert.AlertType.WARNING -> listOf(ButtonType.OK)
                else -> listOf(ButtonType.OK)
            }
            dialog.dialogPane.buttonTypes.addAll(types)
            return dialog
        }
    }
}

/**
 * 簡易的なアラート表示のための拡張機能。
 */
fun Alert.toTouchDialog(): TouchDialog<ButtonType> {
    return TouchDialog.createAlert(this.alertType, this.headerText, this.contentText)
}
