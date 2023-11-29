package com.justxdude.superstacker.util

import org.bukkit.entity.Player
import java.time.Duration
import java.time.Instant
import java.util.*

class Debugger(private val name: String, private val p: Player, colour: String?, private val timeunit: TimeUnit) {
    private val instants: MutableList<Instant>
    private val colour: String?
    private val isMe: Boolean

    init {
        instants = ArrayList()
        isMe = p.uniqueId == UUID.fromString("4f8cc0a1-80c6-4e37-8c99-9645ad60836d")
        this.colour = u.cc(colour)
        if (isMe) addPoint()
    }

    fun addPoint() {
        if (isMe) instants.add(Instant.now())
    }

    fun finish() {
        if (isMe) {
            addPoint()
            p.sendMessage(timeMsg)
        }
    }

    private val timeMsg: String
        private get() {
            var msg = "$colour($name) ["
            val l = instants.size
            for (i in 0 until l - 1) msg += u.dc(
                between(
                    instants[i],
                    instants[i + 1]
                ).toDouble()
            ) + (timeunit.suffix + " + ")
            return msg.substring(0, msg.length - 3) + if (l > 2) " = " + u.dc(
                between(
                    instants[0],
                    instants[l - 1]
                ).toDouble()
            ) + timeunit.suffix + "]" else "]"
        }

    private fun between(a: Instant, b: Instant): Long {
        val d = Duration.between(a, b)
        return when (timeunit) {
            TimeUnit.MICRO -> d.toNanos() / 1000
            TimeUnit.MILLI -> d.toMillis()
            TimeUnit.NANO -> d.toNanos()
            else -> d.toMillis()
        }
    }

    enum class TimeUnit(val suffix: String) {
        NANO("ns"),
        MICRO("us"),
        MILLI("ms")

    }
}