package com.github.funczz.touchfx

/**
 * TouchFX ライブラリの基本情報とユーティリティを提供します。
 */
object TouchFX {

    /**
     * ライブラリのデフォルトスタイルシートの URL。
     */
    val defaultStyleSheet: String? = TouchFX::class.java.getResource("css/touchfx.css")?.toExternalForm()

    /**
     * 挨拶メッセージを返します (動作確認用)。
     */
    fun hello() = "Hello, TouchFX!"
}
