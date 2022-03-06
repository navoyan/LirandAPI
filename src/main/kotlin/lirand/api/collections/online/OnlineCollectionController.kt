package lirand.api.collections.online

import lirand.api.LirandAPI
import lirand.api.extensions.server.registerEvents
import lirand.api.utilities.Initializable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin

internal fun provideOnlineCollectionController(plugin: Plugin): OnlineCollectionController? {
	return LirandAPI.instances[plugin]?.onlineCollectionController
}


internal class OnlineCollectionController(val plugin: Plugin) : Listener, Initializable {

	private val registeredCallbackables = mutableListOf<OnlinePlayerCallbackable<*>>()


	override fun initialize() {
		plugin.registerEvents(this)
	}


	fun register(callbackable: OnlinePlayerCallbackable<*>) {
		registeredCallbackables.add(callbackable)
	}


	@EventHandler
	fun onPlayerQuitEvent(event: PlayerQuitEvent) {
		for (callbackable in registeredCallbackables) {
			callbackable.quit(event.player)
		}
	}

	@EventHandler
	fun onPluginDisableEvent(event: PluginDisableEvent) {
		if (plugin != event.plugin) return

		registeredCallbackables.filter { it.quitAllOnPluginDisable }
			.forEach { callbackable ->
				callbackable.quitAll()
			}
	}

}