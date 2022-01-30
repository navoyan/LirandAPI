package lirand.api.dsl.menu.fixed

import lirand.api.menu.slot.StaticSlot

@MenuDSLMarker
interface StaticSlotDSL : StaticSlot {

	override val eventHandler: StaticSlotDSLEventHandler

	@MenuDSLMarker
	fun onInteract(click: MenuPlayerSlotInteractEvent) {
		eventHandler.interactCallbacks.add(click)
	}

	@MenuDSLMarker
	fun onUpdate(update: MenuPlayerSlotUpdateEvent) {
		eventHandler.updateCallbacks.add(update)
	}

	override fun clone(): StaticSlotDSL

}