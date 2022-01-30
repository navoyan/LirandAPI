package lirand.api.menu

import lirand.api.extensions.inventory.get
import lirand.api.extensions.inventory.name
import lirand.api.menu.slot.PlayerMenuSlotInteract
import lirand.api.menu.slot.StaticSlot
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

interface PlayerMenu {
	val menu: StaticMenu<*>
	val player: Player

	fun putPlayerData(key: String, value: Any) {
		val map = menu.playerData[player]
		if (map != null) map[key] = value
		else menu.playerData[player] = WeakHashMap<String, Any>().apply { put(key, value) }
	}

	fun getPlayerData(key: String) = menu.playerData[player]?.get(key)
}

interface PlayerInventoryMenu : PlayerMenu {
	val inventory: Inventory

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

open class PlayerMenuInteract(
	override val menu: StaticMenu<*>,
	override val player: Player,
	override val inventory: Inventory,
	override var canceled: Boolean
) : PlayerInventoryMenu, PlayerMenuCancellable

class PlayerMenuPreOpen(
	override val menu: StaticMenu<*>,
	override val player: Player,
	override var canceled: Boolean = false
) : PlayerMenu, PlayerMenuCancellable

class PlayerMenuOpen(
	override val menu: StaticMenu<*>,
	override val player: Player,
	override val inventory: Inventory
) : PlayerInventoryMenu

class PlayerMenuUpdate(
	override val menu: StaticMenu<*>,
	override val player: Player,
	override val inventory: Inventory
) : PlayerInventoryMenu

class PlayerMenuComplete(
	menu: StaticMenu<*>,
	inventory: AnvilInventory,
	player: Player,
	text: String,
	slotIndex: Int,
	slot: StaticSlot,
	canceled: Boolean,
	click: ClickType,
	action: InventoryAction,
	clicked: ItemStack?,
	cursor: ItemStack?,
	hotbarKey: Int
) : PlayerMenuSlotInteract(
	menu, inventory, player, slotIndex, slot,
	canceled, click, action, clicked, cursor, hotbarKey
) {

	var text: String = text
		set(value) {
			field = value
			val item = inventory[0] ?: return
			val meta = item.itemMeta ?: return
			meta.name = value
			item.itemMeta = meta
		}

	constructor(
		interact: PlayerMenuSlotInteract, text: String
	) : this(
		interact.menu, interact.inventory as AnvilInventory, interact.player,
		text, interact.slotIndex, interact.slot,
		interact.canceled, interact.click, interact.action,
		interact.clicked, interact.cursor, interact.hotbarKey
	)
}

class PlayerAnvilMenuPrepare(
	override val menu: StaticMenu<*>,
	override val player: Player,
	override val inventory: Inventory,
	result: ItemStack?
) : PlayerInventoryMenu {

	var result: ItemStack? = result
		set(value) {
			field = value
			inventory.setItem(2, value)
		}

	val text: String? get() = result?.itemMeta?.name
}

class PlayerMenuClose(
	override val menu: StaticMenu<*>,
	override val player: Player
) : PlayerMenu

interface PlayerMenuMove : PlayerMenuCancellable {
	val movedItem: ItemStack?
}

class PlayerMoveToMenu(
	menu: StaticMenu<*>,
	player: Player,
	inventory: Inventory,
	canceled: Boolean,
	override val movedItem: ItemStack?,
	val hotbarKey: Int
) : PlayerMenuInteract(menu, player, inventory, canceled), PlayerMenuMove