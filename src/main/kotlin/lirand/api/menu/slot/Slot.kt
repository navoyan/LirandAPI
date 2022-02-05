package lirand.api.menu.slot

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

interface Slot<I : Inventory> : StaticSlot<I> {

	override val eventHandler: SlotEventHandler<I>

	/**
	 * a clone of Slot without slotData and playerSlotData
	 */
	fun clone(item: ItemStack?): Slot<I>

}