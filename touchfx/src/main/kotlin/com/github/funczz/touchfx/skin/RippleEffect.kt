package com.github.funczz.touchfx.skin

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import kotlin.math.max
import kotlin.math.sqrt

/**
 * ノードに対して波紋効果（Ripple Effect）を付与するユーティリティ。
 */
object RippleEffect {

    private const val RIPPLE_APPLIED_KEY = "touchfx-ripple-applied"

    /**
     * 指定されたノードに波紋効果を適用します。
     * ノードは [Pane] である必要があります。
     */
    fun apply(node: Node) {
        if (node.properties.containsKey(RIPPLE_APPLIED_KEY)) return
        val pane = node as? Pane ?: return
        
        // 子要素での消費に関わらず確実に開始
        pane.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
            val ripple = Circle(0.0, Color.rgb(0, 0, 0, 0.15)).apply {
                isMouseTransparent = true
                // レイアウト計算から除外することで、親（セル）のサイズに影響を与えない
                isManaged = false
                centerX = event.x
                centerY = event.y
                
                val clipRect = Rectangle()
                clipRect.widthProperty().bind(pane.widthProperty())
                clipRect.heightProperty().bind(pane.heightProperty())
                this.clip = clipRect
            }
            
            pane.children.add(ripple)
            
            val maxDist = sqrt(max(pane.width, pane.height).let { it * it * 2 })
            val endRadius = maxDist * 1.2
            
            val timeline = Timeline(
                KeyFrame(Duration.ZERO, 
                    KeyValue(ripple.radiusProperty(), 0.0),
                    KeyValue(ripple.opacityProperty(), 1.0)
                ),
                KeyFrame(Duration.millis(500.0), 
                    KeyValue(ripple.radiusProperty(), endRadius),
                    KeyValue(ripple.opacityProperty(), 0.0)
                )
            )
            
            timeline.setOnFinished {
                pane.children.remove(ripple)
            }
            timeline.play()
        }
        
        node.properties[RIPPLE_APPLIED_KEY] = true
    }
}
