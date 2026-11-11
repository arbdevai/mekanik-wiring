package com.mekanik.wiring.data.repository

import com.mekanik.wiring.data.local.dao.DiagramDao
import com.mekanik.wiring.data.local.entity.DiagramProjectEntity
import com.mekanik.wiring.domain.model.CircuitDiagnosticReport
import com.mekanik.wiring.domain.model.ComponentPin
import com.mekanik.wiring.domain.model.ComponentType
import com.mekanik.wiring.domain.model.DiagnosticItem
import com.mekanik.wiring.domain.model.Manufacturer
import com.mekanik.wiring.domain.model.MotorComponent
import com.mekanik.wiring.domain.model.PinRole
import com.mekanik.wiring.domain.model.WiringCable
import com.mekanik.wiring.domain.model.WiringDiagram
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WiringRepositoryImpl(
    private val diagramDao: DiagramDao
) : WiringRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    override fun getAllSavedDiagrams(): Flow<List<WiringDiagram>> {
        return diagramDao.getAllDiagrams().map { list ->
            list.map { entityToDomain(it) }
        }
    }

    override suspend fun saveDiagram(diagram: WiringDiagram): Result<Unit> {
        return try {
            val entity = domainToEntity(diagram)
            diagramDao.insertOrUpdate(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDiagram(id: String): Result<Unit> {
        return try {
            val rows = diagramDao.deleteById(id)
            if (rows > 0) Result.success(Unit) else Result.failure(Exception("Diagram tidak ditemukan"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDiagramById(id: String): WiringDiagram? {
        val entity = diagramDao.getDiagramById(id) ?: return null
        return entityToDomain(entity)
    }

    override fun runCircuitDiagnostic(diagram: WiringDiagram): CircuitDiagnosticReport {
        val missingList = mutableListOf<DiagnosticItem>()
        val cables = diagram.cables
        val components = diagram.components

        val connectedPinSet = mutableSetOf<String>()
        cables.forEach { cable ->
            connectedPinSet.add("${cable.fromComponentId}:${cable.fromPinId}")
            connectedPinSet.add("${cable.toComponentId}:${cable.toPinId}")
        }

        val cdi = components.find { it.type == ComponentType.CDI }
        val koil = components.find { it.type == ComponentType.KOIL }
        val kiprok = components.find { it.type == ComponentType.KIPROK }

        var isIgnitionComplete = false
        var isChargingComplete = false

        if (cdi != null) {
            val cdiPins = cdi.pins
            var hasGnd = false
            var hasPulser = false
            var hasPower = false
            var hasKoil = false

            cdiPins.forEach { pin ->
                val isConn = connectedPinSet.contains("${cdi.id}:${pin.id}")
                when (pin.role) {
                    PinRole.GND -> {
                        if (isConn) hasGnd = true
                        else missingList.add(DiagnosticItem(cdi.name, pin.name, false, pin.role, "Massa CDI belum terhubung ke rangka/aki"))
                    }
                    PinRole.PULSER -> {
                        if (isConn) hasPulser = true
                        else missingList.add(DiagnosticItem(cdi.name, pin.name, false, pin.role, "Sinyal pulser CDI belum terhubung dari spul"))
                    }
                    PinRole.SWITCHED_12V, PinRole.AC_IGNITION_SOURCE -> {
                        if (isConn) hasPower = true
                        else missingList.add(DiagnosticItem(cdi.name, pin.name, false, pin.role, "Sumber daya pengapian CDI belum tersambung"))
                    }
                    PinRole.COIL_OUT -> {
                        if (isConn) hasKoil = true
                        else missingList.add(DiagnosticItem(cdi.name, pin.name, false, pin.role, "Output CDI belum tersambung ke koil"))
                    }
                    else -> {}
                }
            }

            var koilHasSpark = false
            if (koil != null) {
                val sparkPin = koil.pins.find { it.role == PinRole.SPARK_OUT }
                if (sparkPin != null && connectedPinSet.contains("${koil.id}:${sparkPin.id}")) {
                    koilHasSpark = true
                } else {
                    missingList.add(DiagnosticItem(koil.name, "Kop Busi", false, PinRole.SPARK_OUT, "Koil belum terhubung ke busi"))
                }
            }

            isIgnitionComplete = hasGnd && hasPulser && hasPower && hasKoil && koilHasSpark
        }

        if (kiprok != null) {
            var kiprokGnd = false
            var kiprokAcIn = false
            var kiprokDcOut = false

            kiprok.pins.forEach { pin ->
                val isConn = connectedPinSet.contains("${kiprok.id}:${pin.id}")
                when (pin.role) {
                    PinRole.GND -> if (isConn) kiprokGnd = true
                    PinRole.CHARGE_AC -> if (isConn) kiprokAcIn = true
                    PinRole.BATTERY_12V -> if (isConn) kiprokDcOut = true
                    else -> {}
                }
            }

            isChargingComplete = kiprokGnd && kiprokAcIn && kiprokDcOut
        }

        val summary = when {
            isIgnitionComplete && isChargingComplete -> "Jalur Pengapian & Pengisian Berfungsi Normal"
            isIgnitionComplete -> "Jalur Pengapian Aktif (Pengisian Belum Lengkap)"
            else -> "Rangkaian Pengapian Belum Selesai (Api Busi Tidak Akan Keluar)"
        }

        return CircuitDiagnosticReport(
            isIgnitionComplete = isIgnitionComplete,
            isChargingComplete = isChargingComplete,
            missingConnections = missingList,
            summaryText = summary
        )
    }

    override fun exportDiagramJson(diagram: WiringDiagram): String {
        return json.encodeToString(diagram)
    }

    override fun importDiagramJson(jsonString: String): Result<WiringDiagram> {
        return try {
            val diagram = json.decodeFromString<WiringDiagram>(jsonString)
            Result.success(diagram)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun domainToEntity(diagram: WiringDiagram): DiagramProjectEntity {
        return DiagramProjectEntity(
            id = diagram.id,
            title = diagram.title,
            description = diagram.description,
            manufacturer = diagram.manufacturer.name,
            componentsJson = json.encodeToString(diagram.components),
            cablesJson = json.encodeToString(diagram.cables),
            createdAt = diagram.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun entityToDomain(entity: DiagramProjectEntity): WiringDiagram {
        val components = try {
            json.decodeFromString<List<MotorComponent>>(entity.componentsJson)
        } catch (e: Exception) {
            emptyList()
        }
        val cables = try {
            json.decodeFromString<List<WiringCable>>(entity.cablesJson)
        } catch (e: Exception) {
            emptyList()
        }
        val manufacturer = try {
            Manufacturer.valueOf(entity.manufacturer)
        } catch (e: Exception) {
            Manufacturer.HONDA
        }

        return WiringDiagram(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            manufacturer = manufacturer,
            components = components,
            cables = cables,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
