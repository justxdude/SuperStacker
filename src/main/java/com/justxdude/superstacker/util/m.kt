package com.justxdude.superstacker.util

object m {
    // messages
    private val prefix = prefix()
    fun prefix(): String? {
        return u.hc("prefix ")
    }

    var noPermission: String? = null
    var invalidPlayerSelf: String? = null
    var unknownCommand: String? = null
    var invalidnumber: String? = null
    fun setup() {
        noPermission = msg("&cYou do not have permission to do this")
        invalidPlayerSelf = msg("&cYou need to be a player to use this command")
        unknownCommand = msg("&cUnknown Command")
        invalidnumber = msg("&cPlease specify a valid number")
    }

    fun s(): String? {
        return msg("")
    }

    fun invalidPlayerOther(Name: String): String? {
        return msg("&c$Name is not a player")
    }

    private fun msg(msg: String): String? {
        return u.hc(prefix + msg)
    }
}
