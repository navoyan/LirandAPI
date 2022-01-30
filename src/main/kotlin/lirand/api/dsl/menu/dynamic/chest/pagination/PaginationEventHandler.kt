package lirand.api.dsl.menu.dynamic.chest.pagination

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.PlayerMenu
import org.bukkit.plugin.Plugin

typealias MenuPlayerPageChangeEvent = suspend PlayerMenu.() -> Unit
typealias MenuPlayerPageAvailableEvent = suspend PlayerMenu.() -> Unit

class PaginationEventHandler(val plugin: Plugin) {
	val pageChangeCallbacks = mutableListOf<MenuPlayerPageChangeEvent>()
	val pageAvailableCallbacks = mutableListOf<MenuPlayerPageAvailableEvent>()

	fun pageChange(pageChange: PlayerMenu) {
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