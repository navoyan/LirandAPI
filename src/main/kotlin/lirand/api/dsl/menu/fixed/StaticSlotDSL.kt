package lirand.api.dsl.menu.fixed

import lirand.api.menu.slot.StaticSlot
import org.bukkit.inventory.Inventory

@MenuDSLMarker
interface StaticSlotDSL<I : Inventory> : StaticSlot<I> {

	override val eventHandler: StaticSlotDSLEventHandler<I>

	@MenuDSLMarker
	fun onInteract(interactCallback: MenuPlayerSlotInteractCallback<I>) {
		eventHandler.interactCallbacks.add(interactCallback)
	}

	@MenuDSLMarker
	fun onUpdate(updateCallback: MenuPlayerSlotUpdateCallback<I>) {
		eventHandler.updateCallbacks.add(updateCallback)
	}

	override fun clone(): StaticSlotDSL<I>

}