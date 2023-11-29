package com.justxdude.superstacker.commands

import com.justxdude.networkapi.commands.Command
import com.justxdude.superstacker.util.MiniItemBuilder
import com.justxdude.superstacker.util.SpawnerUtil
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class GiveSpawnerCommand : Command("gs") {
    override fun canExecute(player: Player, args: Array<String>): Boolean {
        return true
    }

    override fun execute(player: Player, args: Array<String>) {
        player.giveOrDropItem(SpawnerUtil.getSpawner(EntityType.CHICKEN, 1))
    }

    override fun onTabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return emptyList()
    }
    private fun Player.giveOrDropItem(item: ItemStack) = inventory.addItem(item).forEach { (_, item) ->  world.dropItem(location, item)}

}