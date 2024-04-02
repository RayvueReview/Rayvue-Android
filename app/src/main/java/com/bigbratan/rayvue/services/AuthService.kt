package com.bigbratan.rayvue.services

import android.annotation.SuppressLint
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val userService: UserService,
    private val accountService: AccountService,
    private val firebaseStorageService: FirebaseStorageService,
    private val reviewsService: ReviewsService,
) {
    private val auth = Firebase.auth

    fun skipAccount() {

    }

    @SuppressLint("RestrictedApi")
    suspend fun createAccount(
        isReviewer: Boolean,
    ) {
        val email = accountService.getEmail()
        val password = accountService.getPassword()
        val userName = accountService.getName()

        if (email != null && password != null && userName != null) {
            val authResult = auth.createUserWithEmailAndPassword(
                email,
                password
            ).await()
            val userId = authResult.user?.uid
            val userData = hashMapOf(
                "id" to userId,
                "isReviewer" to isReviewer,
                "userName" to userName
            )

            userId?.let {
                firebaseStorageService.addDocument(
                    collection = "users",
                    documentId = it,
                    data = userData,
                )
                userService.fetchUser()
                accountService.removeAllData()
            }
        } else {
            null
        }
    }

    @SuppressLint("RestrictedApi")
    suspend fun checkEmail(
        email: String,
    ): Boolean {
        val signInMethods = auth.fetchSignInMethodsForEmail(email).await().signInMethods

        return !signInMethods.isNullOrEmpty()
    }

    fun checkCode(
        inviteCode: Int,
    ): Boolean {
        return inviteCode == 123456789
    }

    @SuppressLint("RestrictedApi")
    suspend fun deleteAccount() {
        val user = firebaseStorageService.getCurrentUser()
        val userId = user?.uid

        userId?.let {
            firebaseStorageService.deleteDocument(
                collection = "users",
                documentId = it,
            )
            reviewsService.deleteReviews(userId = userId)
            userService.removeUser()
            user.delete().await()
        }
    }

    @SuppressLint("RestrictedApi")
    suspend fun enterAccount(
        email: String,
        password: String,
    ): AuthResult {
        val authResult = auth.signInWithEmailAndPassword(
            email,
            password
        ).await()
        val userId = authResult.user?.uid

        userId?.let {
            userService.fetchUser()
        }

        return authResult
    }

    @SuppressLint("RestrictedApi")
    fun exitAccount() {
        auth.signOut()
        userService.removeUser()
    }

    suspend fun updateName(
        userName: String,
    ) {
        val userId = firebaseStorageService.getCurrentUser()?.uid

        userId?.let {
            firebaseStorageService.updateDocument(
                collection = "users",
                documentId = it,
                field = "userName",
                value = userName,
            )
            userService.fetchUser()
        }
    }
}