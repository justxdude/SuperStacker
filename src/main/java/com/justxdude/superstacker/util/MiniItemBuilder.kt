package com.justxdude.superstacker.util

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class MiniItemBuilder {
    private var mat: Material
    private var amount: Int
    private var lore: MutableList<String?>?
    private var meta: ItemMeta?
    private var enchants: MutableMap<Enchantment, Int>
    private var stack: ItemStack
    private var name: String? = null
    private var hideEnchants = false
    private var clearEnchants = false
    private var locname: String? = null
    private val persistentStringData = HashMap<NamespacedKey, String>()

    // CONSTRUCTORS
    constructor(type: Material) {
        mat = type
        amount = 1
        stack = ItemStack(type, amount)
        meta = stack.itemMeta
        enchants = HashMap()
        lore = ArrayList()
    }

    constructor(type: Material, amount: Int) {
        mat = type
        this.amount = amount
        stack = ItemStack(type, amount)
        meta = stack.itemMeta
        enchants = HashMap()
        lore = ArrayList()
    }

    constructor(`is`: ItemStack) {
        mat = `is`.type
        amount = `is`.amount
        stack = `is`
        meta = `is`.itemMeta
        enchants = if (`is`.itemMeta!!
                .enchants.size == 0
        ) HashMap() else `is`.itemMeta!!.enchants
        lore = meta!!.lore
    }

    // SETTERS
    fun setType(type: Material): MiniItemBuilder {
        mat = type
        return this
    }

    fun setName(name: String?): MiniItemBuilder {
        this.name = name
        return this
    }

    fun setAmount(amount: Int): MiniItemBuilder {
        this.amount = amount
        return this
    }

    fun hideEnchants(hide: Boolean): MiniItemBuilder {
        hideEnchants = hide
        return this
    }

    fun setEnchants(enchants: MutableMap<Enchantment, Int>): MiniItemBuilder {
        this.enchants = enchants
        return this
    }

    fun clearEnchants(clearEnchants: Boolean): MiniItemBuilder {
        this.clearEnchants = clearEnchants
        return this
    }

    fun addEnchant(enchant: Enchantment, level: Int): MiniItemBuilder {
        enchants[enchant] = level
        return this
    }

    fun addPersistentStringData(key: NamespacedKey, data: String): MiniItemBuilder {
        persistentStringData[key] = data
        return this
    }

    fun addLores(vararg lores: String?): MiniItemBuilder {
        if (lore == null) lore = ArrayList()
        for (x in lores) lore!!.add(u.cc(x))
        return this
    }

    fun setLocname(loc: String?): MiniItemBuilder {
        locname = loc
        return this
    }

    // BUILDER
    fun build(): ItemStack {
        stack.amount = amount
        stack.type = mat
        if (enchants.size > 0) {
            for (e in enchants.keys) {
                meta!!.addEnchant(e, enchants[e]!!, true)
            }
        }
        if (lore != null && lore!!.size > 0) meta!!.lore = lore
        if (clearEnchants) {
            for (e in meta!!.enchants.keys) meta!!.removeEnchant(e!!)
        }
        if (persistentStringData.size > 0) {
            for (key in persistentStringData.keys) {
                val c = meta!!.persistentDataContainer
                c.set(key, PersistentDataType.STRING, persistentStringData[key]!!)
            }
        }
        if (name != null) meta!!.setDisplayName(u.hc(name))
        if (hideEnchants) meta!!.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        if (locname != null) meta!!.setLocalizedName(locname)
        stack.setItemMeta(meta)
        return stack
    }
}
