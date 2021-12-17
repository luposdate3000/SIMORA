/*
 *    Copyright 2010 Tyler Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
* Modified on June 3rd, 2021 by Johann Mantler.
* The code was translated into Kotlin and cut to the required parts.
* */
package simora.simulator_iot.models.geo

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 *
 *
 * Primary calculations and tools.
 *
 *
 *
 *
 * Note: distance calculations are done using the Haversine formula which uses a
 * spherical approximation of the Earth. Values are known to differ from reality
 * by as much as 0.3% so if complete accuracy is very important to you, you
 * should be using a different library. Furthermore, by default this library
 * uses the mean radius of the Earth (6371.009 km). If your calculations are
 * localized to a particular region of the Earth, there may be values to use for
 * this radius which will yield more accurate results.
 *
 *
 * @author Tyler Coles
 */
internal object LatLngTool {
    /**
     * Converts an angle measured in degrees to an approximately
     * equivalent angle measured in radians.  The conversion from
     * degrees to radians is generally inexact.
     *
     * @return the measurement of the angle `angleInDegrees`
     * in radians.
     */
    private fun toRadians(angleInDegrees: Double): Double {
        return angleInDegrees / 180.0 * PI
    }

    /**
     * Clamp latitude to +/- 90 degrees.
     *
     * @param latitude
     * in degrees.
     * @return the normalized latitude.
     */
    internal fun normalizeLatitude(latitude: Double): Double {
        return if (latitude > 0) {
            latitude.coerceAtMost(90.0)
        } else {
            latitude.coerceAtLeast(-90.0)
        }
    }

    /**
     * Convert longitude to be within the +/- 180 degrees range.
     *
     * @param longitude
     * in degrees.
     * @return the normalized longitude.
     * Returns positive infinity, or negative infinity.
     */
    internal fun normalizeLongitude(longitude: Double): Double {
        var longitudeResult = longitude % 360
        if (longitudeResult > 180) {
            val diff = longitudeResult - 180
            longitudeResult = -180 + diff
        } else if (longitudeResult < -180) {
            val diff = longitudeResult + 180
            longitudeResult = 180 + diff
        }
        return longitudeResult
    }

    internal fun getDistanceInMeters(latitudeA: Double, longitudeA: Double, latitudeB: Double, longitudeB: Double): Double {
        val lat1R = toRadians(latitudeA)
        val lat2R = toRadians(latitudeB)
        val dLatR = abs(lat2R - lat1R)
        val dLngR = abs(toRadians(longitudeB - longitudeA))
        val a = sin(dLatR / 2) * sin(dLatR / 2) + (
            cos(lat1R) * cos(lat2R) *
                sin(dLngR / 2) * sin(dLngR / 2)
            )
        val x = 2 * atan2(sqrt(a), sqrt(1 - a))
        val y = LatLngConfig.getEarthRadius(LengthUnit.METER)
        return x * y
    }
}
