package com.justxdude.superstacker.customevents

import com.justxdude.superstacker.objects.EntityStack
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

class StackDeathEvent(val stack: EntityStack, var amountToKill: Int, val deathCause: DamageCause, val killer: Player?) :
    CustomEvent() {

    val entity: LivingEntity
        get() = stack.entity
}
