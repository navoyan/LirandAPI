package lirand.api.dsl.menu.exposed.fixed

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

interface StaticSlot<I : Inventory> {

	val plugin: Plugin

	val item: ItemStack?

	val eventHandler: StaticSlotEventHandler<I>

	val slotData: MenuTypedDataMap
	val playerSlotData: MenuPlayerDataMap

	fun clone(plugin: Plugin = this.plugin): StaticSlot<I>

}