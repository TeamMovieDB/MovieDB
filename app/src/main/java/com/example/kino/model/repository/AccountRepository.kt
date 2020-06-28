package com.example.kino.model.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.kino.R
import com.example.kino.model.account.LoginValidationData
import com.example.kino.model.account.Session
import com.example.kino.model.account.Success
import com.example.kino.model.account.Token
import com.example.kino.utils.ApiResponse
import com.example.kino.utils.PostApi
import com.example.kino.utils.constants.DEFAULT_VALUE
import com.example.kino.utils.constants.NULLABLE_VALUE
import com.example.kino.utils.constants.RESPONSE_ERROR
import io.reactivex.Single

interface AccountRepository {
    fun createSession(apiKey: String, token: Token): Single<ApiResponse<Session>>
    fun createToken(apiKey: String): Single<ApiResponse<Token>>
    fun validateWithLogin(apiKey: String, data: LoginValidationData): Single<ApiResponse<Token>>
    fun logOut(apiKey: String, context: Context): Single<ApiResponse<Success>>

    fun getLocalUsername(context: Context): String
    fun getLocalPassword(context: Context): String
    fun hasSessionId(context: Context): Boolean
    fun saveLoginData(context: Context, username: String, password: String, sessionId: String)
    fun deleteLoginData(context: Context)
    fun getLocalSessionId(context: Context): String

    fun setThemeState(themeState: Boolean, context: Context)
    fun getTheme(context: Context): Boolean
}

class AccountRepositoryImpl(
    private var service: PostApi,
    private var sharedPreferences: SharedPreferences
) : AccountRepository {

    override fun createSession(apiKey: String, token: Token): Single<ApiResponse<Session>> {
        return service.createSession(apiKey, token).map { response ->
            if (response.isSuccessful) {
                val session = response.body() ?: Session(null)
                ApiResponse.Success(session)
            } else ApiResponse.Error<Session>(RESPONSE_ERROR)
        }
    }

    override fun createToken(apiKey: String): Single<ApiResponse<Token>> {
        return service.createRequestToken(apiKey).map { response ->
            if (response.isSuccessful) {
                val token = response.body() ?: Token(null)
                ApiResponse.Success(token)
            } else {
                ApiResponse.Error<Token>(RESPONSE_ERROR)
            }
        }
    }

    override fun validateWithLogin(
        apiKey: String,
        data: LoginValidationData
    ): Single<ApiResponse<Token>> {
        return service.validateWithLogin(apiKey, data).map { response ->
            if (response.isSuccessful) {
                val token = response.body() ?: Token(null)
                ApiResponse.Success(token)
            } else ApiResponse.Error<Token>(RESPONSE_ERROR)
        }
    }

    override fun logOut(apiKey: String, context: Context): Single<ApiResponse<Success>> {
        val session = Session(getLocalSessionId(context))
        return service.deleteSession(apiKey, session).map { response ->
            if (response.isSuccessful) {
                ApiResponse.Success(response.body()!!)
            } else {
                ApiResponse.Error<Success>(RESPONSE_ERROR)
            }
        }
    }

    override fun getLocalPassword(context: Context): String {
        return if (sharedPreferences.contains(context.getString(R.string.password)))
            sharedPreferences.getString(
                context.getString(R.string.password), DEFAULT_VALUE
            ) as String
        else DEFAULT_VALUE
    }

    override fun getLocalUsername(context: Context): String {
        return if (sharedPreferences.contains(context.getString(R.string.username)))
            sharedPreferences.getString(
                context.getString(R.string.username), DEFAULT_VALUE
            ) as String
        else DEFAULT_VALUE
    }

    override fun hasSessionId(context: Context): Boolean {
        return sharedPreferences.contains(context.getString(R.string.session_id))
    }

    override fun getLocalSessionId(context: Context): String {
        return if (sharedPreferences.contains(context.getString(R.string.session_id))) {
            sharedPreferences.getString(
                context.getString(R.string.session_id), NULLABLE_VALUE
            ) as String
        } else NULLABLE_VALUE
    }

    override fun saveLoginData(
        context: Context, username: String, password: String, sessionId: String
    ) {
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.username), username)
        editor.putString(context.getString(R.string.session_id), sessionId)
        editor.putString(context.getString(R.string.password), password)
        editor.apply()
    }

    override fun deleteLoginData(context: Context) {
        val editor = sharedPreferences.edit()
        editor.remove(context.getString(R.string.session_id))
        editor.apply()
    }

    override fun setThemeState(themeState: Boolean, context: Context) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(context.getString(R.string.theme_state), themeState)
        editor.apply()
    }

    override fun getTheme(context: Context): Boolean {
        return sharedPreferences.getBoolean(context.getString(R.string.theme_state), false)
    }

}
