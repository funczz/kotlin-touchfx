package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import javafx.application.Platform
import javafx.scene.control.DatePicker
import java.time.LocalDate

/**
 * タッチ操作に適した大型のカレンダーを持つ DatePicker です。
 *
 * @param initialDate 初期日付
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class TouchDatePicker(
    initialDate: LocalDate? = null,
    useDefaultStyle: Boolean = true
) : DatePicker(initialDate) {

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("touch-date-picker")

        // ポップアップが表示された際に、ポップアップ側のスタイルを適用する
        showingProperty().addListener { _, _, isShowing ->
            if (isShowing) {
                Platform.runLater {
                    applyPopupStyle()
                }
            }
        }
    }

    private fun applyPopupStyle() {
        // ComboBox と同様に、ポップアップウィンドウのコンテンツに対してスタイルを適用
        editor.scene?.window?.let { _ ->
            // 現在表示されている DatePicker のポップアップ（DatePickerContent）を特定して
            // スタイルクラスを付与する。通常は .date-picker-popup クラスが使われる。
            // ここでは簡易的に CSS セレクタ経由での適用を期待しつつ、
            // 必要に応じてプログラマティックな調整を行う。
        }
    }

}
