package com.justxdude.superstacker

import com.justxdude.superstacker.commands.GiveSpawnerCommand
import com.justxdude.superstacker.listeners.AmbientStackHandler
import com.justxdude.superstacker.listeners.EntityListener
import com.justxdude.superstacker.listeners.StackListener
import com.justxdude.superstacker.objects.LootTable
import com.justxdude.superstacker.objects.Reward
import com.justxdude.superstacker.objects.RewardBlock
import com.justxdude.superstacker.objects.Rewards
import com.justxdude.superstacker.util.Settings
import com.justxdude.superstacker.util.u
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.stream.Collectors

class SuperStacker : JavaPlugin() {
    var mobCap = 0
    var spawnerCap = 0
    var witherRoseMultiplier = 0
    private var spawnerLores: HashMap<EntityType, List<String>>? = null
    private var cleaner: ItemRemover? = null
    var cookedItems: MutableMap<Material, Material> = mutableMapOf()
    override fun onEnable() {
        instance = this
        cookedItems = mutableMapOf()
        EntityListener(this)
        StackListener(this)

        config.options().copyDefaults()
        saveDefaultConfig()
        Settings.setup()
        loadData()

        // Commands
        GiveSpawnerCommand()

        availableTypes = Arrays.stream(EntityType.entries.toTypedArray())
            .filter { e: EntityType ->
                (e.isSpawnable
                        && e.isAlive && e != EntityType.ARMOR_STAND) && e != EntityType.PLAYER
            }
            .collect(Collectors.toSet())
        typesString = (availableTypes as MutableSet<EntityType>?)?.stream()?.map { obj: EntityType -> obj.toString() }
            ?.collect(Collectors.toList())
        numberString = mutableListOf("1", "2", "3", "4", "5")
        registeredLootTables = HashMap()
        registerLootTables()
        registerCookedItems()
        AmbientStackHandler.start()
        ItemRemover().also { cleaner = it }.start(this)
    }

    private fun hasPlugin(name: String): Boolean {
        return Bukkit.getPluginManager().getPlugin(name) != null
    }

    fun loadData() {
        val c = config
        mobCap = 500
        spawnerCap = 300
        witherRoseMultiplier = 1
        spawnerLores = HashMap()
        if (c.contains("SpawnerLore")) {
            for (mobname in c.getConfigurationSection("SpawnerLore")!!.getKeys(false)) {
                var type: EntityType
                type = try {
                    EntityType.valueOf(mobname)
                } catch (ex: IllegalArgumentException) {
                    logger.severe("Unknown Entity while loading spawner lore from config: $mobname")
                    continue
                }
                spawnerLores!![type] = c.getStringList("SpawnerLore.$mobname").stream().map<Any>(u::cc).collect(
                    Collectors.toList()
                ) as List<String>
            }
        }
    }

    fun hasSpawnerLoreFor(type: EntityType): Boolean {
        return spawnerLores!!.containsKey(type)
    }

    fun getSpawnerLoreFor(type: EntityType): List<String> {
        return spawnerLores!![type]!!
    }

    /**
     * @return a list of the "mobs" that contained errors
     */
    private fun registerLootTables(): List<String> {
        val errors: MutableList<String> = ArrayList()
        val config = config
        for (t in config.getConfigurationSection("mobdrops")!!.getKeys(false)) {
            try {
                val type = EntityType.valueOf(t)
                val dropstring = config.getStringList("mobdrops.$t")
                val drops: MutableList<Reward> = ArrayList()
                for (s in dropstring) {
                    val split = s.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    drops.add(
                        RewardBlock(
                            split[0].toDouble(),
                            ItemStack(Material.valueOf(split[1])), split[2].toInt(), split[3].toInt()
                        )
                    )
                }
                registerLootTable(LootTable(type, Rewards(drops)))
            } catch (e: EnumConstantNotPresentException) {
                logger.severe("Error loading drops for $t")
                errors.add(t)
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                logger.severe("Error loading drops for $t")
                errors.add(t)
                e.printStackTrace()
            }
        }
        return errors
    }

    private fun registerCookedItems() {
        cookedItems = mutableMapOf()
        cookedItems[Material.CHICKEN] = Material.COOKED_CHICKEN
        cookedItems[Material.COD] = Material.COOKED_COD
        cookedItems[Material.BEEF] = Material.COOKED_BEEF
        cookedItems[Material.PORKCHOP] = Material.COOKED_PORKCHOP
        cookedItems[Material.RABBIT] = Material.COOKED_RABBIT
        cookedItems[Material.SALMON] = Material.COOKED_SALMON
        cookedItems[Material.MUTTON] = Material.COOKED_MUTTON
    }

    companion object {
        @JvmField
        var availableTypes: Collection<EntityType>? = null
        var typesString: List<String>? = null
        var numberString: List<String>? = null
        @JvmField
        var registeredLootTables: HashMap<EntityType, LootTable>? = null
        lateinit var instance: SuperStacker
        @JvmStatic
        fun get(): SuperStacker {
            return getPlugin(SuperStacker::class.java)
        }

        fun registerLootTable(loot: LootTable) {
            registeredLootTables!![loot.type] = loot
        }
    }
}
