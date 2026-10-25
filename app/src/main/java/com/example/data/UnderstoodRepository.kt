package com.example.data

import kotlinx.coroutines.flow.Flow

class UnderstoodRepository(private val understoodDao: UnderstoodDao) {
    val allHistory: Flow<List<UnderstoodItem>> = understoodDao.getAllHistory()
    val favorites: Flow<List<UnderstoodItem>> = understoodDao.getFavorites()

    suspend fun insertItem(item: UnderstoodItem): Long {
        return understoodDao.insertItem(item)
    }

    suspend fun toggleFavorite(id: Int, currentFav: Boolean) {
        understoodDao.updateFavorite(id, !currentFav)
    }

    suspend fun deleteItem(id: Int) {
        understoodDao.deleteItem(id)
    }

    suspend fun clearHistory() {
        understoodDao.clearAllHistory()
    }
}
