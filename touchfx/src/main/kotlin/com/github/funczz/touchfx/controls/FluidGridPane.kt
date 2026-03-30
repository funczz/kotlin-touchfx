package com.github.funczz.touchfx.controls

import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.layout.Pane
import kotlin.math.floor
import kotlin.math.max

/**
 * 利用可能な幅に応じて、1行あたりの列数を自動調整するグリッドコンテナです。
 *
 * @property columnWidth 各列の最小（推奨）幅
 */
class FluidGridPane(columnWidth: Double = 200.0) : Pane() {

    /**
     * 各列の最小幅。
     */
    val columnWidthProperty = SimpleDoubleProperty(columnWidth)
    var columnWidth: Double
        get() = columnWidthProperty.get()
        set(value) = columnWidthProperty.set(value)

    /**
     * 要素間の垂直方向の余白。
     */
    val vgapProperty = SimpleDoubleProperty(10.0)
    var vgap: Double
        get() = vgapProperty.get()
        set(value) = vgapProperty.set(value)

    /**
     * 要素間の水平方向の余白。
     */
    val hgapProperty = SimpleDoubleProperty(10.0)
    var hgap: Double
        get() = hgapProperty.get()
        set(value) = hgapProperty.set(value)

    init {
        // 幅や設定が変わったら再レイアウトを要求する
        widthProperty().addListener { _, _, _ -> requestLayout() }
        columnWidthProperty.addListener { _, _, _ -> requestLayout() }
        vgapProperty.addListener { _, _, _ -> requestLayout() }
        hgapProperty.addListener { _, _, _ -> requestLayout() }
        paddingProperty().addListener { _, _, _ -> requestLayout() }
    }

    /**
     * このコンテナの高さが幅に依存することをレイアウトエンジンに伝えます。
     */
    override fun getContentBias(): Orientation {
        return Orientation.HORIZONTAL
    }

    override fun layoutChildren() {
        val children = activeChildren
        if (children.isEmpty()) return

        val pad = padding
        val availableWidth = width - pad.left - pad.right
        if (availableWidth <= 0) return

        val numCols = calculateNumCols(availableWidth)
        val actualColWidth = calculateColWidth(availableWidth, numCols)
        val rowHeights = calculateRowHeights(children, numCols, actualColWidth)

        var row = 0
        var col = 0
        for (i in children.indices) {
            val child = children[i]
            val x = pad.left + col * (actualColWidth + hgap)
            
            var y = pad.top
            for (r in 0 until row) {
                y += rowHeights[r] + vgap
            }

            layoutInArea(child, x, y, actualColWidth, rowHeights[row], 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP)

            col++
            if (col >= numCols) {
                col = 0
                row++
            }
        }
    }

    override fun computePrefWidth(height: Double): Double {
        val children = activeChildren
        if (children.isEmpty()) return padding.left + padding.right
        // 最小限 1 列分の幅を推奨幅とする
        return columnWidth + padding.left + padding.right
    }

    override fun computeMinWidth(height: Double): Double {
        return padding.left + padding.right // 限界まで縮小可能にする
    }

    override fun computePrefHeight(width: Double): Double {
        val children = activeChildren
        if (children.isEmpty()) return padding.top + padding.bottom

        // 引数の width が指定されていない場合は、現在の幅を使用する
        val targetWidth = if (width < 0) this.width else width
        val availableWidth = targetWidth - padding.left - padding.right
        if (availableWidth <= 0) return padding.top + padding.bottom

        val numCols = calculateNumCols(availableWidth)
        val actualColWidth = calculateColWidth(availableWidth, numCols)
        val rowHeights = calculateRowHeights(children, numCols, actualColWidth)
        
        return padding.top + padding.bottom + rowHeights.sum() + (max(0, rowHeights.size - 1) * vgap)
    }

    override fun computeMinHeight(width: Double): Double = computePrefHeight(width)

    private fun calculateNumCols(availableWidth: Double): Int {
        return max(1, floor((availableWidth + hgap) / (columnWidth + hgap)).toInt())
    }

    private fun calculateColWidth(availableWidth: Double, numCols: Int): Double {
        return (availableWidth - (numCols - 1) * hgap) / numCols
    }

    private fun calculateRowHeights(children: List<Node>, numCols: Int, actualColWidth: Double): List<Double> {
        val rowHeights = mutableListOf<Double>()
        for (i in children.indices step numCols) {
            var maxHeight = 0.0
            for (j in 0 until numCols) {
                val index = i + j
                if (index < children.size) {
                    maxHeight = max(maxHeight, children[index].prefHeight(actualColWidth))
                }
            }
            rowHeights.add(maxHeight)
        }
        return rowHeights
    }

    private val activeChildren: List<Node>
        get() = children.filter { it.isManaged }
}
