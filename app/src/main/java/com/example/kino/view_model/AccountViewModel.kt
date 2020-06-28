package com.example.kino.view_model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.kino.model.account.Success
import com.example.kino.model.repository.AccountRepository
import com.example.kino.utils.ApiResponse
import com.example.kino.utils.constants.API_KEY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AccountViewModel(
    private val context: Context,
    private val accountRepository: AccountRepository
) : BaseViewModel() {

    val username = MutableLiveData<String>()
    val liveData = MutableLiveData<State>()

    init {
        getUsername()
    }

    private fun getUsername() {
        username.value = accountRepository.getLocalUsername(context)
    }

    fun logOut() {
        disposable.add(
            accountRepository.logOut(API_KEY, context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        when (result) {
                            is ApiResponse.Success<Success> -> {
                                if (result.result.success) {
                                    deleteLogInData()
                                    liveData.value = State.LogOutSuccessful
                                }
                            }
                            is ApiResponse.Error -> liveData.value = State.LogOutFailed
                        }
                    },
                    {
                        liveData.value = State.LogOutFailed
                    }
                )
        )
    }

    private fun deleteLogInData() {
        accountRepository.deleteLoginData(context)
    }

    sealed class State {
        object LogOutSuccessful : State()
        object LogOutFailed : State()
    }
}
