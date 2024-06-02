package com.bigbratan.rayvue.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "local_storage")


class LocalStorageService @Inject constructor(
    @ApplicationContext val context: Context,
) {
    val gson = Gson()
    val dataStore = context.dataStore

    suspend fun saveData(
        key: String,
        value: Any
    ) {
        val json = gson.toJson(value)

        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = json
        }
    }

    inline fun <reified T> readData(
        key: String,
        defaultValue: T
    ): Flow<T> {
        val type = object : TypeToken<T>() {}.type

        return dataStore.data.map { preferences ->
            val json = preferences[stringPreferencesKey(key)] ?: return@map defaultValue

            gson.fromJson<T>(json, type) ?: defaultValue
        }
    }
}
