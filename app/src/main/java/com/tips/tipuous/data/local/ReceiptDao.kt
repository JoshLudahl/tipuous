package com.tips.tipuous.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY date_epoch_millis DESC")
    fun getAllFlow(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts ORDER BY date_epoch_millis DESC")
    fun getAll(): List<ReceiptEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE id = :id")
    fun deleteById(id: String)
}
