package lirand.api.dsl.menu.builders.dynamic.anvil

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lirand.api.dsl.menu.builders.MenuDSLEventHandler
import lirand.api.dsl.menu.exposed.MenuSlotInteractEvent
import lirand.api.dsl.menu.exposed.PlayerAnvilMenuPrepareEvent
import lirand.api.dsl.menu.exposed.dynamic.anvil.AnvilMenuEventHandler
import org.bukkit.inventory.AnvilInventory
import org.bukkit.plugin.Plugin

typealias AnvilMenuCompleteCallback = MenuSlotInteractEvent<AnvilInventory>.(scope: CoroutineScope) -> Unit
typealias AnvilMenuPrepareCallback = PlayerAnvilMenuPrepareEvent.(scope: CoroutineScope) -> Unit


class AnvilMenuDSLEventHandler(plugin: Plugin) :
	AnvilMenuEventHandler, MenuDSLEventHandler<AnvilInventory>(plugin) {

	val completeCallbacks = mutableListOf<AnvilMenuCompleteCallback>()
	val prepareCallbacks = mutableListOf<AnvilMenuPrepareCallback>()

	override fun handleComplete(completeEvent: MenuSlotInteractEvent<AnvilInventory>) {
		for (callback in completeCallbacks) {
			scope.launch {
				callback(completeEvent, this)
			}
		}
	}

	override fun handlePrepare(prepareEvent: PlayerAnvilMenuPrepareEvent) {
		for (callback in prepareCallbacks) {
			scope.launch {
				delay(1)
				callback(prepareEvent, this)
			}
		}
	}

}