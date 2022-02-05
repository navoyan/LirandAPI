package lirand.api.dsl.menu.dynamic.anvil

import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.menu.PlayerAnvilMenuPrepare
import lirand.api.menu.slot.PlayerMenuSlotInteract
import org.bukkit.inventory.AnvilInventory
import org.bukkit.plugin.Plugin

typealias PlayerMenuCompleteEvent = PlayerMenuSlotInteract<AnvilInventory>.() -> Unit
typealias PlayerAnvilMenuPrepareEvent = PlayerAnvilMenuPrepare.() -> Unit

class AnvilMenuEventHandler(plugin: Plugin) : MenuDSLEventHandler<AnvilInventory>(plugin) {

	val completeCallbacks = mutableListOf<PlayerMenuCompleteEvent>()
	val prepareCallbacks = mutableListOf<PlayerAnvilMenuPrepareEvent>()

	fun complete(complete: PlayerMenuSlotInteract<AnvilInventory>) {
		for (callback in completeCallbacks) {
			callback(complete)
		}
	}

	fun prepare(prepare: PlayerAnvilMenuPrepare) {
		for (callback in prepareCallbacks) {
			callback(prepare)
		}
	}

}