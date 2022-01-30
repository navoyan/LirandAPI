package lirand.api.dsl.menu.dynamic.anvil

import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.menu.PlayerAnvilMenuPrepare
import lirand.api.menu.PlayerMenuComplete
import org.bukkit.plugin.Plugin

typealias PlayerMenuCompleteEvent = PlayerMenuComplete.() -> Unit
typealias PlayerAnvilMenuPrepareEvent = PlayerAnvilMenuPrepare.() -> Unit

class AnvilMenuEventHandler(plugin: Plugin) : MenuDSLEventHandler(plugin) {

	val completeCallbacks = mutableListOf<PlayerMenuCompleteEvent>()
	val prepareCallbacks = mutableListOf<PlayerAnvilMenuPrepareEvent>()

	fun complete(complete: PlayerMenuComplete) {
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