package kr.co.huve.wealthApp.util


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