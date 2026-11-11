package com.mekanik.wiring.data.repository

import com.mekanik.wiring.domain.model.CircuitDiagnosticReport
import com.mekanik.wiring.domain.model.WiringDiagram
import kotlinx.coroutines.flow.Flow

interface WiringRepository {
    fun getAllSavedDiagrams(): Flow<List<WiringDiagram>>
    suspend fun saveDiagram(diagram: WiringDiagram): Result<Unit>
    suspend fun deleteDiagram(id: String): Result<Unit>
    suspend fun getDiagramById(id: String): WiringDiagram?
    fun runCircuitDiagnostic(diagram: WiringDiagram): CircuitDiagnosticReport
    fun exportDiagramJson(diagram: WiringDiagram): String
    fun importDiagramJson(jsonString: String): Result<WiringDiagram>
}
