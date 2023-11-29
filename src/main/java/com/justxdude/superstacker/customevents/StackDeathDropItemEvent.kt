package com.justxdude.superstacker.customevents

import org.bukkit.inventory.ItemStack

class StackDeathDropItemEvent(
    val drops: MutableCollection<ItemStack>): CustomEvent() {
}