package com.mekanik.wiring.data.nativeengine

import android.util.Log
import com.mekanik.wiring.domain.model.PinSide
import com.mekanik.wiring.domain.model.RoutePoint

object WireRouterNative {

    private const val TAG = "WireRouterNative"
    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("mekanik_router")
            isNativeLoaded = true
            Log.i(TAG, "Native library 'mekanik_router' successfully loaded.")
        } catch (e: UnsatisfiedLinkError) {
            isNativeLoaded = false
            Log.w(TAG, "Native library not found, utilizing pure Kotlin Manhattan fallback router.", e)
        }
    }

    private external fun calculateRoute(
        startX: Float,
        startY: Float,
        startSide: Int,
        endX: Float,
        endY: Float,
        endSide: Int,
        obstacleArray: FloatArray?
    ): FloatArray?

    fun route(
        startX: Float,
        startY: Float,
        startSide: PinSide,
        endX: Float,
        endY: Float,
        endSide: PinSide,
        obstacles: List<Float> = emptyList()
    ): List<RoutePoint> {
        val startSideInt = when (startSide) {
            PinSide.LEFT -> 0
            PinSide.TOP -> 1
            PinSide.RIGHT -> 2
            PinSide.BOTTOM -> 3
        }

        val endSideInt = when (endSide) {
            PinSide.LEFT -> 0
            PinSide.TOP -> 1
            PinSide.RIGHT -> 2
            PinSide.BOTTOM -> 3
        }

        if (isNativeLoaded) {
            try {
                val obsArray = if (obstacles.isNotEmpty()) obstacles.toFloatArray() else null
                val result = calculateRoute(
                    startX,
                    startY,
                    startSideInt,
                    endX,
                    endY,
                    endSideInt,
                    obsArray
                )
                if (result != null && result.size >= 4) {
                    val points = mutableListOf<RoutePoint>()
                    for (i in 0 until result.size - 1 step 2) {
                        points.add(RoutePoint(result[i], result[i + 1]))
                    }
                    return points
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during native route computation, switching to fallback", e)
            }
        }

        // Resilient Pure Kotlin Fallback
        return calculateKotlinManhattan(startX, startY, startSide, endX, endY, endSide)
    }

    private fun calculateKotlinManhattan(
        startX: Float,
        startY: Float,
        startSide: PinSide,
        endX: Float,
        endY: Float,
        endSide: PinSide,
        leadOffset: Float = 28f
    ): List<RoutePoint> {
        val points = mutableListOf<RoutePoint>()
        points.add(RoutePoint(startX, startY))

        val leadStart = when (startSide) {
            PinSide.LEFT -> RoutePoint(startX - leadOffset, startY)
            PinSide.TOP -> RoutePoint(startX, startY - leadOffset)
            PinSide.RIGHT -> RoutePoint(startX + leadOffset, startY)
            PinSide.BOTTOM -> RoutePoint(startX, startY + leadOffset)
        }
        points.add(leadStart)

        val leadEnd = when (endSide) {
            PinSide.LEFT -> RoutePoint(endX - leadOffset, endY)
            PinSide.TOP -> RoutePoint(endX, endY - leadOffset)
            PinSide.RIGHT -> RoutePoint(endX + leadOffset, endY)
            PinSide.BOTTOM -> RoutePoint(endX, endY + leadOffset)
        }

        val startHorizontal = (startSide == PinSide.LEFT || startSide == PinSide.RIGHT)
        val endHorizontal = (endSide == PinSide.LEFT || endSide == PinSide.RIGHT)

        if (startHorizontal && endHorizontal) {
            val midX = (leadStart.x + leadEnd.x) / 2f
            points.add(RoutePoint(midX, leadStart.y))
            points.add(RoutePoint(midX, leadEnd.y))
        } else if (!startHorizontal && !endHorizontal) {
            val midY = (leadStart.y + leadEnd.y) / 2f
            points.add(RoutePoint(leadStart.x, midY))
            points.add(RoutePoint(leadEnd.x, midY))
        } else if (startHorizontal && !endHorizontal) {
            points.add(RoutePoint(leadEnd.x, leadStart.y))
        } else {
            points.add(RoutePoint(leadStart.x, leadEnd.y))
        }

        points.add(leadEnd)
        points.add(RoutePoint(endX, endY))
        return points
    }
}
