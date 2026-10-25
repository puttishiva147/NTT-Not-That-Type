package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UnderstoodDao {
    @Query("SELECT * FROM understood_items ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<UnderstoodItem>>

    @Query("SELECT * FROM understood_items WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<UnderstoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: UnderstoodItem): Long

    @Update
    suspend fun updateItem(item: UnderstoodItem)

    @Query("UPDATE understood_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Int, isFavorite: Boolean)

    @Query("DELETE FROM understood_items WHERE id = :id")
    suspend fun deleteItem(id: Int)

    @Query("DELETE FROM understood_items")
    suspend fun clearAllHistory()
}
