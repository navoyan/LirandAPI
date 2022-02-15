package lirand.api.dsl.menu.dynamic.anvil

import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.menu.PlayerAnvilMenuPrepareEvent
import lirand.api.menu.slot.PlayerMenuSlotInteractEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.plugin.Plugin

typealias PlayerMenuCompleteCallback = PlayerMenuSlotInteractEvent<AnvilInventory>.() -> Unit
typealias PlayerAnvilMenuPrepareCallback = PlayerAnvilMenuPrepareEvent.() -> Unit

class AnvilMenuEventHandler(plugin: Plugin) : MenuDSLEventHandler<AnvilInventory>(plugin) {

	val completeCallbacks = mutableListOf<PlayerMenuCompleteCallback>()
	val prepareCallbacks = mutableListOf<PlayerAnvilMenuPrepareCallback>()

	fun handleComplete(completeEvent: PlayerMenuSlotInteractEvent<AnvilInventory>) {
		for (callback in completeCallbacks) {
			callback(completeEvent)
		}
	}

	fun handlePrepare(prepareEvent: PlayerAnvilMenuPrepareEvent) {
		for (callback in prepareCallbacks) {
			callback(prepareEvent)
		}
	}

}