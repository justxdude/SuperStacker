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
                    val e: LivingEntity = Bukkit.getEntity(uuid) as LivingEntity
                    if (e == null || !e.isValid()) {
                        toRemove.add(uuid)
                        continue
                    }
                    toRemove.addAll(entities.stackNearby(e))
                }
                trackedStacks.removeAll(toRemove)
            }
        }.runTaskTimer(SuperStacker.get(SuperStacker::class.java), 600, 600)
    }
}
