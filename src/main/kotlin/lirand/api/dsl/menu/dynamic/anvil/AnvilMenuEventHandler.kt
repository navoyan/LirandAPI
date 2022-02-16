package lirand.api.dsl.menu.dynamic.anvil

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.menu.PlayerAnvilMenuPrepareEvent
import lirand.api.menu.slot.MenuSlotInteractEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.plugin.Plugin

typealias AnvilMenuCompleteCallback = MenuSlotInteractEvent<AnvilInventory>.(scope: CoroutineScope) -> Unit
typealias AnvilMenuPrepareCallback = PlayerAnvilMenuPrepareEvent.(scope: CoroutineScope) -> Unit

class AnvilMenuEventHandler(plugin: Plugin) : MenuDSLEventHandler<AnvilInventory>(plugin) {

	val completeCallbacks = mutableListOf<AnvilMenuCompleteCallback>()
	val prepareCallbacks = mutableListOf<AnvilMenuPrepareCallback>()

	fun handleComplete(completeEvent: MenuSlotInteractEvent<AnvilInventory>) {
		for (callback in completeCallbacks) {
			scope.launch {
				callback(completeEvent, this)
			}
		}
	}

	fun handlePrepare(prepareEvent: PlayerAnvilMenuPrepareEvent) {
		for (callback in prepareCallbacks) {
			scope.launch {
				delay(1)
				callback(prepareEvent, this)
			}
		}
	}

}