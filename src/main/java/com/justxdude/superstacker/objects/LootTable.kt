package com.justxdude.superstacker.objects

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack

class LootTable(val type: EntityType, private val rewards: Rewards) {

    val drops: List<ItemStack?>
        get() = getDrops(0, false)

    fun getDrops(looting: Int, fireAspect: Boolean): List<ItemStack?> {
        return getDrops(looting, fireAspect, 1)
    }

    fun getDrops(looting: Int, fireAspect: Boolean, mobs: Int): List<ItemStack?> {
        var rdrops: List<Reward?>? = null
        val drops: MutableList<ItemStack?> = ArrayList()
        if (mobs <= 5) {
            rdrops = rewards.getRandomRewards(mobs)
            for (r in rdrops) drops.add(getReward(r, looting, fireAspect))
        } else {
            rdrops = rewards.allRewards
            val list = HashMap<Material, Int>()
            for (r in rdrops!!) list[r!!.getRewardItem(1, fireAspect).type] = r.getEstimatedDrops(mobs, looting)
            for (m in list.keys) {
                var amount = list[m]!!
                while (amount > 0) {
                    val stacksize = if (amount > m.maxStackSize) m.maxStackSize else amount
                    drops.add(ItemStack(m, stacksize))
                    amount -= stacksize
                }
            }
        }
        return drops
    }

    private fun getReward(r: Reward?, looting: Int, FireAspect: Boolean): ItemStack? {
        return r!!.getRewardItem(r.getRandomAmount(r.minAmount, r.maxAmount + looting), FireAspect)
    }
}
