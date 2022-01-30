package lirand.api.dsl.menu.dynamic

import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.dsl.menu.fixed.StaticSlotDSL
import lirand.api.menu.slot.Slot
import org.bukkit.inventory.ItemStack

interface SlotDSL : Slot, StaticSlotDSL {

	override val eventHandler: SlotDSLEventHandler

	@MenuDSLMarker
	fun onRender(render: MenuPlayerSlotRenderEvent) {
		eventHandler.renderCallbacks.add(render)
	}

	override fun clone(item: ItemStack?): SlotDSL
	override fun clone(): SlotDSL
}