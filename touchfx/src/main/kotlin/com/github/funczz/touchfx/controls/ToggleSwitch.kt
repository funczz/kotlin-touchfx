package com.github.funczz.touchfx.controls

import com.github.funczz.touchfx.TouchFX
import com.github.funczz.touchfx.skin.RippleEffect
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.css.PseudoClass
import javafx.geometry.Pos
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle

/**
 * タッチ操作に最適化された、自作のスイッチ形式選択コントロールです。
 *
 * @param text スイッチの横に表示するラベルテキスト
 * @param useDefaultStyle デフォルトのスタイルシートを適用するかどうか
 */
class ToggleSwitch(
    text: String? = null,
    useDefaultStyle: Boolean = true
) : Control() {

    companion object {
        private val SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected")
    }

    private val selectedProperty = SimpleBooleanProperty(false)
    var isSelected: Boolean
        get() = selectedProperty.get()
        set(value) = selectedProperty.set(value)

    fun selectedProperty() = selectedProperty

    private val textProperty = SimpleStringProperty(text ?: "")
    var text: String
        get() = textProperty.get()
        set(value) = textProperty.set(value)

    fun textProperty() = textProperty

    init {
        if (useDefaultStyle) {
            TouchFX.defaultStyleSheet?.let {
                stylesheets.add(it)
            }
        }
        styleClass.add("toggle-switch-custom")
        
        // 状態変化に合わせて擬似クラスを更新
        selectedProperty.addListener { _ ->
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, isSelected)
        }
        
        // 初期状態の反映
        pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, isSelected)

        // クリックで状態反転
        setOnMouseClicked {
            isSelected = !isSelected
        }
        
        RippleEffect.apply(this)
    }

    override fun createDefaultSkin(): Skin<*> {
        return ToggleSwitchSkin(this)
    }
}

/**
 * ToggleSwitch の外観を定義するスキン。
 */
class ToggleSwitchSkin(control: ToggleSwitch) : SkinBase<ToggleSwitch>(control) {

    private val track = Rectangle(60.0, 30.0).apply {
        arcWidth = 30.0
        arcHeight = 30.0
        styleClass.add("track")
    }

    private val thumb = Circle(13.0).apply {
        styleClass.add("thumb")
    }

    private val switchContainer = StackPane(track, thumb).apply {
        alignment = Pos.CENTER
        maxWidth = 60.0
        maxHeight = 30.0
    }

    private val label = Label().apply {
        textProperty().bind(control.textProperty())
        styleClass.add("label")
    }

    private val mainContainer = HBox(15.0, switchContainer, label).apply {
        alignment = Pos.CENTER_LEFT
    }

    init {
        children.add(mainContainer)

        control.selectedProperty().addListener { _, _, isSelected ->
            updateThumbPosition(isSelected)
        }
        
        updateThumbPosition(control.isSelected)
    }

    private fun updateThumbPosition(isSelected: Boolean) {
        // つまみの位置移動のみを管理。色は CSS に任せる。
        if (isSelected) {
            thumb.translateX = 15.0
        } else {
            thumb.translateX = -15.0
        }
    }
}
