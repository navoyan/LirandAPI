package lirand.api.dsl.menu.dynamic.chest.pagination

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.PlayerInventoryMenuEvent
import lirand.api.menu.PlayerMenuEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerPageChangeCallback = suspend PlayerInventoryMenuEvent<Inventory>.() -> Unit
typealias MenuPlayerPageAvailableCallback = suspend PlayerMenuEvent.() -> Unit

class PaginationEventHandler(val plugin: Plugin) {
	val pageChangeCallbacks = mutableListOf<MenuPlayerPageChangeCallback>()
	val pageAvailableCallbacks = mutableListOf<MenuPlayerPageAvailableCallback>()

	fun handlePageChange(pageChangeEvent: PlayerInventoryMenuEvent<Inventory>) {
		for (callback in pageChangeCallbacks) {
			plugin.launch {
				callback(pageChangeEvent)
			}
		}
	}

	fun handlePageAvailable(pageAvailableEvent: PlayerMenuEvent) {
		for (callback in pageAvailableCallbacks) {
			plugin.launch {
				callback(pageAvailableEvent)
			}
		}
	}
}