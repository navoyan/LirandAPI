package lirand.api.menu

import lirand.api.dsl.menu.dynamic.chest.ChestMenu
import lirand.api.dsl.menu.fixed.chest.StaticChestMenu
import lirand.api.menu.slot.StaticSlot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

fun ChestMenu.calculateSlot(line: Int, slot: Int) = (line * 9 - 10) + slot
fun StaticChestMenu.calculateSlot(line: Int, slot: Int) = (line * 9 - 10) + slot

fun <S : StaticSlot<I>, I : Inventory> StaticMenu<S, I>.getSlotOrBaseSlot(slot: Int): S = slots[slot] ?: baseSlot


fun <I : Inventory> StaticMenu<*, I>.viewersFromPlayers(players: Set<Player>) =
	viewers.filterKeys { it in players }

fun <S : StaticSlot<I>, I : Inventory> StaticMenu<S, I>.slotsWithBaseSlot(): Collection<S> = slots.values + baseSlot

fun StaticMenu<*, *>.hasPlayer(player: Player) = viewers.containsKey(player)
fun StaticMenu<*, *>.takeIfHasPlayer(player: Player): StaticMenu<*, *>? = if (hasPlayer(player)) this else null

fun Inventory.isMenu() = holder is StaticMenu<*, *>

fun Inventory.asMenu(): StaticMenu<*, *>? = holder as? StaticMenu<*, *>

fun Player.getMenu(): StaticMenu<*, *>? {
	return openInventory.topInventory.asMenu()?.takeIfHasPlayer(this)
}

fun Menu<*, *>.putPlayerData(player: Player, key: String, value: Any) =
	playerData.getOrPut(player) { WeakHashMap() }.put(key, value)

fun Menu<*, *>.getPlayerData(player: Player, key: String): Any? = playerData[player]?.get(key)