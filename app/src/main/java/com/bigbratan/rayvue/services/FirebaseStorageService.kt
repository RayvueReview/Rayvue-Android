package com.bigbratan.rayvue.services

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageService @Inject constructor() {
    private val auth = Firebase.auth
    val db = Firebase.firestore

    fun getCurrentUser() = auth.currentUser

    suspend inline fun <reified T : Any> getDocuments(
        collection: String,
        documentFields: Array<String>,
        filters: Map<String, Any> = emptyMap()
    ): List<T> {
        var query: Query = db.collection(collection)

        for ((field, value) in filters) {
            query = query.whereEqualTo(field, value)
        }

        val querySnapshot = query.get().await()
        val list = mutableListOf<T>()

        for (document in querySnapshot.documents) {
            val objectFields = mutableMapOf<String, Any>()

            for (documentField in documentFields) {
                val value = document.get(documentField)

                if (value != null) {
                    objectFields[documentField] = value
                }
            }

            val obj = convertToObject<T>(objectFields)
            list.add(obj)
        }
        return list
    }

    suspend inline fun <reified T : Any> searchDocuments(
        collection: String,
        documentFields: Array<String>,
        searchField: String? = null,
        searchQuery: String? = null,
        filters: Map<String, Any> = emptyMap()
    ): List<T> {
        var query: Query = db.collection(collection)

        for ((field, value) in filters) {
            query = query.whereEqualTo(field, value)
        }

        if (searchField != null && !searchQuery.isNullOrEmpty()) {
            val endQuery =
                searchQuery.lowercase().filter { it.isLetterOrDigit() }
                    .substring(0, searchQuery.length - 1) + searchQuery.last().inc()

            query = query
                .whereGreaterThanOrEqualTo(searchField, searchQuery)
                .whereLessThan(searchField, endQuery)
        }

        val querySnapshot = query.get().await()
        val list = mutableListOf<T>()

        for (document in querySnapshot.documents) {
            val objectFields = mutableMapOf<String, Any>()

            for (documentField in documentFields) {
                val value = document.get(documentField)

                if (value != null) {
                    objectFields[documentField] = value
                }
            }

            val obj = convertToObject<T>(objectFields)
            list.add(obj)
        }
        return list
    }

    inline fun <reified T : Any> convertToObject(fieldMap: Map<String, Any>): T {
        return Gson().fromJson(Gson().toJson(fieldMap), T::class.java)
    }

    suspend fun getDocument(
        collection: String,
        documentId: String,
    ): DocumentSnapshot {
        return db.collection(collection)
            .document(documentId)
            .get()
            .await()
    }

    suspend fun addDocument(
        collection: String,
        documentId: String,
        data: Any,
    ) {
        db.collection(collection)
            .document(documentId)
            .set(data)
            .await()
    }

    suspend fun updateDocument(
        collection: String,
        documentId: String,
        field: String,
        value: Any,
    ) {
        db.collection(collection)
            .document(documentId)
            .update(field, value)
            .await()
    }

    suspend fun deleteDocument(
        collection: String,
        documentId: String,
    ) {
        db.collection(collection)
            .document(documentId)
            .delete()
            .await()
    }

    suspend fun deleteDocuments(
        collection: String,
        internalId: String,
        matchingId: String,
    ) {
        val querySnapshot = db.collection(collection)
            .whereEqualTo(internalId, matchingId)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            db.collection(collection).document(document.id).delete().await()
        }
    }
}