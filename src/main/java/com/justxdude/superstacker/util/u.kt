package com.justxdude.superstacker.util

import com.justxdude.superstacker.SuperStacker
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

object u {
    fun translateHexColorCodes( /*String startTag, String endTag,*/
                                message: String
    ): String {
        val COLOR_CHAR = '\u00A7'
        val hexPattern = Pattern.compile( /*startTag*/"#" + "([A-Fa-f0-9]{6})" /*+ endTag*/)
        val matcher = hexPattern.matcher(message)
        val buffer = StringBuffer(message.length + 4 * 8)
        while (matcher.find()) {
            val group = matcher.group(1)
            matcher.appendReplacement(
                buffer, COLOR_CHAR.toString() + "x"
                        + COLOR_CHAR + group[0] + COLOR_CHAR + group[1]
                        + COLOR_CHAR + group[2] + COLOR_CHAR + group[3]
                        + COLOR_CHAR + group[4] + COLOR_CHAR + group[5]
            )
        }
        return matcher.appendTail(buffer).toString()
    }

    fun hc(s: String?): String? {
        return if (s == null) null else translateHexColorCodes(cc(s))
    }

    fun cc(s: String?): String {
        return ChatColor.translateAlternateColorCodes('&', s!!)
    }

    fun dc(value: Int): String {
        val pattern = "###,###,###"
        val decimalFormat = DecimalFormat(pattern)
        return decimalFormat.format(value.toLong())
    }

    fun dc(value: Double): String {
        val pattern = "###,###,###.##"
        val decimalFormat = DecimalFormat(pattern)
        return decimalFormat.format(value)
    }

    /**
     * @param commands a list of possible commands
     * @param Input the players input on the given argument
     * @return all items on the commands list that start with the given input, most often used for tab completing.
     */
    fun TabCompleter(Input: String, commands: List<String>): List<String> {
        val wordsThatStartWithArg: MutableList<String> = ArrayList()
        for (x in commands) if (x.lowercase(Locale.getDefault())
                .startsWith(Input.lowercase(Locale.getDefault()))
        ) wordsThatStartWithArg.add(x)
        return wordsThatStartWithArg
    }

    /**
     * @param input Input from the given argument
     * @param cmds All available commands
     * @return all items on the commands list that start with the given input, most often used for tab completing.
     */
    fun TabCompleter(input: String, vararg cmds: String): List<String> {
        val wordsThatStartWithArg: MutableList<String> = ArrayList()
        for (x in cmds) if (x.lowercase(Locale.getDefault())
                .startsWith(input.lowercase(Locale.getDefault()))
        ) wordsThatStartWithArg.add(x)
        return wordsThatStartWithArg
    }

    fun capitaliseFirstLetters(s: String): String {
        val words = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var returnable = ""
        for (x in words) {
            val firstletter = x.substring(0, 1)
            val notfirstletter = x.substring(1).lowercase(Locale.getDefault())
            returnable =
                (if (returnable.length == 0) returnable else "$returnable ") + firstletter.uppercase(Locale.getDefault()) + notfirstletter
        }
        return returnable
    }

    fun itemInMainHand(p: Player): ItemStack {
        return p.inventory.itemInMainHand
    }

    fun send(p: Player, s: String?) {
        p.sendMessage(cc(s))
    }

    fun isMaterial(s: String?): Boolean {
        return Material.matchMaterial(s!!) != null
    }

    fun getMaterial(s: String?): Material? {
        return Material.matchMaterial(s!!)
    }

    fun hasInventorySpace(p: Player): Boolean {
        return p.inventory.firstEmpty() != -1
    }

    fun isPlayer(s: CommandSender?): Boolean {
        return s is Player
    }

    fun isPlayer(name: String?): Boolean {
        return Bukkit.getPlayer(name!!) != null
    }

    fun getPlayer(s: String?): Player? {
        return Bukkit.getPlayer(s!!)
    }

    fun getNicerName(m: Material): String {
        return capitaliseFirstLetters(m.toString().lowercase(Locale.getDefault()).replace("_".toRegex(), " "))
    }

    fun getNicerName(item: ItemStack): String {
        return getNicerName(item.type)
    }

    fun isItem(item: ItemStack?): Boolean {
        return item != null && item.type != Material.AIR
    }

    fun bc(s: String?) {
        Bukkit.getPlayer("MLGPenguin")!!.sendMessage(hc(s)!!)
    }

    fun isValidEntityType(type: String): Boolean {
        return try {
            val t = EntityType.valueOf(type.uppercase(Locale.getDefault()))
            SuperStacker.availableTypes!!.contains(t)
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: NullPointerException) {
            false
        }
    }

    val allPlayersNames: List<String>
        get() {
            val names: MutableList<String> = ArrayList()
            for (p in Bukkit.getOnlinePlayers()) names.add(p.name)
            return names
        }
    val onlinePlayers: List<Player>
        get() = ArrayList(Bukkit.getOnlinePlayers())

    fun getRandomNumberBetween(lower: Int, higher: Int): Int {
        val rand = Random()
        return lower + rand.nextInt(higher + 1 - lower)
    }

    fun isInt(s: String): Boolean {
        return try {
            s.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun getInt(s: String): Int {
        return s.toInt()
    }

    fun isDouble(s: String): Boolean {
        return try {
            s.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun pluralise(amount: Int, singularName: String): String {
        return if (amount == 1) "$amount $singularName" else amount.toString() + " " + singularName + "s"
    }

    fun pluralise(amount: Long, singularName: String): String {
        return if (amount == 1L) "$amount $singularName" else amount.toString() + " " + singularName + "s"
    }
}
