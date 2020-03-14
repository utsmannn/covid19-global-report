package com.utsman.covid19

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

fun <T>CompositeDisposable.route(observable: Observable<T>, io: ((T) -> Unit)? = null,
                              main: ((T) -> Unit)? = null,
                              error: ((throwable: Throwable) -> Unit)? = null) {

    add(
        observable
            .subscribeOn(Schedulers.io())
            .doOnNext {
                io?.invoke(it)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                main?.invoke(it)
            }, {
                error?.invoke(it)
            })

    )
}