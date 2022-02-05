package lirand.api.menu

import lirand.api.dsl.menu.dynamic.anvil.AnvilMenu
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.set

interface PlayerMenu {
	val menu: StaticMenu<*, *>
	val player: Player

	fun putPlayerData(key: String, value: Any) {
		val map = menu.playerData[player]
		if (map != null) map[key] = value
		else menu.playerData[player] = WeakHashMap<String, Any>().apply { put(key, value) }
	}

	fun getPlayerData(key: String) = menu.playerData[player]?.get(key)
}

interface PlayerInventoryMenu<I : Inventory> : PlayerMenu {
	val inventory: I

	fun close() = player.closeInventory()

	fun getItem(slot: Int): ItemStack? = inventory.getItem(slot - 1)

	fun setItem(slot: Int, item: ItemStack?) {
		inventory.setItem(rawSlot(slot), item)
	}

	fun getPlayerItem(slot: Int): ItemStack? = player.inventory.getItem(rawSlot(slot))

	fun setPlayerItem(slot: Int, item: ItemStack?) {
		player.inventory.setItem(rawSlot(slot), item)
	}
}

interface PlayerMenuCancellable {
	var canceled: Boolean
}

open class PlayerMenuInteract<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override val inventory: I,
	override var canceled: Boolean
) : PlayerInventoryMenu<I>, PlayerMenuCancellable

class PlayerMenuPreOpen(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override var canceled: Boolean = false
) : PlayerMenu, PlayerMenuCancellable

class PlayerMenuOpen<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override val inventory: I
) : PlayerInventoryMenu<I>

class PlayerMenuUpdate<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val player: Player,
	override val inventory: I
) : PlayerInventoryMenu<I>

class PlayerAnvilMenuPrepare(
	override val menu: AnvilMenu,
	override val player: Player,
	override val inventory: AnvilInventory
) : PlayerInventoryMenu<AnvilInventory>

class PlayerMenuClose(
	override val menu: StaticMenu<*, *>,
	override val player: Player
) : PlayerMenu

interface PlayerMenuMove : PlayerMenuCancellable {
	val movedItem: ItemStack?
}

class PlayerMoveToMenu<I : Inventory>(
	menu: StaticMenu<*, *>,
	player: Player,
	inventory: I,
	canceled: Boolean,
	override val movedItem: ItemStack?,
	val hotbarKey: Int
) : PlayerMenuInteract<I>(menu, player, inventory, canceled), PlayerMenuMove