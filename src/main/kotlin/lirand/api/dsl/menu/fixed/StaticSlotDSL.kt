package lirand.api.dsl.menu.fixed

import lirand.api.menu.slot.StaticSlot
import org.bukkit.inventory.Inventory

@MenuDSLMarker
interface StaticSlotDSL<I : Inventory> : StaticSlot<I> {

	override val eventHandler: StaticSlotDSLEventHandler<I>

	@MenuDSLMarker
	fun onInteract(click: MenuPlayerSlotInteractEvent<I>) {
		eventHandler.interactCallbacks.add(click)
	}

	@MenuDSLMarker
	fun onUpdate(update: MenuPlayerSlotUpdateEvent<I>) {
		eventHandler.updateCallbacks.add(update)
	}

	override fun clone(): StaticSlotDSL<I>

}