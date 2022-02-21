package lirand.api.dsl.menu.exposed.dynamic.chest.pagination.slot

import lirand.api.dsl.menu.exposed.MenuSlotInteractEvent
import lirand.api.dsl.menu.exposed.MenuSlotRenderEvent
import lirand.api.dsl.menu.exposed.PlayerMenuSlotPageChangeEvent
import lirand.api.dsl.menu.exposed.PlayerMenuSlotUpdateEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

interface PaginationSlotEventHandler<T> {

	val plugin: Plugin

	fun handlePageChange(provided: T?, pageChangeEvent: PlayerMenuSlotPageChangeEvent)

	fun handleRender(provided: T?, renderEvent: MenuSlotRenderEvent<Inventory>)

	fun handleUpdate(provided: T?, updateEvent: PlayerMenuSlotUpdateEvent<Inventory>)

	fun handleInteract(provided: T?, interactEvent: MenuSlotInteractEvent<Inventory>)

}