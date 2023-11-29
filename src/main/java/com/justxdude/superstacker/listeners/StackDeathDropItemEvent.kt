package com.justxdude.superstacker.listeners

import org.bukkit.inventory.ItemStack

class StackDeathDropItemEvent(
    val drops: MutableCollection<ItemStack>): CustomEvent() {
}