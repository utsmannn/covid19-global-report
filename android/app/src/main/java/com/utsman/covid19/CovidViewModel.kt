package com.utsman.covid19

import androidx.lifecycle.*
import com.utsman.covid19.ext.logi
import com.utsman.covid19.network.*
import io.reactivex.disposables.CompositeDisposable

class CovidViewModel : ViewModel() {
    private val composite = CompositeDisposable()
    private val lastDate: MutableLiveData<ResponsesLastDate> = MutableLiveData()
    private var day: Int? = null
    private var month: Int? = null

    val networkState: MutableLiveData<NetworkState> = MutableLiveData()
    val throwable: MutableLiveData<Throwable> = MutableLiveData()
    val total: MutableLiveData<Total> = MutableLiveData()
    val totalCountry: MutableLiveData<Total> = MutableLiveData()
    val dataCountry: MutableLiveData<List<DataCountry>> = MutableLiveData()
    val data: MutableLiveData<List<Data>> = MutableLiveData()

    fun getLastDate(): LiveData<ResponsesLastDate> {
        networkState.postValue(NetworkState.LOADING)
        composite.route(
            RetrofitInstance.create().getLastDate(),
            io = {
                day = it.lastDate?.day
                month = it.lastDate?.month

                lastDate.postValue(it)
                networkState.postValue(NetworkState.LOADED)
            },
            error = {
                networkState.postValue(NetworkState.ERROR)
                throwable.postValue(it)
            }
        )

        return lastDate
    }

    fun getDataCountry(q: String? = null) {
        networkState.postValue(NetworkState.LOADING)
        composite.route(
            RetrofitInstance.create().getDataCountry(day, month, 2020, q),
            io = {
                dataCountry.postValue(it.countries)
                totalCountry.postValue(it.total)
                networkState.postValue(NetworkState.LOADED)
            },
            error = {
                networkState.postValue(NetworkState.ERROR)
                throwable.postValue(it)
            }
        )
    }

    fun getData(day: Int?, month: Int?) {
        networkState.postValue(NetworkState.LOADING)
        composite.route(
            RetrofitInstance.create().getData(day, month, 2020),
            io = {
                networkState.postValue(NetworkState.LOADED)
                data.postValue(it.data)
                total.postValue(it.total)
            },
            error = {
                networkState.postValue(NetworkState.ERROR)
                throwable.postValue(it)
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        composite.dispose()
    }

}