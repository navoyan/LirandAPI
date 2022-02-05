package lirand.api.dsl.menu.fixed.chest.slot

import lirand.api.dsl.menu.fixed.StaticSlotDSL
import lirand.api.dsl.menu.fixed.StaticSlotDSLEventHandler
import lirand.api.dsl.menu.fixed.chest.StaticChestMenu
import lirand.api.extensions.inventory.get
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

class StaticChestSlot(
	override var cancelEvents: Boolean,
	override val eventHandler: StaticSlotDSLEventHandler<Inventory>,
) : StaticSlotDSL<Inventory> {

	internal var menu: StaticChestMenu? = null
	internal var slotIndex = 0

	override val item: ItemStack?
		get() = menu?.inventory?.get(slotIndex - 1)

	override val slotData = WeakHashMap<String, Any>()
	override val playerSlotData = WeakHashMap<Player, WeakHashMap<String, Any>>()

	override fun clone() = StaticChestSlot(cancelEvents, eventHandler.clone())

}