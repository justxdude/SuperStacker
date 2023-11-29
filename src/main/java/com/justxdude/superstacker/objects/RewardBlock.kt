package com.justxdude.superstacker.objects

import com.justxdude.superstacker.SuperStacker
import org.bukkit.inventory.ItemStack

class RewardBlock(chance: Double, Material: ItemStack, minAmount: Int, maxAmount: Int) :
    Reward(chance, minAmount, maxAmount) {
    private val item: ItemStack
    init {
        item = Material
    }

    override fun getRewardItem(amount: Int, fire: Boolean): ItemStack {
        val newItem: ItemStack = item.clone()
        newItem.setAmount(amount)
        if (fire) if (SuperStacker.instance.cookedItems.containsKey(newItem.type)) newItem.type = SuperStacker.instance.cookedItems[newItem.type]!!
        return newItem
    }

    override val isAvailable: Boolean
        get() = true
}
