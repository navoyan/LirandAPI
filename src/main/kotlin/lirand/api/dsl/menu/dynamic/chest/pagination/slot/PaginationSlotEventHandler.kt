package lirand.api.dsl.menu.dynamic.chest.pagination.slot

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.StaticMenu
import lirand.api.menu.slot.PlayerMenuInventorySlotEvent
import lirand.api.menu.slot.PlayerMenuSlotInteractEvent
import lirand.api.menu.slot.PlayerMenuSlotRenderEvent
import lirand.api.menu.slot.PlayerMenuSlotUpdateEvent
import lirand.api.menu.slot.StaticSlot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerSlotPageChangeCallback<T> = suspend PlayerMenuSlotPageChangeEvent.(T?) -> Unit
typealias MenuPlayerPageSlotInteractCallback<T> = PlayerMenuSlotInteractEvent<Inventory>.(T?) -> Unit
typealias MenuPlayerPageSlotRenderCallback<T> = PlayerMenuSlotRenderEvent<Inventory>.(T?) -> Unit
typealias MenuPlayerPageSlotUpdateCallback<T> = suspend PlayerMenuSlotUpdateEvent<Inventory>.(T?) -> Unit

class PaginationSlotEventHandler<T>(val plugin: Plugin) {

	val pageChangeCallbacks = mutableListOf<MenuPlayerSlotPageChangeCallback<T>>()
	val interactCallbacks = mutableListOf<MenuPlayerPageSlotInteractCallback<T>>()
	val renderCallbacks = mutableListOf<MenuPlayerPageSlotRenderCallback<T>>()
	val updateCallbacks = mutableListOf<MenuPlayerPageSlotUpdateCallback<T>>()

	fun handlePageChange(currentItem: T?, pageChangeEvent: PlayerMenuSlotPageChangeEvent) {
		for (callback in pageChangeCallbacks) {
			plugin.launch {
				callback(pageChangeEvent, currentItem)
			}
		}
	}

	fun handleRender(currentItem: T?, renderEvent: PlayerMenuSlotRenderEvent<Inventory>) {
		for (callback in renderCallbacks) {
			callback(renderEvent, currentItem)
		}
	}

	fun handleUpdate(currentItem: T?, updateEvent: PlayerMenuSlotUpdateEvent<Inventory>) {
		for (callback in updateCallbacks) {
			plugin.launch {
				callback(updateEvent, currentItem)
			}
		}
	}

	fun handleInteract(currentItem: T?, interactEvent: PlayerMenuSlotInteractEvent<Inventory>) {
		for (callback in interactCallbacks) {
			callback(interactEvent, currentItem)
		}
	}
}

class PlayerMenuSlotPageChangeEvent(
	override val menu: StaticMenu<*, *>,
	override val slotIndex: Int,
	override val slot: StaticSlot<Inventory>,
	override val player: Player,
	override val inventory: Inventory
) : PlayerMenuInventorySlotEvent<Inventory>