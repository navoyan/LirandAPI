package lirand.api

import lirand.api.controllers.CommandController
import lirand.api.controllers.Controller
import lirand.api.controllers.MenuController
import lirand.api.extensions.server.registerEvents
import org.bukkit.event.Listener
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

	private val controllers = listOf<Controller>(
		commandController, menuController
	)

	init {
		_instances[plugin] = this

		for (controller in controllers) {
			controller.onEnable()

			if (controller is Listener)
				plugin.registerEvents(controller)
		}
	}
}