package com.utsman.covid19

import androidx.lifecycle.*
import com.utsman.covid19.network.RetrofitInstance
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CovidViewModel : ViewModel() {
    private val composite = CompositeDisposable()
    val throwable: MutableLiveData<Throwable> = MutableLiveData()
    val total: MutableLiveData<Total> = MutableLiveData()
    val data: MutableLiveData<List<Data>> = MutableLiveData()
    var country = ""

    fun getData(day: Int, month: Int, q: String? = null) {
        logi("aaaaaaaa")
        composite.route(
            RetrofitInstance.create().getData(day, month, 2020, q),
            io = {
                data.postValue(it.data)
                total.postValue(it.total)
            },
            error = {
                throwable.postValue(it)
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        composite.dispose()
    }

}