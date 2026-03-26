package com.github.funczz.touchfx.skin

import javafx.animation.FadeTransition
import javafx.animation.Interpolator
import javafx.animation.ScaleTransition
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import java.util.WeakHashMap

/**
 * ノードに対してタップ時の波紋効果 (Ripple Effect) を提供するユーティリティです。
 * Scene 全体でイベントを監視し、対象ノードがタップされた際に波紋を表示します。
 */
object RippleEffect {

    private val enabledNodes = WeakHashMap<Node, RippleConfig>()
    private val sceneFilters = WeakHashMap<Scene, Boolean>()

    private data class RippleConfig(val color: Color, val duration: Duration)

    /**
     * 指定されたノードに波紋効果を適用します。
     */
    fun apply(
        node: Node,
        color: Color = Color.color(0.0, 0.0, 0.0, 0.15),
        duration: Duration = Duration.millis(500.0)
    ) {
        enabledNodes[node] = RippleConfig(color, duration)
        
        // ノードがシーンに追加されたら、シーンにフィルタを登録する
        if (node.scene != null) {
            registerSceneFilter(node.scene)
        }
        node.sceneProperty().addListener { _, _, newScene ->
            if (newScene != null) {
                registerSceneFilter(newScene)
            }
        }
    }

    private fun registerSceneFilter(scene: Scene) {
        if (sceneFilters.containsKey(scene)) return
        sceneFilters[scene] = true

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED) { event ->
            val target = event.target as? Node ?: return@addEventFilter
            
            // クリックされたノード、またはその親のいずれかが登録されているか確認
            var current: Node? = target
            var foundNode: Node? = null
            var config: RippleConfig? = null
            
            while (current != null) {
                config = enabledNodes[current]
                if (config != null) {
                    foundNode = current
                    break
                }
                current = current.parent
            }

            if (foundNode != null && config != null) {
                showRipple(scene, foundNode, event.sceneX, event.sceneY, config)
            }
        }
    }

    private fun showRipple(scene: Scene, node: Node, sceneX: Double, sceneY: Double, config: RippleConfig) {
        val root = scene.root as? Pane ?: return
        val nodeBounds = node.localToScene(node.layoutBounds)
        
        val ripple = Circle(2.0, config.color)
        ripple.isMouseTransparent = true
        
        // シーンのルートにおける絶対座標
        ripple.translateX = sceneX
        ripple.translateY = sceneY
        
        // クリップの設定
        val clipRect = Rectangle(
            nodeBounds.minX - sceneX,
            nodeBounds.minY - sceneY,
            nodeBounds.width,
            nodeBounds.height
        )
        clipRect.translateX = -ripple.radius
        clipRect.translateY = -ripple.radius
        ripple.clip = clipRect
        
        val maxRadius = Math.sqrt(
            Math.pow(nodeBounds.width, 2.0) + Math.pow(nodeBounds.height, 2.0)
        ) * 1.5

        val scale = ScaleTransition(config.duration, ripple)
        scale.fromX = 1.0
        scale.fromY = 1.0
        scale.toX = maxRadius / 2.0
        scale.toY = maxRadius / 2.0
        scale.interpolator = Interpolator.EASE_OUT

        val fade = FadeTransition(config.duration, ripple)
        fade.fromValue = 1.0
        fade.toValue = 0.0
        fade.interpolator = Interpolator.EASE_OUT
        fade.setOnFinished { 
            root.children.remove(ripple)
        }

        root.children.add(ripple)
        ripple.toFront()
        
        scale.play()
        fade.play()
    }
}
