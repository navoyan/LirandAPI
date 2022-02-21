package lirand.api.dsl.menu.exposed.dynamic

import lirand.api.dsl.menu.exposed.MenuSlotRenderEvent
import lirand.api.dsl.menu.exposed.fixed.StaticSlotEventHandler
import org.bukkit.inventory.Inventory

interface SlotEventHandler<I : Inventory> : StaticSlotEventHandler<I> {

	fun handleRender(renderEvent: MenuSlotRenderEvent<I>)

}