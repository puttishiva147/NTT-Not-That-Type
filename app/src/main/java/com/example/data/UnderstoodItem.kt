package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "understood_items")
data class UnderstoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val originalQuery: String,
    val primaryNumber: String,
    val numericValue: Long,
    val currencySymbol: String = "",
    val languageCode: String,
    val languageName: String,
    val scriptText: String,
    val phoneticText: String,
    val englishMeaning: String,
    val breakdown: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val source: String = "manual" // "screen", "voice", "search", "test"
)
