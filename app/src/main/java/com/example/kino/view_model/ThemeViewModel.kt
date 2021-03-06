package com.example.kino.view_model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.example.kino.model.repository.AccountRepository

class ThemeViewModel(
    private val context: Context,
    private val accountRepository: AccountRepository
) : BaseViewModel() {
    val themeStateLiveData = MutableLiveData<Boolean>()

    fun setThemeState(themeState: Boolean) {
        accountRepository.setThemeState(themeState, context)
    }

    fun getTheme() {
        themeStateLiveData.value = accountRepository.getTheme(context)
    }
}