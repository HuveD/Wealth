package kr.co.huve.wealth.view

import io.reactivex.rxjava3.core.Observable

interface EventObservable<E> {
    fun events(): Observable<E>
}