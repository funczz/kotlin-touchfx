package com.github.funczz.touchfx.i18n

import java.util.*

/**
 * TouchFX の多言語対応を管理するユーティリティ。
 */
object TouchFXI18n {

    private const val BUNDLE_NAME = "com.github.funczz.touchfx.i18n.messages"

    /**
     * 現在のロケール。
     */
    var locale: Locale = Locale.getDefault()
        set(value) {
            field = value
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, value)
        }

    private var bundle: ResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale)

    /**
     * 指定されたキーに対応する翻訳文字列を取得します。
     */
    fun getString(key: String): String {
        return try {
            bundle.getString(key)
        } catch (e: MissingResourceException) {
            key
        }
    }

    /**
     * 指定されたキーに対応する翻訳文字列を取得し、引数を埋め込みます。
     */
    fun getString(key: String, vararg args: Any): String {
        return try {
            val pattern = bundle.getString(key)
            String.format(pattern, *args)
        } catch (e: MissingResourceException) {
            key
        }
    }
}
