package com.mekanik.wiring.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagrams")
data class DiagramProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val manufacturer: String,
    val componentsJson: String,
    val cablesJson: String,
    val createdAt: Long,
    val updatedAt: Long
)
