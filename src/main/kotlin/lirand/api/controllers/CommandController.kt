package lirand.api.controllers

import lirand.api.LirandAPI
import lirand.api.extensions.server.unregister
import org.bukkit.command.Command
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin

internal fun provideCommandController(plugin: Plugin) = LirandAPI.instances[plugin]?.commandController

internal class CommandController(val plugin: Plugin) : Listener, Controller {

	val commands = mutableListOf<Command>()

	@EventHandler
	fun onPluginDisableEvent(event: PluginDisableEvent) {
		commands.forEach {
			it.unregister(plugin)
		}
	}
}