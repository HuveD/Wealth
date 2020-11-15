package kr.co.huve.wealthApp.intent

interface Intent<T> {
    fun reduce(oldState: T): T
}

/**
 * DSL function to help build intents from code blocks.
 *
 * NOTE: Magic of extension functions, (T)->T and T.()->T interchangeable.
 */
fun <T> intent(block: T.() -> T) : Intent<T> = object : Intent<T> {
    override fun reduce(oldState: T): T = block(oldState)
}

/**
 * By delegating work to other models, repositories or services, we
 * end up with situations where we don't need to update our ModelStore
 * state until the delegated work completes.
 *
 * Use the `sideEffect {}` DSL function for those situations.
 */
fun <T> sideEffect(block: T.() -> Unit) : Intent<T> = object : Intent<T> {
    override fun reduce(oldState: T): T = oldState.apply(block)
}


/*
    뷰 상태를 바꿔야 된다 -> Intent
    작업은 있지만 뷰 상태를 바꾸지 않는다 -> Side Effect

    화면 이동(다시 돌아올 일 없음) -> Intent
    화면 이동 후 다시 돌아오는 경우 -> Side Effect
 */