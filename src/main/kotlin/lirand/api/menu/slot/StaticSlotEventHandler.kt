package lirand.api.menu.slot

import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

interface StaticSlotEventHandler<I : Inventory> {

	val plugin: Plugin

	fun handleInteract(interactEvent: MenuSlotInteractEvent<I>)

	fun handleUpdate(updateEvent: PlayerMenuSlotUpdateEvent<I>)

	fun clone(plugin: Plugin = this.plugin): StaticSlotEventHandler<I>

}