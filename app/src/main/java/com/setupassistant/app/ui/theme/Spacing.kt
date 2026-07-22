package com.setupassistant.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * 余白の刻み。画面ごとに数値を書くとばらつくため、ここから選ぶ。
 */
object Spacing {
    /** 密接した要素の間 */
    val Tight = 4.dp

    /** 同じまとまりの中の要素の間 */
    val Small = 8.dp

    /** カード内の区切り */
    val Medium = 12.dp

    /** 画面の左右余白、カードの内側 */
    val Large = 16.dp

    /** まとまり同士の間、フォームの左右余白 */
    val XLarge = 24.dp
}
