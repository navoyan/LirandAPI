package lirand.api.menu.slot

import org.bukkit.inventory.ItemStack

interface Slot : StaticSlot {

	override val eventHandler: SlotEventHandler

	/**
	 * a clone of Slot without slotData and playerSlotData
	 */
	fun clone(item: ItemStack?): Slot

}