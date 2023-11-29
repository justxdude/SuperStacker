package com.justxdude.superstacker.objects

import com.justxdude.superstacker.util.u
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

abstract class Reward(val chance: Double, var minAmount: Int, var maxAmount: Int) {
    abstract fun getRewardItem(amount: Int, fire: Boolean): ItemStack
    abstract val isAvailable: Boolean
    fun getRandomAmount(minimum: Int, maximum: Int): Int {
        return u.getRandomNumberBetween(minimum, maximum)
    }

    fun getEstimatedDrops(its: Int, looting: Int): Int {
        val bottom = Math.round(chance * minAmount * its).toInt()
        val top = Math.round(chance * (maxAmount + looting) * its).toInt()
        return u.getRandomNumberBetween(bottom, top)
    }

    companion object {
        var dud: ItemStack = ItemStack(Material.COAL, 1)
    }
}
