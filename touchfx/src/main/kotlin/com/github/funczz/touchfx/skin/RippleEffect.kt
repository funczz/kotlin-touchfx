package com.github.funczz.touchfx.skin

import javafx.animation.KeyFrame
import javafx.animation.KeyValue
import javafx.animation.Timeline
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
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
     * ノード自身が [Pane] でない場合、親の [Pane] を探して波紋を重畳します。
     */
    fun apply(node: Node) {
        if (node.properties.containsKey(RIPPLE_APPLIED_KEY)) return
        
        node.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
            // 追加先の Pane を特定する
            val targetPane = findTargetPane(node) ?: return@addEventFilter
            
            // 座標変換 (Local -> Scene -> targetPane)
            val scenePos = node.localToScene(event.x, event.y)
            val panePos = targetPane.sceneToLocal(scenePos)
            
            val nodeWidth = if (node is Region) node.width else node.layoutBounds.width
            val nodeHeight = if (node is Region) node.height else node.layoutBounds.height

            val ripple = Circle(0.0, Color.rgb(0, 0, 0, 0.15)).apply {
                isMouseTransparent = true
                isManaged = false
                centerX = panePos.x
                centerY = panePos.y
                
                // node の範囲で切り取る
                val clipRect = Rectangle().apply {
                    val topLeftInPane = targetPane.sceneToLocal(node.localToScene(0.0, 0.0))
                    x = topLeftInPane.x
                    y = topLeftInPane.y
                    width = nodeWidth
                    height = nodeHeight
                }
                this.clip = clipRect
            }
            
            targetPane.children.add(ripple)
            
            val maxDist = sqrt(max(nodeWidth, nodeHeight).let { it * it * 2 })
            val endRadius = maxDist * 1.5
            
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
                targetPane.children.remove(ripple)
            }
            timeline.play()
        }
        
        node.properties[RIPPLE_APPLIED_KEY] = true
    }

    private fun findTargetPane(node: Node?): Pane? {
        var current: Node? = node
        while (current != null) {
            if (current is Pane) return current
            current = current.parent
        }
        return null
    }
}
