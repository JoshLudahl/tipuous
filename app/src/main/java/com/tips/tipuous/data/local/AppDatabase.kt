package com.tips.tipuous.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tips.tipuous.model.AdvancedSplit
import kotlinx.serialization.json.Json

class AdvancedSplitConverter {
    @TypeConverter
    fun fromAdvancedSplit(value: AdvancedSplit?): String? = value?.let { Json.encodeToString(it) }

    @TypeConverter
    fun toAdvancedSplit(value: String?): AdvancedSplit? = value?.let { Json.decodeFromString<AdvancedSplit>(it) }
}

@Database(
    entities = [ReceiptEntity::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(AdvancedSplitConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE receipts ADD COLUMN advanced_split_json TEXT")
                }
            }

        private val MIGRATION_3_4 =
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE receipts ADD COLUMN split_count INTEGER NOT NULL DEFAULT 1")
                }
            }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room
                    .databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "tipuous.db",
                    ).addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(false)
                    .allowMainThreadQueries() // keep API non-suspend for minimal changes
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
