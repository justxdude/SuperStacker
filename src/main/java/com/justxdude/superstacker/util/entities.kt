package com.justxdude.superstacker.util

import com.justxdude.superstacker.SuperStacker
import com.justxdude.superstacker.objects.EntityStack
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.*
import java.util.stream.Collectors

object entities {
    private val plugin: SuperStacker = SuperStacker.get(SuperStacker::class.java)
    private fun getNearby(e: LivingEntity): MutableList<LivingEntity> {
        return e.getNearbyEntities(8.0, 8.0, 8.0).stream()
            .filter { x: Entity -> x.type == e.type && (x as LivingEntity).health > 0 && x.isValid() }
            .map { x: Entity -> x as LivingEntity }
            .collect(Collectors.toList())
    }

    /**
     * gets all nearby stacks of same type in order oldest to youngest.
     * This INCLUDES the entity e but EXCLUDES mobs that are already stacked to max stack size
     * @param e
     * @return
     */
    fun getEligibleStacksSortedByAge(e: LivingEntity): List<EntityStack> {
        val nearby = getNearby(e)
        nearby.add(e)
        return nearby
            .stream()
            .filter { l: LivingEntity? -> EntityStack.isStacked(l!!) && EntityStack.getStackAmount(l) < plugin.mobCap }
            .sorted { a: LivingEntity, b: LivingEntity -> b.ticksLived.compareTo(a.ticksLived) }
            .map { entity: LivingEntity? -> EntityStack(entity!!) }
            .collect(Collectors.toList())
    }

    /**
     *
     * @param e the living entity
     * @return a list of entity UUIDs that were removed as a cause of the operation.
     */
    fun stackNearby(e: LivingEntity): List<UUID> {
        val removed: MutableList<UUID> = ArrayList()
        val nearby = getEligibleStacksSortedByAge(e)
        var totalmobs = 0
        for (s in nearby) totalmobs += s.amount
        for (s in nearby) {
            if (totalmobs <= 0) {
                removed.add(s.entity.uniqueId)
                s.destroyStack()
                continue
            }
            val amount = Math.min(totalmobs, plugin.mobCap)
            s.amount = amount
            totalmobs -= amount
        }
        return removed
    }
}
