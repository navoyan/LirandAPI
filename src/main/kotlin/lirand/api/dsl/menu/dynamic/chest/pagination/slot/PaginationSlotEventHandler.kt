package lirand.api.dsl.menu.dynamic.chest.pagination.slot

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.StaticMenu
import lirand.api.menu.slot.PlayerMenuInventorySlot
import lirand.api.menu.slot.PlayerMenuSlotInteract
import lirand.api.menu.slot.PlayerMenuSlotRender
import lirand.api.menu.slot.PlayerMenuSlotUpdate
import lirand.api.menu.slot.StaticSlot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerSlotPageChangeEvent<T> = suspend PlayerMenuSlotPageChange.(T?) -> Unit
typealias MenuPlayerPageSlotInteractEvent<T> = PlayerMenuSlotInteract<Inventory>.(T?) -> Unit
typealias MenuPlayerPageSlotRenderEvent<T> = PlayerMenuSlotRender<Inventory>.(T?) -> Unit
typealias MenuPlayerPageSlotUpdateEvent<T> = suspend PlayerMenuSlotUpdate<Inventory>.(T?) -> Unit

class PaginationSlotEventHandler<T>(val plugin: Plugin) {
	val pageChangeCallbacks = mutableListOf<MenuPlayerSlotPageChangeEvent<T>>()
	val interactCallbacks = mutableListOf<MenuPlayerPageSlotInteractEvent<T>>()
	val renderCallbacks = mutableListOf<MenuPlayerPageSlotRenderEvent<T>>()
	val updateCallbacks = mutableListOf<MenuPlayerPageSlotUpdateEvent<T>>()

	fun handlePageChange(currentItem: T?, pageChange: PlayerMenuSlotPageChange) {
		for (callback in pageChangeCallbacks) {
			plugin.launch {
				callback(pageChange, currentItem)
			}
		}
	}

	fun handleRender(currentItem: T?, render: PlayerMenuSlotRender<Inventory>) {
		for (callback in renderCallbacks) {
			callback(render, currentItem)
		}
	}

	fun handleUpdate(currentItem: T?, update: PlayerMenuSlotUpdate<Inventory>) {
		for (callback in updateCallbacks) {
			plugin.launch {
				callback(update, currentItem)
			}
		}
	}

	fun handleInteract(currentItem: T?, interact: PlayerMenuSlotInteract<Inventory>) {
		for (callback in interactCallbacks) {
			callback(interact, currentItem)
		}
	}
}

class PlayerMenuSlotPageChange(
	override val menu: StaticMenu<*, *>,
	override val slotIndex: Int,
	override val slot: StaticSlot<Inventory>,
	override val player: Player,
	override val inventory: Inventory
) : PlayerMenuInventorySlot<Inventory>