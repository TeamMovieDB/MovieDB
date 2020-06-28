package com.example.kino.view_model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.kino.model.account.LoginValidationData
import com.example.kino.model.account.Session
import com.example.kino.model.account.Token
import com.example.kino.model.repository.AccountRepository
import com.example.kino.utils.ApiResponse
import com.example.kino.utils.constants.API_KEY
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SignInViewModel(
    private val context: Context,
    private var accountRepository: AccountRepository
) : BaseViewModel() {
    private lateinit var loginValidationData: LoginValidationData
    private var token: Token? = null
    private var sessionId: String = ""
    private var username: String = ""
    private var password: String = ""

    val liveData = MutableLiveData<State>()

    init {
        if (accountRepository.hasSessionId(context)) liveData.value = State.Result
    }

    fun createTokenRequest(receivedUsername: String, receivedPassword: String) {
        liveData.value = State.ShowLoading

        disposable.add(
            accountRepository.createToken(API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        when (result) {
                            is ApiResponse.Success<Token> -> {
                                token = result.result
                                username = receivedUsername
                                password = receivedPassword

                                if (token?.token != null) {
                                    loginValidationData = LoginValidationData(
                                        username,
                                        password,
                                        token!!.token!!
                                    )
                                    validateWithLogin()
                                }
                            }

                            is ApiResponse.Error -> {
                                liveData.value = State.FailedLoading
                                liveData.value = State.HideLoading
                            }
                        }
                    },
                    {
                        liveData.value = State.FailedLoading
                        liveData.value = State.HideLoading
                    }

                )
        )
    }

    private fun validateWithLogin() {
        disposable.add(
            accountRepository.validateWithLogin(API_KEY, loginValidationData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        when (result) {
                            is ApiResponse.Success<Token> -> {
                                createSession()
                            }
                            is ApiResponse.Error -> {
                                liveData.value = State.WrongDataProvided
                                liveData.value = State.HideLoading
                            }
                        }
                    },
                    {
                        liveData.value = State.WrongDataProvided
                        liveData.value = State.HideLoading
                    }

                )
        )
    }

    private fun createSession() {
        liveData.value = State.ShowLoading
        token?.let {
            accountRepository.createSession(API_KEY, it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        when (result) {
                            is ApiResponse.Success<Session> -> {
                                sessionId = result.result.sessionId.toString()
                                saveLoginData()
                                liveData.value = State.HideLoading
                                liveData.value = State.Result
                            }
                            is ApiResponse.Error -> {
                                liveData.value = State.FailedLoading
                                liveData.value = State.HideLoading
                            }
                        }
                    },
                    {
                        liveData.value = State.FailedLoading
                        liveData.value = State.HideLoading
                    }

                )
        }?.let { disposable.add(it) }
    }

    private fun saveLoginData() {
        accountRepository.saveLoginData(context, username, password, sessionId)
    }

    fun getSavedUsername(): String {
        return accountRepository.getLocalUsername(context)
    }

    fun getSavedPassword(): String {
        return accountRepository.getLocalPassword(context)
    }

    sealed class State {
        object ShowLoading : State()
        object HideLoading : State()
        object FailedLoading : State()
        object WrongDataProvided : State()
        object Result : State()
    }
}
