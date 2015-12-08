/**
 * Backports of the Java 1.8 methods to Kotlin extension methods
 */

package com.emberjs.utils

import java.util.concurrent.TimeUnit

val Process.isAlive: Boolean
    get() {
        try {
            exitValue()
            return false
        } catch (e: IllegalThreadStateException) {
            return true
        }
    }

fun Process.waitFor(timeout: Long, unit: TimeUnit): Boolean {
    val startTime = System.nanoTime()
    var rem = unit.toNanos(timeout)

    do {
        if (!isAlive)
            return true

        if (rem > 0)
            Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(rem) + 1, 100))

        rem = unit.toNanos(timeout) - (System.nanoTime() - startTime);

    } while (rem > 0)

    return false
}

