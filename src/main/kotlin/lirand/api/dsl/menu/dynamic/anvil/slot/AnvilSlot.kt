package lirand.api.dsl.menu.dynamic.anvil.slot

import lirand.api.dsl.menu.dynamic.SlotDSL
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import java.util.*

class AnvilSlot(
	override val item: ItemStack?,
	override var cancelEvents: Boolean,
	override val eventHandler: AnvilSlotEventHandler
) : SlotDSL<AnvilInventory> {

	override val slotData = WeakHashMap<String, Any>()
	override val playerSlotData = WeakHashMap<Player, WeakHashMap<String, Any>>()

	override fun clone(item: ItemStack?) = AnvilSlot(item, cancelEvents, eventHandler.clone())
	override fun clone() = clone(item)

	companion object Index {
		const val LEFT = 0
		const val RIGHT = 1
		const val RESULT = 2
	}
}