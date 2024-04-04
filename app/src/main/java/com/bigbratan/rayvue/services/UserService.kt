package com.bigbratan.rayvue.services;


import com.bigbratan.rayvue.models.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(DelicateCoroutinesApi::class)
@Singleton
class UserService @Inject constructor(
    private val firebaseStorageService: FirebaseStorageService,
) {
    val user = MutableStateFlow<User?>(null)

    val isUserLoggedIn: Boolean
        get() = firebaseStorageService.getCurrentUser() != null

    init {
        GlobalScope.launch(Dispatchers.IO) {
            if (isUserLoggedIn)
                fetchUser()
        }
    }

    suspend fun fetchUser() {
        user.value = firebaseStorageService.getCurrentUser()?.uid?.let { userId ->
            firebaseStorageService.getDocuments<User>(
                collection = "users",
                documentFields = arrayOf(
                    "id",
                    "userName",
                    "isReviewer",
                ),
                filters = mapOf("id" to userId)
            ).first()
        }
    }

    fun removeUser() {
        user.value = User(
            id = null,
            userName = null,
            isReviewer = false,
        )
    }
}
