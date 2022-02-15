package lirand.api.dsl.menu.dynamic

import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.dsl.menu.fixed.StaticSlotDSL
import lirand.api.menu.slot.Slot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface SlotDSL<I : Inventory> : Slot<I>, StaticSlotDSL<I> {

	override val eventHandler: SlotDSLEventHandler<I>

	@MenuDSLMarker
	fun onRender(renderCallback: MenuPlayerSlotRenderCallback<I>) {
		eventHandler.renderCallbacks.add(renderCallback)
	}

	override fun clone(item: ItemStack?): SlotDSL<I>
	override fun clone(): SlotDSL<I>
}