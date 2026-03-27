package com.github.funczz.touchfx.controls

import javafx.animation.TranslateTransition
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import javafx.util.Duration
import kotlin.math.abs

/**
 * 左右へのスワイプ操作によって背後のアクションボタンを表示できるコンテナです。
 *
 * @property contentNode メインとなるコンテンツノード
 */
class SwipeableContainer(
    val contentNode: Node
) : StackPane() {

    /**
     * 右側にスワイプ（左側から露出）した際に表示されるノード。
     */
    var leftBackgroundNode: Node? = null
        set(value) {
            field?.let { children.remove(it) }
            field = value
            value?.let {
                it.isVisible = false
                children.add(0, it)
                StackPane.setAlignment(it, Pos.CENTER_LEFT)
            }
        }

    /**
     * 左側にスワイプ（右側から露出）した際に表示されるノード。
     */
    var rightBackgroundNode: Node? = null
        set(value) {
            field?.let { children.remove(it) }
            field = value
            value?.let {
                it.isVisible = false
                children.add(0, it)
                StackPane.setAlignment(it, Pos.CENTER_RIGHT)
            }
        }

    /**
     * アクションを確定させるためのスワイプしきい値（ピクセル）。
     */
    var threshold: Double = 50.0

    /**
     * 現在のスワイプ量。
     */
    var swipeOffset: Double = 0.0
        private set(value) {
            field = value
            contentNode.translateX = value
            updateBackgroundVisibility()
        }

    private var startX: Double = 0.0
    private var startY: Double = 0.0
    private var isSwiping: Boolean = false
    private var isLockedToOtherGesture: Boolean = false

    init {
        children.add(contentNode)
        
        // コンテナのサイズでクリップする
        val clipRect = Rectangle()
        clipRect.widthProperty().bind(widthProperty())
        clipRect.heightProperty().bind(heightProperty())
        clip = clipRect

        contentNode.addEventFilter(MouseEvent.MOUSE_PRESSED, ::handleMousePressed)
        contentNode.addEventFilter(MouseEvent.MOUSE_DRAGGED, ::handleMouseDragged)
        contentNode.addEventFilter(MouseEvent.MOUSE_RELEASED, ::handleMouseReleased)
    }

    /**
     * コンテンツを元の位置に戻します。
     */
    fun reset(animate: Boolean = true) {
        if (animate) {
            val transition = TranslateTransition(Duration.millis(200.0), contentNode)
            transition.toX = 0.0
            transition.setOnFinished { 
                swipeOffset = 0.0
                isSwiping = false
                isLockedToOtherGesture = false
            }
            transition.play()
        } else {
            swipeOffset = 0.0
            isSwiping = false
            isLockedToOtherGesture = false
            contentNode.translateX = 0.0
        }
    }

    private fun handleMousePressed(event: MouseEvent) {
        startX = event.sceneX
        startY = event.sceneY
        
        if (abs(swipeOffset) < 0.1) {
            isSwiping = false
            isLockedToOtherGesture = false
        }
    }

    private fun handleMouseDragged(event: MouseEvent) {
        if (isLockedToOtherGesture) return

        val deltaX = event.sceneX - startX
        val deltaY = event.sceneY - startY
        
        // スワイプ開始の判定
        if (!isSwiping) {
            // 水平移動が垂直移動より大きく、かつ一定量(5px)を超えた場合にスワイプ開始
            if (abs(deltaX) > 5.0 && abs(deltaX) > abs(deltaY)) {
                isSwiping = true
            } else if (abs(deltaY) > 5.0) {
                isLockedToOtherGesture = true
                return
            }
        }

        if (isSwiping) {
            var newOffset = deltaX
            
            // スワイプ方向の制限: 
            // - 右スワイプ (newOffset > 0) は leftBackgroundNode がある場合のみ許可
            // - 左スワイプ (newOffset < 0) は rightBackgroundNode がある場合のみ許可
            if (newOffset > 0 && leftBackgroundNode == null) {
                newOffset = 0.0
            } else if (newOffset < 0 && rightBackgroundNode == null) {
                newOffset = 0.0
            }
            
            swipeOffset = newOffset
            event.consume()
        }
    }

    private fun handleMouseReleased(event: MouseEvent) {
        if (isSwiping) {
            val currentOffset = swipeOffset
            val leftWidth = leftBackgroundNode?.prefWidth(-1.0) ?: 0.0
            val rightWidth = rightBackgroundNode?.prefWidth(-1.0) ?: 0.0

            if (currentOffset > threshold && leftBackgroundNode != null) {
                animateTo(leftWidth)
            } else if (currentOffset < -threshold && rightBackgroundNode != null) {
                animateTo(-rightWidth)
            } else {
                reset()
            }
            event.consume()
        } else if (abs(swipeOffset) > 0.1) {
            reset()
        }
    }

    private fun animateTo(targetX: Double) {
        val transition = TranslateTransition(Duration.millis(200.0), contentNode)
        transition.toX = targetX
        transition.setOnFinished { 
            swipeOffset = targetX 
            isSwiping = false
        }
        transition.play()
    }

    private fun updateBackgroundVisibility() {
        leftBackgroundNode?.let {
            it.isVisible = swipeOffset > 0
            if (it.isVisible) it.toFront()
        }
        rightBackgroundNode?.let {
            it.isVisible = swipeOffset < 0
            if (it.isVisible) it.toFront()
        }
        contentNode.toFront()
    }
}
