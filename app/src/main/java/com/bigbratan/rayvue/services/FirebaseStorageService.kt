package com.bigbratan.rayvue.services

import android.util.Log
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
        filters: Map<String, Any> = emptyMap(),
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<T>, DocumentSnapshot?> {
        var query: Query = db.collection(collection).limit(limit)

        for ((field, value) in filters) {
            query = query.whereEqualTo(field, value)
        }

        startAfter?.let {
            query = query.startAfter(it)
        }

        val querySnapshot = query.get().await()
        val list = mutableListOf<T>()
        var lastSnapshot: DocumentSnapshot? = null

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
            lastSnapshot = document
        }
        Log.d("mydata - firebase service", "${Pair(list, lastSnapshot)}")
        return Pair(list, lastSnapshot)
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

    suspend inline fun <reified T : Any> getDocument(
        collection: String,
        documentId: String,
        documentFields: Array<String>,
    ): T {
        val documentSnapshot = db.collection(collection)
            .document(documentId)
            .get()
            .await()

        val fieldsMap = documentFields.associateWith { fieldName ->
            documentSnapshot.get(fieldName)
        }.filterValues { it != null }

        return convertToObject<T>(fieldsMap)
    }

    inline fun <reified T : Any> convertToObject(
        fieldsMap: Map<String, Any?>
    ): T {
        return Gson().fromJson(Gson().toJson(fieldsMap), T::class.java)
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