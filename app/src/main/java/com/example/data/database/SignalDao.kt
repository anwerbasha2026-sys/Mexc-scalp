package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY timestamp DESC")
    fun getAllSignals(): Flow<List<SignalEntity>>

    @Query("SELECT * FROM signals WHERE status = 'ACTIVE' ORDER BY timestamp DESC")
    fun getActiveSignals(): Flow<List<SignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity): Long

    @Update
    suspend fun updateSignal(signal: SignalEntity)

    @Delete
    suspend fun deleteSignal(signal: SignalEntity)

    @Query("DELETE FROM signals")
    suspend fun clearAllSignals()
}
