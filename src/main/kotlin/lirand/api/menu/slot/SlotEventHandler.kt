package lirand.api.menu.slot

import org.bukkit.inventory.Inventory

interface SlotEventHandler<I : Inventory> : StaticSlotEventHandler<I> {

	fun handleRender(renderEvent: PlayerMenuSlotRenderEvent<I>)

}