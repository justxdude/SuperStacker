package com.justxdude.superstacker.listeners

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

open class CustomEvent : Event() {
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }
}
