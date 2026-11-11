package com.mekanik.wiring.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ComponentPin(
    val id: String,
    val name: String,
    val role: PinRole,
    val side: PinSide,
    val offsetFraction: Float = 0.5f
)

@Serializable
data class MotorComponent(
    val id: String,
    val name: String,
    val codeModel: String,
    val type: ComponentType,
    val x: Float,
    val y: Float,
    val width: Float = 160f,
    val height: Float = 110f,
    val pins: List<ComponentPin>
)

@Serializable
data class RoutePoint(
    val x: Float,
    val y: Float
)

@Serializable
data class WiringCable(
    val id: String,
    val fromComponentId: String,
    val fromPinId: String,
    val toComponentId: String,
    val toPinId: String,
    val wireColorHex: Long,
    val stripeColorHex: Long? = null,
    val label: String,
    val gaugeMm: Float = 0.75f,
    val pathPoints: List<RoutePoint> = emptyList()
)

@Serializable
data class WiringDiagram(
    val id: String,
    val title: String,
    val description: String,
    val manufacturer: Manufacturer = Manufacturer.HONDA,
    val components: List<MotorComponent> = emptyList(),
    val cables: List<WiringCable> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class DiagnosticItem(
    val componentName: String,
    val pinName: String,
    val isConnected: Boolean,
    val role: PinRole,
    val diagnosticMessage: String
)

data class CircuitDiagnosticReport(
    val isIgnitionComplete: Boolean,
    val isChargingComplete: Boolean,
    val missingConnections: List<DiagnosticItem>,
    val summaryText: String
)
