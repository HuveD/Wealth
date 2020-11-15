package kr.co.huve.wealthApp.view

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Consumers of a given state source often need to create fine-grained subscriptions
 * to control performance and frequency of updates.
 */
interface StateSubscriber<S> {
    fun Observable<S>.subscribeToState(): Disposable
}