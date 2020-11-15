package kr.co.huve.wealthApp.model

import io.reactivex.rxjava3.core.Observable
import kr.co.huve.wealthApp.intent.Intent

interface Model<S> {
    /**
     * Model will receive intents to be processed via this function.
     *
     * ModelState is immutable. Processed intents will work much like `copy()`
     * and create a new (modified) modelState from an old one.
     */
    fun process(intent: Intent<S>)

    /**
     * Observable stream of changes to ModelState
     *
     * Every time a modelState is replaced by a new one, this observable will
     * fire.
     *
     * This is what views will subscribe to.
     */
    fun modelState(): Observable<S>
}