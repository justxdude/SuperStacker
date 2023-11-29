package com.justxdude.superstacker.objects

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class Command(var sender: CommandSender, var args: Array<String>, var permission: String) {
    fun hasPermission(): Boolean {
        return sender.hasPermission(permission)
    }

    val isPlayer: Boolean
        get() = sender is Player

    fun getPlayer(): Player {
        return sender as Player
    }

    abstract fun execute()
}
