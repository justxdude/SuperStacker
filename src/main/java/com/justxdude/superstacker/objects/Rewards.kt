package com.justxdude.superstacker.objects

import java.util.*
import java.util.stream.Collectors

class Rewards(rewards: List<Reward>?) {
    val allRewards: List<Reward>
    private val certainRewards: List<Reward>
    private val chanceRewards: MutableList<Reward>
    private val areChanceRewards: Boolean
    private val random = Random()

    constructor(vararg rewards: Reward?) : this(Arrays.asList<Reward>(*rewards))

    init {
        allRewards = ArrayList(rewards)
        certainRewards = allRewards.stream().filter { e: Reward -> e.chance == 1.0 }.collect(Collectors.toList())
        chanceRewards = ArrayList(allRewards)
        chanceRewards.removeAll(certainRewards)
        areChanceRewards = chanceRewards.size > 0
    }

    val randomRewards: List<Reward>
        get() {
            val selected: MutableList<Reward> = ArrayList()
            selected.addAll(certainRewards)
            val rand = random.nextDouble()
            if (areChanceRewards) for (r in chanceRewards) if (rand <= r.chance) selected.add(r)
            return selected
        }

    fun getRandomRewards(mobs: Int): List<Reward> {
        val rewards: MutableList<Reward> = ArrayList()
        for (i in 0 until mobs) rewards.addAll(randomRewards)
        return rewards
    }
}
