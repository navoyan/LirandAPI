package lirand.api.dsl.menu.dynamic.chest.pagination.slot

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lirand.api.dsl.menu.dynamic.chest.ChestMenu
import lirand.api.menu.slot.PlayerMenuInventorySlotEvent
import lirand.api.menu.slot.MenuSlotInteractEvent
import lirand.api.menu.slot.MenuSlotRenderEvent
import lirand.api.menu.slot.PlayerMenuSlotUpdateEvent
import lirand.api.menu.slot.StaticSlot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuSlotPageChangeCallback<T> =
		suspend PlayerMenuSlotPageChangeEvent.(currentItem: T?, scope: CoroutineScope) -> Unit
typealias MenuPageSlotInteractCallback<T> =
		MenuSlotInteractEvent<Inventory>.(currentItem: T?, scope: CoroutineScope) -> Unit
typealias MenuPageSlotRenderCallback<T> =
		MenuSlotRenderEvent<Inventory>.(currentItem: T?, scope: CoroutineScope) -> Unit
typealias MenuPageSlotUpdateCallback<T> =
		suspend PlayerMenuSlotUpdateEvent<Inventory>.(currentItem: T?, scope: CoroutineScope) -> Unit

class PaginationSlotEventHandler<T>(val plugin: Plugin) {
	private val scope = CoroutineScope(
		plugin.minecraftDispatcher + SupervisorJob() +
				CoroutineExceptionHandler { _, exception -> exception.printStackTrace() }
	)

	val pageChangeCallbacks = mutableListOf<MenuSlotPageChangeCallback<T>>()
	val interactCallbacks = mutableListOf<MenuPageSlotInteractCallback<T>>()
	val renderCallbacks = mutableListOf<MenuPageSlotRenderCallback<T>>()
	val updateCallbacks = mutableListOf<MenuPageSlotUpdateCallback<T>>()

	fun handlePageChange(currentItem: T?, pageChangeEvent: PlayerMenuSlotPageChangeEvent) {
		for (callback in pageChangeCallbacks) {
			scope.launch {
				callback(pageChangeEvent, currentItem, this)
			}
		}
	}

	fun handleRender(currentItem: T?, renderEvent: MenuSlotRenderEvent<Inventory>) {
		for (callback in renderCallbacks) {
			scope.launch {
				callback(renderEvent, currentItem, this)
			}
		}
	}

	fun handleUpdate(currentItem: T?, updateEvent: PlayerMenuSlotUpdateEvent<Inventory>) {
		for (callback in updateCallbacks) {
			scope.launch {
				callback(updateEvent, currentItem, this)
			}
		}
	}

	fun handleInteract(currentItem: T?, interactEvent: MenuSlotInteractEvent<Inventory>) {
		for (callback in interactCallbacks) {
			scope.launch {
				callback(interactEvent, currentItem, this)
			}
		}
	}
}

class PlayerMenuSlotPageChangeEvent(
	override val menu: ChestMenu,
	override val slotIndex: Int,
	override val slot: StaticSlot<Inventory>,
	override val player: Player,
	override val inventory: Inventory
) : PlayerMenuInventorySlotEvent<Inventory>