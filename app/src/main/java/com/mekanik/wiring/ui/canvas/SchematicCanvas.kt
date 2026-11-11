package com.mekanik.wiring.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.mekanik.wiring.data.nativeengine.WireRouterNative
import com.mekanik.wiring.domain.model.ComponentPin
import com.mekanik.wiring.domain.model.MotorComponent
import com.mekanik.wiring.domain.model.PinSide
import com.mekanik.wiring.domain.model.WiringCable
import com.mekanik.wiring.ui.theme.GridDotColor
import com.mekanik.wiring.ui.theme.GridLineColor
import com.mekanik.wiring.ui.theme.NeutralBorder
import com.mekanik.wiring.ui.theme.NeutralSurface
import com.mekanik.wiring.ui.theme.NeutralSurfaceElevated
import com.mekanik.wiring.ui.theme.OledBlack
import com.mekanik.wiring.ui.theme.TextPrimary
import com.mekanik.wiring.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SchematicCanvas(
    modifier: Modifier = Modifier,
    components: List<MotorComponent>,
    cables: List<WiringCable>,
    selectedWireId: String?,
    highlightedNetPinIds: Set<String>,
    onComponentMove: (componentId: String, newX: Float, newY: Float) -> Unit,
    onWireCreated: (fromComponentId: String, fromPinId: String, toComponentId: String, toPinId: String) -> Unit,
    onWireSelected: (wireId: String?) -> Unit,
    onPinClicked: (componentId: String, pinId: String) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset(0f, 0f)) }

    // Wire drawing state
    var connectingStartPin by remember { mutableStateOf<Pair<String, ComponentPin>?>(null) }
    var connectingCurrentPos by remember { mutableStateOf<Offset?>(null) }
    var hoveredTargetPin by remember { mutableStateOf<Pair<String, ComponentPin>?>(null) }

    // Dragged component
    var draggedComponentId by remember { mutableStateOf<String?>(null) }

    fun screenToWorld(screenPos: Offset): Offset {
        return Offset(
            (screenPos.x - panOffset.x) / scale,
            (screenPos.y - panOffset.y) / scale
        )
    }

    fun findPinAtWorldPos(worldPos: Offset, tolerance: Float = 28f): Pair<String, ComponentPin>? {
        for (component in components) {
            for (pin in component.pins) {
                val pinCenter = getPinWorldPosition(component, pin)
                val dist = (worldPos - pinCenter).getDistance()
                if (dist <= tolerance) {
                    return Pair(component.id, pin)
                }
            }
        }
        return null
    }

    fun findComponentAtWorldPos(worldPos: Offset): MotorComponent? {
        for (component in components) {
            if (worldPos.x >= component.x && worldPos.x <= component.x + component.width &&
                worldPos.y >= component.y && worldPos.y <= component.y + component.height
            ) {
                return component
            }
        }
        return null
    }

    fun findWireAtWorldPos(worldPos: Offset, tolerance: Float = 16f): WiringCable? {
        for (cable in cables) {
            val fromComp = components.find { it.id == cable.fromComponentId } ?: continue
            val toComp = components.find { it.id == cable.toComponentId } ?: continue
            val fromPin = fromComp.pins.find { it.id == cable.fromPinId } ?: continue
            val toPin = toComp.pins.find { it.id == cable.toPinId } ?: continue

            val p1 = getPinWorldPosition(fromComp, fromPin)
            val p2 = getPinWorldPosition(toComp, toPin)

            // Simple distance check to segment
            val dist = distanceToSegment(worldPos, p1, p2)
            if (dist <= tolerance) {
                return cable
            }
        }
        return null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (connectingStartPin == null && draggedComponentId == null) {
                        scale = (scale * zoom).coerceIn(0.35f, 3.2f)
                        panOffset += pan
                    }
                }
            }
            .pointerInput(components, cables) {
                detectDragGestures(
                    onDragStart = { startScreenOffset ->
                        val worldPos = screenToWorld(startScreenOffset)
                        val pinHit = findPinAtWorldPos(worldPos)
                        if (pinHit != null) {
                            connectingStartPin = pinHit
                            connectingCurrentPos = worldPos
                        } else {
                            val compHit = findComponentAtWorldPos(worldPos)
                            if (compHit != null) {
                                draggedComponentId = compHit.id
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val worldAmount = dragAmount / scale
                        if (connectingStartPin != null) {
                            val currentWorld = screenToWorld(change.position)
                            connectingCurrentPos = currentWorld
                            hoveredTargetPin = findPinAtWorldPos(currentWorld, 32f)
                        } else if (draggedComponentId != null) {
                            val comp = components.find { it.id == draggedComponentId }
                            if (comp != null) {
                                val newX = (comp.x + worldAmount.x).coerceAtLeast(0f)
                                val newY = (comp.y + worldAmount.y).coerceAtLeast(0f)
                                onComponentMove(comp.id, newX, newY)
                            }
                        }
                    },
                    onDragEnd = {
                        val start = connectingStartPin
                        val target = hoveredTargetPin
                        if (start != null && target != null && start.first != target.first) {
                            onWireCreated(start.first, start.second.id, target.first, target.second.id)
                        }
                        connectingStartPin = null
                        connectingCurrentPos = null
                        hoveredTargetPin = null
                        draggedComponentId = null
                    },
                    onDragCancel = {
                        connectingStartPin = null
                        connectingCurrentPos = null
                        hoveredTargetPin = null
                        draggedComponentId = null
                    }
                )
            }
            .pointerInput(components, cables) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val worldPos = screenToWorld(tapOffset)
                        val pinHit = findPinAtWorldPos(worldPos)
                        if (pinHit != null) {
                            onPinClicked(pinHit.first, pinHit.second.id)
                            return@detectTapGestures
                        }
                        val wireHit = findWireAtWorldPos(worldPos)
                        if (wireHit != null) {
                            onWireSelected(wireHit.id)
                        } else {
                            onWireSelected(null)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGrid(scale, panOffset)

            // Render Wires
            for (cable in cables) {
                val fromComp = components.find { it.id == cable.fromComponentId } ?: continue
                val toComp = components.find { it.id == cable.toComponentId } ?: continue
                val fromPin = fromComp.pins.find { it.id == cable.fromPinId } ?: continue
                val toPin = toComp.pins.find { it.id == cable.toPinId } ?: continue

                val startWorld = getPinWorldPosition(fromComp, fromPin)
                val endWorld = getPinWorldPosition(toComp, toPin)

                val startScreen = worldToScreen(startWorld, scale, panOffset)
                val endScreen = worldToScreen(endWorld, scale, panOffset)

                val isHighlighted = (cable.id == selectedWireId) ||
                        highlightedNetPinIds.contains("${cable.fromComponentId}:${cable.fromPinId}") ||
                        highlightedNetPinIds.contains("${cable.toComponentId}:${cable.toPinId}")

                val hasNetFilter = highlightedNetPinIds.isNotEmpty() || selectedWireId != null
                val alpha = if (hasNetFilter) {
                    if (isHighlighted) 1.0f else 0.22f
                } else 1.0f

                val routePoints = WireRouterNative.route(
                    startWorld.x,
                    startWorld.y,
                    fromPin.side,
                    endWorld.x,
                    endWorld.y,
                    toPin.side
                )

                val screenPoints = routePoints.map { worldToScreen(it.toOffset(), scale, panOffset) }

                drawOrthogonalWire(
                    points = screenPoints,
                    primaryColor = Color(cable.wireColorHex).copy(alpha = alpha),
                    stripeColor = cable.stripeColorHex?.let { Color(it).copy(alpha = alpha) },
                    isHighlighted = isHighlighted,
                    scale = scale
                )
            }

            // Render Active Draft Wire during drag
            val activeStart = connectingStartPin
            val activePos = connectingCurrentPos
            if (activeStart != null && activePos != null) {
                val comp = components.find { it.id == activeStart.first }
                if (comp != null) {
                    val startWorld = getPinWorldPosition(comp, activeStart.second)
                    val startScreen = worldToScreen(startWorld, scale, panOffset)
                    val endScreen = worldToScreen(activePos, scale, panOffset)

                    val draftPath = Path().apply {
                        moveTo(startScreen.x, startScreen.y)
                        val midX = (startScreen.x + endScreen.x) / 2f
                        cubicTo(midX, startScreen.y, midX, endScreen.y, endScreen.x, endScreen.y)
                    }

                    drawPath(
                        path = draftPath,
                        color = Color(0xFF00E676),
                        style = Stroke(
                            width = 4.5f * scale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                        )
                    )
                }
            }

            // Render Components
            for (component in components) {
                val compScreenPos = worldToScreen(Offset(component.x, component.y), scale, panOffset)
                val compScreenSize = Size(component.width * scale, component.height * scale)

                drawComponentCard(
                    pos = compScreenPos,
                    size = compScreenSize,
                    component = component,
                    scale = scale,
                    isBeingDragged = (draggedComponentId == component.id)
                )

                // Render Pins on Component
                for (pin in component.pins) {
                    val pinWorldPos = getPinWorldPosition(component, pin)
                    val pinScreenPos = worldToScreen(pinWorldPos, scale, panOffset)
                    val isHovered = hoveredTargetPin?.second?.id == pin.id && hoveredTargetPin?.first == component.id
                    val isConnectingStart = connectingStartPin?.second?.id == pin.id && connectingStartPin?.first == component.id
                    val isNetActive = highlightedNetPinIds.contains("${component.id}:${pin.id}")

                    drawPinTerminal(
                        center = pinScreenPos,
                        pin = pin,
                        scale = scale,
                        isHovered = isHovered,
                        isConnectingStart = isConnectingStart,
                        isNetActive = isNetActive
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawGrid(scale: Float, panOffset: Offset) {
    val step = 40f * scale
    if (step < 12f) return

    val startX = (panOffset.x % step)
    val startY = (panOffset.y % step)

    var x = startX
    while (x < size.width) {
        drawLine(
            color = GridLineColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += step
    }

    var y = startY
    while (y < size.height) {
        drawLine(
            color = GridLineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun DrawScope.drawComponentCard(
    pos: Offset,
    size: Size,
    component: MotorComponent,
    scale: Float,
    isBeingDragged: Boolean
) {
    val cornerRadius = CornerRadius(10f * scale, 10f * scale)

    // Body
    drawRoundRect(
        color = if (isBeingDragged) NeutralSurfaceElevated else NeutralSurface,
        topLeft = pos,
        size = size,
        cornerRadius = cornerRadius
    )

    // Border
    drawRoundRect(
        color = if (isBeingDragged) Color(0xFF00E676) else NeutralBorder,
        topLeft = pos,
        size = size,
        cornerRadius = cornerRadius,
        style = Stroke(width = if (isBeingDragged) 2.5f * scale else 1.5f * scale)
    )

    // Header label
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = (13f * scale).coerceAtLeast(10f)
            isAntiAlias = true
            isFakeBoldText = true
        }
        drawText(
            component.name,
            pos.x + 12f * scale,
            pos.y + 24f * scale,
            paint
        )

        paint.apply {
            color = android.graphics.Color.rgb(160, 160, 160)
            textSize = (10f * scale).coerceAtLeast(8f)
            isFakeBoldText = false
        }
        drawText(
            component.codeModel,
            pos.x + 12f * scale,
            pos.y + 40f * scale,
            paint
        )
    }
}

private fun DrawScope.drawPinTerminal(
    center: Offset,
    pin: ComponentPin,
    scale: Float,
    isHovered: Boolean,
    isConnectingStart: Boolean,
    isNetActive: Boolean
) {
    val radius = if (isHovered || isConnectingStart || isNetActive) 7.5f * scale else 5f * scale
    val pinColor = when {
        isHovered -> Color(0xFF00E676)
        isConnectingStart -> Color(0xFFFFD600)
        isNetActive -> Color(0xFF00E5FF)
        else -> Color(0xFFE0E0E0)
    }

    // Outer Snap Ring
    if (isHovered || isConnectingStart || isNetActive) {
        drawCircle(
            color = pinColor.copy(alpha = 0.35f),
            radius = radius + 6f * scale,
            center = center
        )
    }

    // Solid Terminal
    drawCircle(
        color = pinColor,
        radius = radius,
        center = center
    )

    // Pin Label Text
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(200, 200, 200)
            textSize = (9f * scale).coerceAtLeast(7f)
            isAntiAlias = true
        }

        val textOffset = when (pin.side) {
            PinSide.LEFT -> Offset(-paint.measureText(pin.name) - 8f * scale, 3f * scale)
            PinSide.RIGHT -> Offset(8f * scale, 3f * scale)
            PinSide.TOP -> Offset(-paint.measureText(pin.name) / 2f, -8f * scale)
            PinSide.BOTTOM -> Offset(-paint.measureText(pin.name) / 2f, 14f * scale)
        }

        drawText(
            pin.name,
            center.x + textOffset.x,
            center.y + textOffset.y,
            paint
        )
    }
}

private fun DrawScope.drawOrthogonalWire(
    points: List<Offset>,
    primaryColor: Color,
    stripeColor: Color?,
    isHighlighted: Boolean,
    scale: Float
) {
    if (points.size < 2) return

    val wireWidth = if (isHighlighted) 6f * scale else 3.5f * scale

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }

    // Primary wire body
    drawPath(
        path = path,
        color = primaryColor,
        style = Stroke(
            width = wireWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Optional striped dual-tone wire
    if (stripeColor != null) {
        drawPath(
            path = path,
            color = stripeColor,
            style = Stroke(
                width = wireWidth * 0.55f,
                cap = StrokeCap.Butt,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f * scale, 12f * scale), 0f)
            )
        )
    }

    // Highlight glow border
    if (isHighlighted) {
        drawPath(
            path = path,
            color = Color(0xFF00E5FF).copy(alpha = 0.4f),
            style = Stroke(
                width = wireWidth + 6f * scale,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

private fun getPinWorldPosition(component: MotorComponent, pin: ComponentPin): Offset {
    return when (pin.side) {
        PinSide.LEFT -> Offset(component.x, component.y + component.height * pin.offsetFraction)
        PinSide.RIGHT -> Offset(component.x + component.width, component.y + component.height * pin.offsetFraction)
        PinSide.TOP -> Offset(component.x + component.width * pin.offsetFraction, component.y)
        PinSide.BOTTOM -> Offset(component.x + component.width * pin.offsetFraction, component.y + component.height)
    }
}

private fun worldToScreen(worldPos: Offset, scale: Float, panOffset: Offset): Offset {
    return Offset(
        worldPos.x * scale + panOffset.x,
        worldPos.y * scale + panOffset.y
    )
}

private fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
    val l2 = (b - a).getDistanceSquared()
    if (l2 == 0f) return (p - a).getDistance()
    val t = ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2
    val clampedT = t.coerceIn(0f, 1f)
    val proj = Offset(a.x + clampedT * (b.x - a.x), a.y + clampedT * (b.y - a.y))
    return (p - proj).getDistance()
}

private fun com.mekanik.wiring.domain.model.RoutePoint.toOffset(): Offset {
    return Offset(this.x, this.y)
}
