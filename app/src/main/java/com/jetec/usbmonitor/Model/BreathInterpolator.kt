package com.jetec.usbmonitor.Model

import android.animation.TimeInterpolator
import kotlin.math.pow
import kotlin.math.sin

class BreathInterpolator :TimeInterpolator {

    override fun getInterpolation(input: Float): Float {
        val x = 6*input
        val k = 1.0f/3
        val t = 6
        val n = 1
        val PI = 3.1415926f
        var output:Float = 0f

        if (x >= ((n - 1) * t) && x < ((n - (1 - k)) * t)) {
            output = (0.5 * sin
                (PI / (k * t) * (x - k * t / 2 - (n - 1) * t).toDouble()) + 0.5).toFloat()
        } else if (x >= (n - (1 - k)) * t && x < n * t) {
           output = (0.5 * sin
               (PI / ((1 - k) * t) * (x - (3 - k) * t / 2 - (n - 1) * t).toDouble()) + 0.5)
               .pow(
               2.0
           ).toFloat()
        }
        return output
    }
}