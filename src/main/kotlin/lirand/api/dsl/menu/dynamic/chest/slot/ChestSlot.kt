package lirand.api.dsl.menu.dynamic.chest.slot

import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.dynamic.SlotDSLEventHandler
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*


class ChestSlot(
	override val item: ItemStack?,
	override var cancelEvents: Boolean,
	override val eventHandler: SlotDSLEventHandler<Inventory>
) : SlotDSL<Inventory> {

	override val slotData = WeakHashMap<String, Any>()
	override val playerSlotData = WeakHashMap<Player, WeakHashMap<String, Any>>()

	override fun clone(item: ItemStack?) = ChestSlot(item, cancelEvents, eventHandler.clone())
	override fun clone() = clone(item)

}