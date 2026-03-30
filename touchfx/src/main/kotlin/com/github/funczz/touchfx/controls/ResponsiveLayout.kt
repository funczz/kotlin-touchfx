package com.github.funczz.touchfx.controls

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.layout.Pane

/**
 * ナビゲーションとメインコンテンツの位置関係を画面幅に応じて柔軟に配置するコンテナです。
 *
 * @param breakpoint レイアウトを切り替える幅の閾値（ピクセル）
 */
class ResponsiveLayout(breakpoint: Double = 600.0) : Pane() {

    /**
     * ナビゲーションを配置する辺。
     */
    enum class Side { TOP, BOTTOM, LEFT, RIGHT }

    /**
     * メインコンテンツ。
     */
    val contentProperty: ObjectProperty<Node?> = SimpleObjectProperty(this, "content")
    var content: Node?
        get() = contentProperty.get()
        set(value) = contentProperty.set(value)

    /**
     * ナビゲーション（メニュー、タブ等）。
     */
    val navigationProperty: ObjectProperty<Node?> = SimpleObjectProperty(this, "navigation")
    var navigation: Node?
        get() = navigationProperty.get()
        set(value) = navigationProperty.set(value)

    /**
     * レイアウトを切り替える幅の閾値。
     */
    val breakpointProperty = SimpleDoubleProperty(breakpoint)
    var breakpoint: Double
        get() = breakpointProperty.get()
        set(value) = breakpointProperty.set(value)

    /**
     * 閾値未満（狭い画面）の時のナビゲーション位置。
     */
    val narrowPositionProperty = SimpleObjectProperty(Side.BOTTOM)
    var narrowPosition: Side
        get() = narrowPositionProperty.get()
        set(value) = narrowPositionProperty.set(value)

    /**
     * 閾値以上（広い画面）の時のナビゲーション位置。
     */
    val widePositionProperty = SimpleObjectProperty(Side.LEFT)
    var widePosition: Side
        get() = widePositionProperty.get()
        set(value) = widePositionProperty.set(value)

    init {
        contentProperty.addListener { _, old, new -> updateChildren(old, new) }
        navigationProperty.addListener { _, old, new -> updateChildren(old, new) }

        widthProperty().addListener { _, _, _ -> requestLayout() }
        heightProperty().addListener { _, _, _ -> requestLayout() }
        breakpointProperty.addListener { _, _, _ -> requestLayout() }
        narrowPositionProperty.addListener { _, _, _ -> requestLayout() }
        widePositionProperty.addListener { _, _, _ -> requestLayout() }
    }

    private fun updateChildren(old: Node?, new: Node?) {
        old?.let { children.remove(it) }
        new?.let { if (!children.contains(it)) children.add(it) }
    }

    override fun layoutChildren() {
        val nav = navigation
        val cont = content
        val currentWidth = width
        val currentHeight = height
        val isWide = currentWidth >= breakpoint
        val side = if (isWide) widePosition else narrowPosition

        if (nav == null) {
            cont?.let { layoutInArea(it, 0.0, 0.0, currentWidth, currentHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP) }
            return
        }

        val navPrefWidth = nav.prefWidth(-1.0)
        val navPrefHeight = nav.prefHeight(-1.0)

        when (side) {
            Side.TOP -> {
                layoutInArea(nav, 0.0, 0.0, currentWidth, navPrefHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP)
                cont?.let { layoutInArea(it, 0.0, navPrefHeight, currentWidth, currentHeight - navPrefHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP) }
            }
            Side.BOTTOM -> {
                layoutInArea(nav, 0.0, currentHeight - navPrefHeight, currentWidth, navPrefHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.BOTTOM)
                cont?.let { layoutInArea(it, 0.0, 0.0, currentWidth, currentHeight - navPrefHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP) }
            }
            Side.LEFT -> {
                layoutInArea(nav, 0.0, 0.0, navPrefWidth, currentHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP)
                cont?.let { layoutInArea(it, navPrefWidth, 0.0, currentWidth - navPrefWidth, currentHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP) }
            }
            Side.RIGHT -> {
                layoutInArea(nav, currentWidth - navPrefWidth, 0.0, navPrefWidth, currentHeight, 0.0, javafx.geometry.HPos.RIGHT, javafx.geometry.VPos.TOP)
                cont?.let { layoutInArea(it, 0.0, 0.0, currentWidth - navPrefWidth, currentHeight, 0.0, javafx.geometry.HPos.LEFT, javafx.geometry.VPos.TOP) }
            }
        }
    }

    override fun computePrefWidth(height: Double): Double {
        val isWide = width >= breakpoint
        val side = if (isWide) widePosition else narrowPosition
        val navWidth = navigation?.prefWidth(height) ?: 0.0
        val contWidth = content?.prefWidth(height) ?: 0.0

        return when (side) {
            Side.LEFT, Side.RIGHT -> navWidth + contWidth
            Side.TOP, Side.BOTTOM -> maxOf(navWidth, contWidth)
        }
    }

    override fun computePrefHeight(width: Double): Double {
        val isWide = width >= breakpoint
        val side = if (isWide) widePosition else narrowPosition
        val navHeight = navigation?.prefHeight(width) ?: 0.0
        val contHeight = content?.prefHeight(width) ?: 0.0

        return when (side) {
            Side.TOP, Side.BOTTOM -> navHeight + contHeight
            Side.LEFT, Side.RIGHT -> maxOf(navHeight, contHeight)
        }
    }
}
