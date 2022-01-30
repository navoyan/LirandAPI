package lirand.api.menu.slot

import org.bukkit.plugin.Plugin

interface StaticSlotEventHandler {

	val plugin: Plugin

	fun interact(interact: PlayerMenuSlotInteract)

	fun update(update: PlayerMenuSlotUpdate)

	fun clone(plugin: Plugin = this.plugin): StaticSlotEventHandler

}