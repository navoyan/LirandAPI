package lirand.api.menu

import lirand.api.dsl.menu.dynamic.anvil.AnvilMenu
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.set

interface PlayerMenuEvent {
	val menu: StaticMenu<*, *>
	val player: Player

	fun putPlayerData(key: String, value: Any) {
		val map = menu.playerData[player]
		if (map != null) map[key] = value
		else menu.playerData[player] = WeakHashMap<String, Any>().apply { put(key, value) }
	}

	fun getPlayerData(key: String) = menu.playerData[player]?.get(key)

}

internal fun PlayerMenuEvent(
	menu: StaticMenu<*, *>,
	player: Player
): PlayerMenuEvent = PlayerMenuEventImpl(menu, player)

internal class PlayerMenuEventImpl(
	override val menu: StaticMenu<*, *>,
	override val player: Player
) : PlayerMenuEvent



interface PlayerInventoryMenuEvent<I : Inventory> : PlayerMenuEvent {
	val inventory: I

	fun close() = player.closeInventory()
}

fun <I : Inventory> PlayerInventoryMenuEvent(
	menu: StaticMenu<*, *>,
	player: Player,
	inventory: I
): PlayerInventoryMenuEvent<I> =
	PlayerInventoryMenuImpl<I>(
		menu, player, inventory
	)

internal class PlayerInventoryMenuImpl<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override val inventory: I
) : PlayerInventoryMenuEvent<I>



interface PlayerMenuCancellableEvent : PlayerMenuEvent {
	var canceled: Boolean
}


open class PlayerMenuInteractEvent<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override val inventory: I,
	override var canceled: Boolean
) : PlayerInventoryMenuEvent<I>, PlayerMenuCancellableEvent

class PlayerMenuPreOpenEvent(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override var canceled: Boolean = false
) : PlayerMenuEvent, PlayerMenuCancellableEvent

class PlayerMenuOpenEvent<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override val inventory: I
) : PlayerInventoryMenuEvent<I>

class PlayerMenuUpdateEvent<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override val inventory: I
) : PlayerInventoryMenuEvent<I>

class PlayerAnvilMenuPrepareEvent(
	override val menu: AnvilMenu,
	override val player: Player,
	override val inventory: AnvilInventory
) : PlayerInventoryMenuEvent<AnvilInventory>

class PlayerMenuCloseEvent(
	override val menu: StaticMenu<*, *>,
	override val player: Player
) : PlayerMenuEvent

interface PlayerMenuMoveEvent : PlayerMenuCancellableEvent {
	val movedItem: ItemStack?
}

class PlayerMoveToMenuEvent<I : Inventory>(
	menu: StaticMenu<*, *>,
	player: Player,
	inventory: I,
	canceled: Boolean,
	override val movedItem: ItemStack?,
	val hotbarKey: Int
) : PlayerMenuInteractEvent<I>(menu, player, inventory, canceled), PlayerMenuMoveEvent