package com.justxdude.superstacker.listeners

import com.justxdude.superstacker.SuperStacker
import com.justxdude.superstacker.customevents.StackDeathDropItemEvent
import com.justxdude.superstacker.customevents.StackDeathEvent
import com.justxdude.superstacker.objects.EntityStack
import com.justxdude.superstacker.objects.EntityStack.Companion.isStacked
import com.justxdude.superstacker.objects.SpawnerStack
import com.justxdude.superstacker.util.SpawnerUtil
import com.justxdude.superstacker.util.entities
import com.justxdude.superstacker.util.u
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.SpawnerSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.ceil

class StackListener(plugin: SuperStacker?) : Listener {
    @EventHandler
    fun onDeath(e: EntityDeathEvent) {
        val type = e.entityType
        val killed = e.entity
        val killer = killed.killer
        val holding = killer?.inventory?.itemInMainHand
        val looting =
            if (holding == null || !holding.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) 0 else holding.getEnchantmentLevel(
                Enchantment.LOOT_BONUS_MOBS
            )
        val fireAspect = holding != null && holding.containsEnchantment(Enchantment.FIRE_ASPECT) || killed.fireTicks > 0
        if (killed.lastDamageCause != null && killed.lastDamageCause!!.cause == DamageCause.VOID) return
        if (isStacked(killed)) {
            val killedStack = EntityStack(killed)
            val cause = killed.lastDamageCause!!.cause
            val amountToKill = if (cause == DamageCause.FALL) killedStack.amount else 1
            val event = StackDeathEvent(killedStack, amountToKill, cause, killer)
            // Call the event
            Bukkit.getServer().pluginManager.callEvent(event)
            val hasLootTable = SuperStacker.registeredLootTables!!.containsKey(type)
            val customDrops = if (hasLootTable) SuperStacker.registeredLootTables!![type]!!
                .getDrops(looting, fireAspect, event.amountToKill) else e.drops
            if (hasLootTable) {
                e.drops.clear()
                e.drops.addAll(customDrops)
            }
            // Call event
            Bukkit.getServer().pluginManager.callEvent(StackDeathDropItemEvent(e.drops))
            if (killedStack.amount > event.amountToKill) killedStack.respawnEntity(killedStack.amount - event.amountToKill)
        }
    }

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin!!)
    }

    @EventHandler
    fun onSpawn(e: SpawnerSpawnEvent) {
        val spawner = SpawnerStack(e.spawner.block)
        val spawnerAmount = spawner.amount
        var mobAmount: Int =
            u.getRandomNumberBetween(ceil(spawnerAmount * 0.75).toInt(), ceil(spawnerAmount * 1.75).toInt())
        if (mobAmount < 1) mobAmount = 1
        val spawned = e.entity as LivingEntity
        val spawnStack = EntityStack(spawned)
        spawnStack.createStack()
        spawnStack.amount = mobAmount
        val mobuuid = spawned.uniqueId
        val stacked: List<UUID> = entities.stackNearby(spawnStack.entity)
        if (!stacked.contains(mobuuid) && SPAWN_IN_WALLS.contains(spawned.type)) {
            spawned.teleport(setToMiddleOfBlock(spawned.location))
        }
    }

    private fun setToMiddleOfBlock(loc: Location): Location {
        loc.x = loc.blockX + 0.5
        loc.z = loc.blockZ + 0.5
        return loc
    }

    @EventHandler
    fun onPlace(e: BlockPlaceEvent) {
        val block = e.block
        if (block.type == Material.SPAWNER) {
            val holding = e.itemInHand
            val existing = SpawnerStack(e.blockAgainst)
            val placed = SpawnerStack(e.block, SpawnerUtil.getType(holding))
            if (e.isCancelled) return
            if (existing.isStack) {
                if (existing.spawnerType == placed.spawnerType) {
                    if (existing.amount < SuperStacker.get(SuperStacker::class.java).spawnerCap) {
                        val amount = if (e.player.isSneaking) Math.min(
                            holding.amount,
                            SuperStacker.get(SuperStacker::class.java).spawnerCap - existing.amount
                        ) else 1
                        e.isCancelled = true
                        if (e.player.gameMode != GameMode.CREATIVE) holding.amount = holding.amount - amount
                        existing.addSpawners(amount)
                    }
                }
            } else {
                var amount = 1
                if (e.player.isSneaking) {
                    amount = holding.amount
                    holding.amount = 0
                }
                placed.createStack(amount)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onBreak(e: BlockBreakEvent) {
        if (e.isCancelled) return
        if (e.block.type == Material.SPAWNER) {
            e.isCancelled = true
            val s = SpawnerStack(e.block)
            s.breakSpawner(e.player)
        }
    }

    @EventHandler
    fun maintainHolograms(e: EntityDamageEvent) {
        if (e.entity.type == EntityType.ARMOR_STAND) {
            val a = e.entity as ArmorStand
            if (a.isSmall) {
                if (a.persistentDataContainer.has<String, String>(
                        SpawnerUtil.hologramKey,
                        PersistentDataType.STRING
                    )
                ) e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun preventSpawnEggChange(e: PlayerInteractEvent) {
        if (e.clickedBlock != null && e.clickedBlock!!.type == Material.SPAWNER && e.item != null && e.item!!.type.toString()
                .contains("_SPAWN_EGG")
        ) e.isCancelled = true
    }

    companion object {
        private val SPAWN_IN_WALLS: HashSet<EntityType?> = object : HashSet<EntityType?>() {
            init { addAll(
                    listOf(EntityType.ZOMBIE, EntityType.HUSK, EntityType.PIGLIN, EntityType.WANDERING_TRADER))
            }
        }
    }
}
