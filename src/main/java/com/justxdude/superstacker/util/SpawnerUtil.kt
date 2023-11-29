package com.justxdude.superstacker.util

import com.justxdude.superstacker.SuperStacker
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.block.CreatureSpawner
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

object SpawnerUtil {
    var hologramKey: NamespacedKey =
        NamespacedKey(SuperStacker.get(SuperStacker::class.java), "Hologram")
    var containerKey: NamespacedKey =
        NamespacedKey(SuperStacker.get(SuperStacker::class.java), "StackedSpawner")
    private val plugin: SuperStacker = SuperStacker.get(SuperStacker::class.java)
    fun getSpawner(type: EntityType, amount: Int): ItemStack {
        val item = ItemStack(Material.SPAWNER, amount)
        val meta = item.itemMeta
        meta!!.setDisplayName(u.hc(Settings.spawnerItemName!!.replace("%name%".toRegex(), getName(type)!!)))
        if (plugin.hasSpawnerLoreFor(type)) meta.lore = plugin.getSpawnerLoreFor(type)
        val cs = (meta as BlockStateMeta?)!!.blockState as CreatureSpawner
        cs.spawnedType = type
        (meta as BlockStateMeta?)!!.blockState = cs
        item.setItemMeta(meta)
        return item
    }

    fun setType(spawner: Block, type: EntityType?) {
        val block = spawner.state as CreatureSpawner
        block.spawnedType = type
        block.update()
    }

    fun getType(spawner: ItemStack): EntityType? {
        assert(spawner.type == Material.SPAWNER)
        val c = (spawner.itemMeta as BlockStateMeta?)!!.blockState as CreatureSpawner
        return c.spawnedType
    }

    @JvmStatic
	fun getName(type: EntityType): String? {
        return u.capitaliseFirstLetters(type.toString().replace("_".toRegex(), " "))
    }
}
