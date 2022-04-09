package lirand.api.dsl.menu.exposed

import lirand.api.dsl.menu.builders.dynamic.chest.ChestMenuDSL
import lirand.api.dsl.menu.builders.fixed.chest.StaticChestMenuDSL
import lirand.api.dsl.menu.exposed.fixed.StaticMenu
import lirand.api.dsl.menu.exposed.fixed.StaticSlot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

fun ChestMenuDSL.calculateSlot(line: Int, slot: Int) = (line * 9 - 10) + slot
fun StaticChestMenuDSL.calculateSlot(line: Int, slot: Int) = (line * 9 - 10) + slot

fun <S : StaticSlot<I>, I : Inventory> StaticMenu<S, I>.getSlotOrBaseSlot(slot: Int): S = slots[slot] ?: baseSlot


fun <I : Inventory> StaticMenu<*, I>.getViewersFromPlayers(players: Collection<Player>) =
	viewers.filterKeys { it in players }

fun <S : StaticSlot<I>, I : Inventory> StaticMenu<S, I>.getSlotsWithBaseSlot(): Collection<S> = slots.values + baseSlot

fun StaticMenu<*, *>.hasPlayer(player: Player) = viewers.containsKey(player)
fun StaticMenu<*, *>.takeIfHasPlayer(player: Player): StaticMenu<*, *>? = if (hasPlayer(player)) this else null

fun Inventory.isMenu() = holder is StaticMenu<*, *>
fun Inventory.asMenu(): StaticMenu<*, *>? = holder as? StaticMenu<*, *>

fun Player.getMenu(): StaticMenu<*, *>? {
	return openInventory.topInventory.asMenu()?.takeIfHasPlayer(this)
}

fun StaticMenu<*, *>.putPlayerData(player: Player, key: String, value: Any) =
	playerData.getOrPut(player) { WeakHashMap() }.put(key, value)

fun StaticMenu<*, *>.getPlayerData(player: Player, key: String): Any? = playerData[player]?.get(key)