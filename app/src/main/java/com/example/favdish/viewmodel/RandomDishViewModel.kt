package com.example.favdish.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.favdish.model.entities.RandomDish
import com.example.favdish.model.network.RandomDishAPIService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class RandomDishViewModel : ViewModel() {

    private val randomDishAPIService = RandomDishAPIService()
    private val compositeDisposable = CompositeDisposable()

    val isLoadingRandomDish = MutableLiveData<Boolean>()
    val randomDishResponse = MutableLiveData<RandomDish.Recipes>()
    val isLoadingError = MutableLiveData<Boolean>()

    fun getRandomDishRecipeFromAPI() {
        isLoadingRandomDish.value = true

        compositeDisposable.add(
            randomDishAPIService.getRandomDish()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<RandomDish.Recipes>(){
                    override fun onSuccess(t: RandomDish.Recipes) {
                        isLoadingRandomDish.value = false
                        randomDishResponse.value = t
                        isLoadingError.value = false
                    }

                    override fun onError(e: Throwable) {
                        isLoadingRandomDish.value = false

                        isLoadingError.value = true
                        e.printStackTrace()
                    }

                })
        )
    }
}