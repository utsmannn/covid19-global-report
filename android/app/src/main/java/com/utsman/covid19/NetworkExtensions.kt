package com.utsman.covid19

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

fun CompositeDisposable.route(observable: Observable<Responses>, io: ((Responses) -> Unit)? = null,
                                 main: ((Responses) -> Unit)? = null,
                                 error: ((throwable: Throwable) -> Unit)? = null) {

    add(
        observable
            .subscribeOn(Schedulers.io())
            .doOnNext {
                io?.invoke(it)
                logi("oy oy ${it.data?.size}")
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                main?.invoke(it)
            }, {
                error?.invoke(it)
            })

    )
}