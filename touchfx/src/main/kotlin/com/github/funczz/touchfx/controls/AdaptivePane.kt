package com.github.funczz.touchfx.controls

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.layout.Pane
import kotlin.math.max

/**
 * 画面幅に応じて、子要素を水平または垂直に自動配置するレスポンシブなコンテナです。
 *
 * @param breakpoint レイアウトを切り替える幅の閾値（ピクセル）
 */
class AdaptivePane(breakpoint: Double = 600.0) : Pane() {

    /**
     * レイアウトを切り替える幅の閾値。
     */
    val breakpointProperty = SimpleDoubleProperty(breakpoint)
    var breakpoint: Double
        get() = breakpointProperty.get()
        set(value) = breakpointProperty.set(value)

    /**
     * 子要素間の余白。
     */
    val spacingProperty = SimpleDoubleProperty(10.0)
    var spacing: Double
        get() = spacingProperty.get()
        set(value) = spacingProperty.set(value)

    init {
        // 幅や設定が変更されたら再レイアウトを要求する
        widthProperty().addListener { _, _, _ -> requestLayout() }
        breakpointProperty.addListener { _, _, _ -> requestLayout() }
        spacingProperty.addListener { _, _, _ -> requestLayout() }
        paddingProperty().addListener { _, _, _ -> requestLayout() }
    }

    override fun layoutChildren() {
        val children = activeChildren
        if (children.isEmpty()) return

        val currentWidth = width
        val isHorizontal = currentWidth >= breakpoint
        
        val pad = padding
        var x = pad.left
        var y = pad.top
        val s = spacing

        for (child in children) {
            val childWidth = child.prefWidth(-1.0)
            val childHeight = child.prefHeight(-1.0)
            
            if (isHorizontal) {
                // 水平配置 (HBox 的)
                layoutInArea(child, x, y, childWidth, height - pad.top - pad.bottom, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP)
                x += childWidth + s
            } else {
                // 垂直配置 (VBox 的)
                layoutInArea(child, x, y, width - pad.left - pad.right, childHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP)
                y += childHeight + s
            }
        }
    }

    override fun computePrefWidth(height: Double): Double {
        val children = activeChildren
        if (children.isEmpty()) return padding.left + padding.right

        var width = 0.0
        for (child in children) {
            width += child.prefWidth(-1.0)
        }
        width += spacing * (children.size - 1)
        return width + padding.left + padding.right
    }

    override fun computePrefHeight(width: Double): Double {
        val children = activeChildren
        if (children.isEmpty()) return padding.top + padding.bottom

        val isHorizontal = width >= breakpoint || width < 0
        var totalHeight = 0.0
        
        if (isHorizontal) {
            for (child in children) {
                totalHeight = max(totalHeight, child.prefHeight(-1.0))
            }
        } else {
            for (child in children) {
                totalHeight += child.prefHeight(-1.0)
            }
            totalHeight += spacing * (children.size - 1)
        }
        return totalHeight + padding.top + padding.bottom
    }

    private val activeChildren: List<Node>
        get() = children.filter { it.isManaged }
}
