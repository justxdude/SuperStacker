package com.justxdude.superstacker.objects

import com.justxdude.superstacker.SuperStacker
import com.justxdude.superstacker.util.Settings
import com.justxdude.superstacker.util.SpawnerUtil
import com.justxdude.superstacker.util.u
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.stream.Collectors


class SpawnerStack @JvmOverloads constructor(
    private val b: Block, type: EntityType? = if (isSpawner(b)) (b.state as CreatureSpawner).spawnedType else null) {
    private var hologram: ArmorStand? = null
    var spawnerType: EntityType? = type
    private var spawner: CreatureSpawner? = spawnerFromBlock
    init {
        try {
            if (isStack) hologram = b.world.getNearbyEntities(hologramSearchLocation, 0.5, 0.5, 0.5).stream()
                .filter { i: Entity ->
                    i.type == EntityType.ARMOR_STAND && i.persistentDataContainer
                        .has(
                            SpawnerUtil.hologramKey,
                            PersistentDataType.STRING
                        ) && i.persistentDataContainer.get(
                        SpawnerUtil.hologramKey,
                        PersistentDataType.STRING
                    ) == spawnerType.toString()
                }
                .collect(Collectors.toList())[0] as ArmorStand
        } catch (e: java.lang.IndexOutOfBoundsException) {
            hologram = null
        }
    }
    private val hologramLocation: Location
        get() {
            val l = location
            l.add(Vector(0.5, 0.0, 0.5))
            return l
        }
    private val hologramSearchLocation: Location
        get() {
            val l = hologramLocation
            l.add(Vector(0.0, 0.5, 0.0))
            return l
        }
    val blockAbove: Location
        get() {
            val l = location
            l.y = (l.blockY + 1).toDouble()
            return l
        }
    val location: Location
        get() = b.location

    fun getItem(amount: Int): ItemStack {
        return SpawnerUtil.getSpawner(spawnerType!!, amount)
    }

    val isStack: Boolean get() = isSpawner() && pDC.has(SpawnerUtil.containerKey, PersistentDataType.INTEGER)

    private fun isSpawner(): Boolean {
        return b.type == Material.SPAWNER
    }

    private val pDC: PersistentDataContainer get() = getSpawnerObject()!!.persistentDataContainer

    fun createStack(amount: Int) {
        if (!isStack) {
            spawner?.spawnedType = spawnerType
            spawner?.requiredPlayerRange = 32
            spawner?.spawnCount = 1
            createHologram()
            this.amount = amount
        }
    }

    val spawnerFromBlock: CreatureSpawner?
        get() {
            spawner = b.state as CreatureSpawner
            return if (isSpawner()) spawner else null
        }

    fun getSpawnerObject(): CreatureSpawner? {
        return if (spawner == null) spawnerFromBlock else spawner
    }

    fun addSpawners(amount: Int) {
        this.amount = this.amount + amount
    }

    fun removeOneSpawner() {
        removeSpawners(1)
    }

    fun removeSpawners(amount: Int) {
        val existing = this.amount
        if (existing <= amount) deleteSpawner() else this.amount = existing - amount
    }

    fun deleteSpawner() {
        pDC.remove(SpawnerUtil.containerKey)
        getSpawnerObject()!!.update()
        b.type = Material.AIR
        try {
            hologram!!.remove()
        } catch (e: NullPointerException) {
            SuperStacker.get().logger.severe("Failed to remove spawner hologram at ...")
        }
    }

    fun breakSpawner(player: Player) {
        val amount = if (player.isSneaking) 64.coerceAtMost(amount) else 1
        removeSpawners(amount)
        if(player.gameMode != GameMode.CREATIVE) player.giveOrDropItem(getItem(amount))
    }

    init {
        if (isSpawner()) {
            spawnerType = type ?: EntityType.PIG // Default EntityType
            spawner = spawnerFromBlock ?: throw IllegalStateException("Spawner block expected")
        }
        try {
            if (isStack) hologram = b.world.getNearbyEntities(hologramSearchLocation, 0.5, 0.5, 0.5).stream()
                .filter { i: Entity ->
                    i.type == EntityType.ARMOR_STAND && i.persistentDataContainer.has(
                        SpawnerUtil.hologramKey,
                        PersistentDataType.STRING
                    ) && i.persistentDataContainer.get(
                        SpawnerUtil.hologramKey,
                        PersistentDataType.STRING
                    ) == spawnerType.toString()
                }
                .collect(Collectors.toList())[0] as ArmorStand
        } catch (e: IndexOutOfBoundsException) {
            hologram = null
        }
    }

    private fun Player.giveOrDropItem(item: ItemStack) = inventory.addItem(item).forEach { (_, item) ->  world.dropItem(location, item)}

    var amount: Int
        get() = try {
            pDC.get(SpawnerUtil.containerKey, PersistentDataType.INTEGER)!!
        } catch (e: NullPointerException) {
            0
        }
        set(amount) {
            pDC.set(SpawnerUtil.containerKey, PersistentDataType.INTEGER, amount)
            getSpawnerObject()!!.update()
            updateHologram()
        }

    fun updateHologram() {
        try {
            hologram!!.customName = u.hc(
                Settings.spawnerPhysicalName!!.replace("%name%", SpawnerUtil.getName(spawnerType!!)!!).replace("%amount%", u.dc(amount))
            )
        } catch (e: NullPointerException) {
        }
    }

    fun createHologram() {
        hologram = b.world.spawnEntity(hologramLocation, EntityType.ARMOR_STAND) as ArmorStand
        hologram!!.isSmall = true
        hologram!!.isPersistent = true
        hologram!!.isInvulnerable = true
        hologram!!.persistentDataContainer.set(
            SpawnerUtil.hologramKey,
            PersistentDataType.STRING,
            spawnerType.toString()
        )
        hologram!!.isInvisible = true
        hologram!!.isCustomNameVisible = true
        hologram!!.setGravity(false)
    }

    companion object {
        private fun isSpawner(b: Block): Boolean {
            return b.type == Material.SPAWNER
        }
    }
}
