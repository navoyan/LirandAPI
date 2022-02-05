package lirand.api.menu.slot

import lirand.api.menu.PlayerInventoryMenu
import lirand.api.menu.PlayerMenu
import lirand.api.menu.PlayerMenuInteract
import lirand.api.menu.StaticMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

val PlayerMenuSlot.rawSlot get() = slotIndex - 1

interface PlayerMenuSlot : PlayerMenu {
	val slotIndex: Int
	val slot: StaticSlot<*>

	fun putPlayerSlotData(key: String, value: Any) {
		slot.playerSlotData.getOrPut(player) {
			WeakHashMap()
		}[key] = value
	}

	fun getPlayerSlotData(key: String): Any? = slot.playerSlotData[player]?.get(key)
}

interface PlayerMenuInventorySlot<I : Inventory> : PlayerMenuSlot, PlayerInventoryMenu<I> {

	var showingItem: ItemStack?
		get() = getItem(slotIndex)?.takeUnless { it.type == Material.AIR }
		set(value) = setItem(slotIndex, value)

}

open class PlayerMenuSlotInteract<I : Inventory>(
	menu: StaticMenu<*, *>,
	inventory: I,
	player: Player,
	override val slotIndex: Int,
	override val slot: StaticSlot<I>,
	canceled: Boolean,
	val click: ClickType,
	val action: InventoryAction,
	val clicked: ItemStack?,
	val cursor: ItemStack?,
	val hotbarKey: Int
) : PlayerMenuInteract<I>(menu, player, inventory, canceled), PlayerMenuInventorySlot<I>

class PlayerMenuSlotRender<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val slotIndex: Int,
	override val slot: StaticSlot<I>,
	override val player: Player,
	override val inventory: I
) : PlayerMenuInventorySlot<I>

class PlayerMenuSlotUpdate<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val slotIndex: Int,
	override val slot: StaticSlot<I>,
	override val player: Player,
	override val inventory: I
) : PlayerMenuInventorySlot<I>