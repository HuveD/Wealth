package kr.co.huve.wealthApp.util

import android.content.Context


inline fun <R> R?.notNull(body: R.() -> Unit) {
    if (this != null) {
        body()
    }
}

inline fun <R> R?.whenNull(body: () -> Unit) {
    if (this == null) {
        body()
    }
}

fun Context.convertDpToPixel(dp: Float): Float {
    val metrics = resources.displayMetrics
    return if (metrics == null) dp else metrics.density * dp
}

fun Context.convertPixelsToDp(px: Float): Float {
    val metrics = resources.displayMetrics
    return if (metrics == null) px else px / metrics.density
}