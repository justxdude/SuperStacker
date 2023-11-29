package com.justxdude.superstacker

import com.justxdude.superstacker.SuperStacker.Companion.get
import com.justxdude.superstacker.objects.EntityStack
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.*
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors

class ItemRemover {
    fun start(plugin: SuperStacker?) {
        Bukkit.getScheduler().runTaskTimer(plugin!!, Runnable {
            val all: List<Item> = ArrayList(
                worldForRemoval!!.getEntitiesByClass(
                    Item::class.java
                )
            )
            // optimise this by having it check 3 items with this one call instead of 1 as this is the most hurtful operation ^
            if (all.isEmpty()) return@Runnable
            val discovered = all[ThreadLocalRandom.current().nextInt(all.size)]
            val around = getAround(discovered.location, 10)
            if (qualifies(discovered, around.size)) {
                val clearmobs = around.stream().noneMatch { entity: Entity? -> entity is Player }
                val toClear = filterRemovable(around, if (clearmobs) stackableMobFilter else ArrayList())
                purge(toClear)
                get(SuperStacker::class.java).logger.info("Random Entity Cleaner Removed a cluster of " + toClear.size + " entities at " + discovered.location.x + discovered.location.y + discovered.location.z)
            }
        }, 20, 20)
    }

    private val worldForRemoval: World?
        private get() = if (Bukkit.getWorld("SuperiorWorld") == null) Bukkit.getWorld("world") else Bukkit.getWorld("SuperiorWorld")

    companion object {
        private fun qualifies(item: Item, numSurrounding: Int): Boolean {
            return item.ticksLived > 400 && numSurrounding > 200
        }

        fun purge(ents: Collection<Entity>) {
            for (e in ents) e.remove()
        }

        val stackableMobFilter: List<EntityType>
            get() = ArrayList(SuperStacker.availableTypes)

        fun getRemovableEntities(filter: List<EntityType?>, radius: Int, location: Location?): List<Entity> {
            val worlds = if (location == null) Bukkit.getWorlds() else Arrays.asList(location.world)
            return if (radius == -1 || location == null) {
                val ens: MutableList<Entity> = ArrayList()
                worlds.stream().map { w: World -> w.entities }.forEach { list: List<Entity>? ->
                    ens.addAll(
                        list!!
                    )
                }
                ens.stream()
                    .filter { en: Entity ->
                        en.type == EntityType.DROPPED_ITEM || filter.contains(en.type) && EntityStack.isStacked(
                            en as LivingEntity
                        )
                    }
                    .collect(Collectors.toList())
            } else {
                ArrayList(
                    worlds[0].getNearbyEntities(
                        location,
                        radius.toDouble(),
                        radius.toDouble(),
                        radius.toDouble()
                    ) { en: Entity ->
                        en.type == EntityType.DROPPED_ITEM || filter.contains(en.type) && EntityStack.isStacked(
                            en as LivingEntity
                        )
                    })
            }
        }

        private fun getAround(loc: Location, radius: Int): List<Entity> {
            return ArrayList(
                loc.world!!.getNearbyEntities(loc, radius.toDouble(), radius.toDouble(), radius.toDouble())
            )
        }

        private fun filterRemovable(entities: List<Entity>, filter: List<EntityType>): List<Entity> {
            return entities.stream()
                .filter { en: Entity ->
                    en.type == EntityType.DROPPED_ITEM || filter.contains(en.type) && EntityStack.isStacked(
                        en as LivingEntity
                    )
                }
                .collect(Collectors.toList())
        }
    }
}
