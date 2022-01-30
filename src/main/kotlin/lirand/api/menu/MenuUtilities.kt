package lirand.api.menu

import lirand.api.dsl.menu.dynamic.chest.ChestMenu
import lirand.api.dsl.menu.fixed.chest.StaticChestMenu
import lirand.api.menu.slot.StaticSlot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

fun StaticChestMenu.calculateSlot(line: Int, slot: Int) = calculateStartLine(line) + slot
fun StaticChestMenu.calculateStartLine(line: Int) = calculateEndLine(line) - 9
fun StaticChestMenu.calculateEndLine(line: Int) = line * 9

fun ChestMenu.calculateSlot(line: Int, slot: Int) = calculateStartLine(line) + slot
fun ChestMenu.calculateStartLine(line: Int) = calculateEndLine(line) - 9
fun ChestMenu.calculateEndLine(line: Int) = line * 9

fun rawSlot(slot: Int) = slot - 1

fun <S : StaticSlot> StaticMenu<S>.getSlotOrBaseSlot(slot: Int): S = slots[slot] ?: baseSlot

val StaticMenu<*>.rangeOfSlots: IntRange
	get() = when (this) {
		is ChestMenu -> 1..calculateEndLine(lines)
		is StaticChestMenu -> 1..calculateEndLine(lines)
		else -> 1..2
	}


fun StaticMenu<*>.viewersFromPlayers(players: Set<Player>) = viewers.filterKeys { it in players }

fun <S : StaticSlot> StaticMenu<S>.slotsWithBaseSlot(): Collection<S> = slots.values + baseSlot

fun StaticMenu<*>.hasPlayer(player: Player) = viewers.containsKey(player)
fun StaticMenu<*>.takeIfHasPlayer(player: Player): StaticMenu<*>? = if (hasPlayer(player)) this else null

fun Inventory.isMenu() = holder is StaticMenu<*>

fun Inventory.asMenu(): StaticMenu<*>? = holder as? StaticMenu<*>

fun Player.getMenu(): StaticMenu<*>? {
	return openInventory.topInventory.asMenu()?.takeIfHasPlayer(this)
}

fun Menu<*>.putPlayerData(player: Player, key: String, value: Any) =
	playerData.getOrPut(player) { WeakHashMap() }.put(key, value)

fun Menu<*>.getPlayerData(player: Player, key: String): Any? = playerData[player]?.get(key)