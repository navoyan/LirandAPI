package lirand.api.dsl.menu.dynamic.chest.pagination

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lirand.api.menu.PlayerInventoryMenuEvent
import lirand.api.menu.PlayerMenuEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerPageChangeCallback = suspend PlayerInventoryMenuEvent<Inventory>.(scope: CoroutineScope) -> Unit
typealias MenuPlayerPageAvailableCallback = suspend PlayerMenuEvent.(scope: CoroutineScope) -> Unit

class PaginationEventHandler(val plugin: Plugin) {
	private val scope = CoroutineScope(
		plugin.minecraftDispatcher + SupervisorJob() +
				CoroutineExceptionHandler { _, exception -> exception.printStackTrace() }
	)

	val pageChangeCallbacks = mutableListOf<MenuPlayerPageChangeCallback>()
	val pageAvailableCallbacks = mutableListOf<MenuPlayerPageAvailableCallback>()

	fun handlePageChange(pageChangeEvent: PlayerInventoryMenuEvent<Inventory>) {
		for (callback in pageChangeCallbacks) {
			scope.launch {
				callback(pageChangeEvent, this)
			}
		}
	}

	fun handlePageAvailable(pageAvailableEvent: PlayerMenuEvent) {
		for (callback in pageAvailableCallbacks) {
			scope.launch {
				callback(pageAvailableEvent, this)
			}
		}
	}
}