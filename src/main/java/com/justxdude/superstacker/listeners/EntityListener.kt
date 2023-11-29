package com.justxdude.superstacker.listeners

import com.justxdude.superstacker.SuperStacker
import com.justxdude.superstacker.objects.EntityStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import java.util.*

class EntityListener(plugin: SuperStacker) : Listener {
    private val plugin: SuperStacker

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        this.plugin = plugin
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        if (e.entityType == EntityType.PLAYER) return
        if (e.cause == DamageCause.WITHER || e.cause == DamageCause.FIRE) {
            val loc = e.entity.location
            if (loc.y % 1 > 0.8) loc.y = loc.y + 1
            val potentialrose = loc.block.type
            if (potentialrose == Material.WITHER_ROSE && plugin.witherRoseMultiplier > 1) {
                e.damage = e.damage * plugin.witherRoseMultiplier
            }
        }
    }
    @EventHandler
    fun silverfishBurrowListener(e: EntityChangeBlockEvent) {
        val type = e.entityType
        if (type == EntityType.SILVERFISH) {
            val s = EntityStack((e.entity as LivingEntity))
            if (s.isStacked) e.isCancelled = true
        }
    }

    @EventHandler
    fun golemCreatePreventer(e: CreatureSpawnEvent) {
        val BuildReasons =
            Arrays.asList(SpawnReason.BUILD_IRONGOLEM, SpawnReason.BUILD_SNOWMAN, SpawnReason.BUILD_WITHER)
        if (BuildReasons.contains(e.spawnReason)) e.isCancelled = true
    }

    @EventHandler
    fun OmenOmitter(e: EntityPotionEffectEvent) {
        if (e.cause == EntityPotionEffectEvent.Cause.PATROL_CAPTAIN) e.isCancelled = true
    }

    @EventHandler
    fun witchDrinkPotion(e: EntityPotionEffectEvent) {
        if (e.entityType == EntityType.WITCH && e.cause == EntityPotionEffectEvent.Cause.ATTACK) e.isCancelled = true
    }
}
