package com.justxdude.superstacker.objects

import com.justxdude.superstacker.SuperStacker
import com.justxdude.superstacker.listeners.AmbientStackHandler
import com.justxdude.superstacker.util.Settings
import com.justxdude.superstacker.util.SpawnerUtil.getName
import com.justxdude.superstacker.util.u
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.persistence.PersistentDataType

class EntityStack(var entity: LivingEntity) {
    val location: Location
        get() = entity.location
    val type: EntityType
        get() = entity.type

    fun createStack() {
        if (!isStacked && shouldStack(entity)) {
            amount = 1
            setEntityDefaults()
        }
    }

    fun shouldStack(e: LivingEntity?): Boolean {
        return e !is Player && e !is ArmorStand
    }

    fun safetyCheckStack() {
        if (!isStacked) amount = 1
    }

    fun addToStack(amount: Int) {
        if (isStacked) this.amount = this.amount + amount else {
            createStack()
            this.amount = amount + 1
        }
    }

    fun addEntityToStack(en: Entity) {
        val len = en as LivingEntity
        val s = EntityStack(len)
        if (len.type == entity.type) {
            addToStack(if (s.isStacked) s.amount else 1)
            len.remove()
        }
    }

    fun setEntity(e: LivingEntity, amount: Int, hasDefaults: Boolean) {
        entity = e
        if (!hasDefaults) setEntityDefaults()
        this.amount = amount
    }

    fun respawnEntity(amount: Int) {
        setEntity(spawnEntity(location, type), amount, true)
    }

    init {
        createStack()
    }

    private fun setEntityDefaults() {
        setEntityDefaults(entity)
    }

    var amount: Int
        get() {
            val c = entity.persistentDataContainer
            return c.get(amountKey, PersistentDataType.INTEGER)!!
        }
        set(amount) {
            entity.persistentDataContainer.set(amountKey, PersistentDataType.INTEGER, amount)
            updateName()
        }
    val isStacked: Boolean
        get() = isStacked(entity)

    fun removeFromStack(amount: Int) {
        val existing = this.amount
        if (amount >= existing) {
            entity.remove()
            return
        }
        this.amount = existing - amount
    }

    fun destroyStack() {
        entity.remove()
    }

    fun updateName() {
        entity.customName = u.hc(
            Settings.entityname?.replace("%name%".toRegex(), getName(type)!!)
                ?.replace("%amount%".toRegex(), u.dc(amount))
        )
        entity.isCustomNameVisible = true
    }

    companion object {
        private val amountKey: NamespacedKey =
            NamespacedKey(SuperStacker.get(), "StackAmount")

        /**
         * @param e
         * @return -1 if the mob is not stacked, the amount in the stack otherwise.
         */
        fun getStackAmount(e: LivingEntity): Int {
            val s = EntityStack(e)
            return if (!s.isStacked) -1 else s.amount
        }

        inline fun <reified T : LivingEntity> spawnEntity(loc: Location, type: EntityType): T {
            if (!T::class.java.isAssignableFrom(type.entityClass)) {
                throw IllegalArgumentException("Entity type does not match")
            }
            return loc.world!!.spawn(loc, type.entityClass as Class<T>) { entity ->
                setEntityDefaults(entity)
            }
        }


        private val maxHealths: HashMap<EntityType?, Int?> = object : HashMap<EntityType?, Int?>() {
            init {
                put(EntityType.DONKEY, 10)
                put(EntityType.HORSE, 10)
                put(EntityType.LLAMA, 10)
                put(EntityType.MULE, 16)
                put(EntityType.POLAR_BEAR, 20)
                put(EntityType.HOGLIN, 20)
                put(EntityType.GUARDIAN, 10)
                put(EntityType.WITHER, 100)
                put(EntityType.PIGLIN_BRUTE, 36)
            }
        }

        fun setEntityDefaults(e: LivingEntity) {
            AmbientStackHandler.add(e)
            val type = e.type
            val m = e as Mob
            // Make it so sweeping edge doesn't carry mobs away & increase kill speed
            e.setLastDamage(0.0)
            e.setMaximumNoDamageTicks(10)
            e.setNoDamageTicks(6)
            m.isAware = true
            e.setCanPickupItems(false)
            if (maxHealths.containsKey(type)) setMaxHealth(e, maxHealths[type]!!)
            if (e is Ageable && !e.isAdult) e.setAdult()
            if (e is Slime) e.size = 0
            if (e is PiglinAbstract) e.isImmuneToZombification = true
            if (e.getType() == EntityType.HOGLIN) (e as Hoglin).isImmuneToZombification = true
            if (e.getPassengers().size > 0) for (en in e.getPassengers()) en.remove()
            if (e.getType() == EntityType.ZOMBIE && !(e as Zombie).isAdult) {
                for (en in e.getNearbyEntities(1.0, 1.0, 1.0)) {
                    if (en is Chicken && en.getPassengers().contains(e)) {
                        en.remove()
                        break
                    }
                }
            }
            // e.getEquipment --> Remove it.
        }

        private fun setMaxHealth(e: LivingEntity, hp: Int) {
            e.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue = hp.toDouble()
            e.health = hp.toDouble()
        }

        @JvmStatic
		fun isStacked(e: LivingEntity): Boolean {
            return e.persistentDataContainer.has(amountKey, PersistentDataType.INTEGER)
        }
    }
}
