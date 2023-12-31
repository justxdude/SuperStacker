package com.justxdude.superstacker.util

import com.justxdude.superstacker.SuperStacker
import org.bukkit.configuration.file.FileConfiguration

object Settings {
    private var c: FileConfiguration? = null
    var spawnerItemName: String? = null
    @JvmField
	var entityname: String? = null
    var spawnerPhysicalName: String? = null
    fun setup() {
        c = SuperStacker.get().config
        spawnerItemName = c?.getString("SpawnerItemName") ?: "DefaultName"
        entityname = c!!.getString("EntityName")
        spawnerPhysicalName = c!!.getString("SpawnerPhysicalName")
    }
}
