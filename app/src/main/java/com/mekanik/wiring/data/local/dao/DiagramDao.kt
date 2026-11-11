package com.mekanik.wiring.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mekanik.wiring.data.local.entity.DiagramProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagramDao {

    @Query("SELECT * FROM diagrams ORDER BY updatedAt DESC")
    fun getAllDiagrams(): Flow<List<DiagramProjectEntity>>

    @Query("SELECT * FROM diagrams WHERE id = :id")
    suspend fun getDiagramById(id: String): DiagramProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(diagram: DiagramProjectEntity): Long

    @Query("DELETE FROM diagrams WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM diagrams")
    suspend fun deleteAll(): Int
}
