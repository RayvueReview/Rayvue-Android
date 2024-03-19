package com.bigbratan.rayvue.services

import com.bigbratan.rayvue.models.Account
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountService @Inject constructor() {
    private val account = MutableStateFlow<Account?>(null)

    fun addAuthData(
        email: String,
        password: String,
    ) {
        account.value = Account(
            email = email,
            password = password
        )
    }

    fun addName(
        userName: String,
    ) {
        account.value = account.value?.copy(userName = userName)
    }

    fun removeName() {
        account.value = account.value?.copy(userName = null)
    }

    fun removeAllData() {
        account.value = account.value?.copy(
            email = null,
            password = null,
            userName = null,
        )
    }

    fun getEmail(): String? {
        return account.value?.email
    }

    fun getPassword(): String? {
        return account.value?.password
    }

    fun getName(): String? {
        return account.value?.userName
    }
}