package lirand.api.menu.slot

import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

interface StaticSlotEventHandler<I : Inventory> {

	val plugin: Plugin

	fun interact(interact: PlayerMenuSlotInteract<I>)

	fun update(update: PlayerMenuSlotUpdate<I>)

	fun clone(plugin: Plugin = this.plugin): StaticSlotEventHandler<I>

}