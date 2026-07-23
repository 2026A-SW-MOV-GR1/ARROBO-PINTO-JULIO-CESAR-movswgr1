package com.example.persistenciadual.data.sql

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.persistenciadual.domain.TaskItem

class TaskSqliteHelper(
    context: Context
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        database: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Estrategia académica para la primera versión.
        // En producción se utilizarían migraciones sin pérdida de datos.
        database.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(database)
    }

    fun readAll(): List<TaskItem> {
        val result = mutableListOf<TaskItem>()

        val cursor = readableDatabase.query(
            TABLE_TASKS,
            arrayOf(
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_DESCRIPTION,
                COLUMN_COMPLETED,
                COLUMN_CREATED_AT
            ),
            null,
            null,
            null,
            null,
            "$COLUMN_CREATED_AT DESC"
        )

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow(COLUMN_ID)
            val titleIndex = it.getColumnIndexOrThrow(COLUMN_TITLE)
            val descriptionIndex =
                it.getColumnIndexOrThrow(COLUMN_DESCRIPTION)
            val completedIndex =
                it.getColumnIndexOrThrow(COLUMN_COMPLETED)
            val createdAtIndex =
                it.getColumnIndexOrThrow(COLUMN_CREATED_AT)

            while (it.moveToNext()) {
                result.add(
                    TaskItem(
                        id = it.getLong(idIndex),
                        title = it.getString(titleIndex),
                        description = it.getString(descriptionIndex),
                        completed = it.getInt(completedIndex) == 1,
                        createdAt = it.getLong(createdAtIndex)
                    )
                )
            }
        }

        return result
    }

    fun insert(item: TaskItem): Long {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_COMPLETED, if (item.completed) 1 else 0)
            put(COLUMN_CREATED_AT, item.createdAt)
        }

        return writableDatabase.insertOrThrow(
            TABLE_TASKS,
            null,
            values
        )
    }

    fun update(item: TaskItem): Int {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_COMPLETED, if (item.completed) 1 else 0)
            put(COLUMN_CREATED_AT, item.createdAt)
        }

        return writableDatabase.update(
            TABLE_TASKS,
            values,
            "$COLUMN_ID = ?",
            arrayOf(item.id.toString())
        )
    }

    fun delete(id: Long): Int {
        return writableDatabase.delete(
            TABLE_TASKS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    companion object {
        private const val DATABASE_NAME = "tasks_sql.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TASKS = "tasks"

        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_COMPLETED = "completed"
        private const val COLUMN_CREATED_AT = "created_at"
    }
}