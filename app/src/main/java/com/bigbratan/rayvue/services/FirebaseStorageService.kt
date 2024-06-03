package com.bigbratan.rayvue.services

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
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

    suspend inline fun <reified T : Any> getDocumentsRepeatedly(
        collectionId: String,
        documentFields: Array<String>,
        filters: Map<String, Any> = emptyMap(),
        ids: List<String>? = null,
        orderBy: String? = null,
        direction: Query.Direction = Query.Direction.ASCENDING,
        limit: Long,
        startAfter: DocumentSnapshot? = null
    ): Pair<List<T>, DocumentSnapshot?> {
        var query: Query = db.collection(collectionId)

        if (ids != null)
            query = query.whereIn(FieldPath.documentId(), ids)

        filters.forEach { (field, value) ->
            query = query.whereEqualTo(field, value)
        }

        query = query.limit(limit)

        if (orderBy != null)
            query = query.orderBy(orderBy, direction)

        if (startAfter != null)
            query = query.startAfter(startAfter)

        val querySnapshot = query.get().await()
        val documents = querySnapshot.documents
        val lastSnapshot = documents.lastOrNull()

        val list = documents.mapNotNull { document ->
            documentFields.associateWith { document.get(it) }
                .filterValues { it != null }
                .takeIf { it.isNotEmpty() }
                ?.let { convertToObject<T>(it) }
        }

        return list to lastSnapshot
    }

    suspend inline fun <reified T : Any> getDocuments(
        collectionId: String,
        documentFields: Array<String>,
        filters: Map<String, Any> = emptyMap(),
        orderBy: String? = null,
        direction: Query.Direction = Query.Direction.ASCENDING,
        limit: Long? = null
    ): List<T> {
        var query: Query = db.collection(collectionId)

        filters.forEach { (field, value) ->
            query = query.whereEqualTo(field, value)
        }

        if (orderBy != null)
            query = query.orderBy(orderBy, direction)

        if (limit != null)
            query = query.limit(limit)

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            documentFields.associateWith { document.get(it) }
                .filterValues { it != null }
                .takeIf { it.isNotEmpty() }
                ?.let { convertToObject<T>(it) }
        }
    }

    suspend inline fun <reified T : Any> searchDocuments(
        collectionId: String,
        documentFields: Array<String>,
        searchField: String? = null,
        searchQuery: String? = null,
    ): List<T> {
        var query: Query = db.collection(collectionId)

        if (!searchField.isNullOrEmpty() && !searchQuery.isNullOrEmpty()) {
            val endQuery = searchQuery.lowercase()
                .filter { it.isLetterOrDigit() }
                .let { it.substring(0, it.length - 1) + it.last().inc() }

            query = query.whereGreaterThanOrEqualTo(searchField, searchQuery)
                .whereLessThan(searchField, endQuery)
        }

        val querySnapshot = query.get().await()

        return querySnapshot.documents.mapNotNull { document ->
            documentFields.associateWith { document.get(it) }
                .filterValues { it != null }
                .takeIf { it.isNotEmpty() }
                ?.let { convertToObject<T>(it) }
        }
    }

    suspend inline fun <reified T : Any> getDocument(
        collectionId: String,
        documentId: String,
        documentFields: Array<String>,
    ): T {
        val documentSnapshot = db.collection(collectionId)
            .document(documentId)
            .get()
            .await()
        val fieldsMap = documentFields.associateWith { fieldName ->
            documentSnapshot.get(fieldName)
        }.filterValues { it != null }

        return convertToObject<T>(fieldsMap)
    }

    suspend fun getDocumentAsList(
        collectionId: String,
        documentId: String,
        documentField: String
    ): List<String> {
        val documentSnapshot = db.collection(collectionId)
            .document(documentId)
            .get()
            .await()
        val data = documentSnapshot.get(documentField)

        return if (data is List<*>) {
            @Suppress("UNCHECKED_CAST")
            data as List<String>
        } else {
            Log.e(
                "getDocumentAsList",
                "Expected a List for field '$documentField' but got ${data?.javaClass?.simpleName}"
            )
            emptyList()
        }
    }

    suspend fun addDocument(
        collectionId: String,
        documentId: String,
        data: Any,
    ) {
        db.collection(collectionId)
            .document(documentId)
            .set(data)
            .await()
    }

    suspend fun updateDocument(
        collectionId: String,
        documentId: String,
        field: String,
        value: Any,
    ) {
        db.collection(collectionId)
            .document(documentId)
            .update(field, value)
            .await()
    }

    suspend fun addOrUpdateDocument(
        collectionId: String,
        documentId: String,
        data: Any,
    ) {
        db.collection(collectionId)
            .document(documentId)
            .set(data, SetOptions.merge())
            .await()
    }

    suspend fun deleteDocument(
        collectionId: String,
        documentId: String,
    ) {
        db.collection(collectionId)
            .document(documentId)
            .delete()
            .await()
    }

    suspend fun deleteDocuments(
        collectionId: String,
        documentField: String,
        value: String,
    ) {
        val querySnapshot = db.collection(collectionId)
            .whereEqualTo(documentField, value)
            .get()
            .await()

        for (document in querySnapshot.documents) {
            db.collection(collectionId).document(document.id).delete().await()
        }
    }

    inline fun <reified T : Any> convertToObject(
        fieldsMap: Map<String, Any?>
    ): T {
        return Gson().fromJson(Gson().toJson(fieldsMap), T::class.java)
    }
}