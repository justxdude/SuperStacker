package com.justxdude.superstacker.listeners

import com.justxdude.superstacker.SuperStacker
import com.justxdude.superstacker.util.entities
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object AmbientStackHandler {
    var trackedStacks: MutableSet<UUID> = HashSet()
    fun add(e: LivingEntity) {
        trackedStacks.add(e.uniqueId)
    }

    fun remove(uuid: UUID) {
        trackedStacks.remove(uuid)
    }

    fun start() {
        object : BukkitRunnable() {
            override fun run() {
                val toRemove: MutableList<UUID> = ArrayList()
                for (uuid in trackedStacks) {
                    if (toRemove.contains(uuid)) continue
                    val e = Bukkit.getEntity(uuid)
                    if (e is LivingEntity && e.isValid) {
                        // Your logic with the valid LivingEntity
                        toRemove.addAll(entities.stackNearby(e))
                    } else {
                        toRemove.add(uuid)
                    }
                }
                trackedStacks.removeAll(toRemove.toSet())
            }
        }.runTaskTimer(SuperStacker.get(), 600, 600)
    }
}
