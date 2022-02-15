package lirand.api.dsl.menu.dynamic.chest.pagination

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.PlayerInventoryMenu
import lirand.api.menu.PlayerMenu
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerPageChangeEvent = suspend PlayerInventoryMenu<Inventory>.() -> Unit
typealias MenuPlayerPageAvailableEvent = suspend PlayerMenu.() -> Unit

class PaginationEventHandler(val plugin: Plugin) {
	val pageChangeCallbacks = mutableListOf<MenuPlayerPageChangeEvent>()
	val pageAvailableCallbacks = mutableListOf<MenuPlayerPageAvailableEvent>()

	fun pageChange(pageChange: PlayerInventoryMenu<Inventory>) {
		for (callback in pageChangeCallbacks) {
			plugin.launch {
				callback(pageChange)
			}
		}
	}

	fun pageAvailable(pageAvailable: PlayerMenu) {
		for (callback in pageAvailableCallbacks) {
			plugin.launch {
				callback(pageAvailable)
			}
		}
	}
}