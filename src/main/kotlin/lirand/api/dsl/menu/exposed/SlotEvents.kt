package lirand.api.dsl.menu.exposed

import lirand.api.dsl.menu.exposed.dynamic.chest.ChestMenu
import lirand.api.dsl.menu.exposed.fixed.StaticMenu
import lirand.api.dsl.menu.exposed.fixed.StaticSlot
import lirand.api.extensions.inventory.get
import lirand.api.extensions.inventory.set
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

interface PlayerMenuSlotEvent : PlayerMenuEvent {
	val slotIndex: Int
	val slot: StaticSlot<*>

	fun putPlayerSlotData(key: String, value: Any) {
		slot.playerSlotData.getOrPut(player) {
			WeakHashMap()
		}[key] = value
	}

	fun getPlayerSlotData(key: String): Any? = slot.playerSlotData[player]?.get(key)
}

interface PlayerMenuInventorySlotEvent<I : Inventory> : PlayerMenuSlotEvent, PlayerInventoryMenuEvent<I> {

	var showingItem: ItemStack?
		get() = inventory[slotIndex]?.takeIf { it.type != Material.AIR }
		set(value) {
			inventory[slotIndex] = value
		}

}



open class MenuSlotInteractEvent<I : Inventory>(
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
) : PlayerMenuInteractEvent<I>(menu, player, inventory, canceled), PlayerMenuInventorySlotEvent<I>


class PlayerMenuSlotPageChangeEvent(
	override val menu: ChestMenu,
	override val slotIndex: Int,
	override val slot: StaticSlot<Inventory>,
	override val player: Player,
	override val inventory: Inventory
) : PlayerMenuInventorySlotEvent<Inventory>


class MenuSlotRenderEvent<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val slotIndex: Int,
	override val slot: StaticSlot<I>,
	override val player: Player,
	override val inventory: I
) : PlayerMenuInventorySlotEvent<I>


class PlayerMenuSlotUpdateEvent<I : Inventory>(
	override val menu: StaticMenu<*, *>,
	override val slotIndex: Int,
	override val slot: StaticSlot<I>,
	override val player: Player,
	override val inventory: I
) : PlayerMenuInventorySlotEvent<I>