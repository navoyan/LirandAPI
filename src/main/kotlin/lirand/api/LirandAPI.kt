package lirand.api

import lirand.api.collections.online.OnlineCollectionController
import lirand.api.dsl.menu.MenuController
import lirand.api.extensions.server.commands.CommandController
import org.bukkit.plugin.Plugin

class LirandAPI internal constructor(internal val plugin: Plugin) {

	companion object {
		private val _instances = mutableMapOf<Plugin, LirandAPI>()
		val instances: Map<Plugin, LirandAPI> get() = _instances

		fun register(plugin: Plugin): LirandAPI {
			check(plugin !in instances) { "Api for this plugin already initialized." }

			return LirandAPI(plugin)
		}
	}

	internal val commandController = CommandController(plugin)
	internal val menuController = MenuController(plugin)
	internal val onlineCollectionController = OnlineCollectionController(plugin)

	init {
		_instances[plugin] = this

		val controllers = arrayOf(commandController, menuController, onlineCollectionController)

		for (controller in controllers) {
			controller.initialize()
		}
	}
}