package com.example.persistenciadual.domain

enum class StorageEngine(val displayName: String) {
    SQL("SQLite"),
    NOSQL("NoSQL - JSON")
}