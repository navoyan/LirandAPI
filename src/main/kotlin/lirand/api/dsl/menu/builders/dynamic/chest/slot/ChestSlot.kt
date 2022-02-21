package lirand.api.dsl.menu.builders.dynamic.chest.slot

import lirand.api.dsl.menu.builders.dynamic.SlotDSL
import lirand.api.dsl.menu.builders.dynamic.SlotDSLEventHandler
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*


class ChestSlot(
	override val plugin: Plugin,
	override val item: ItemStack?,
	override var cancelEvents: Boolean,
	override val eventHandler: SlotDSLEventHandler<Inventory>
) : SlotDSL<Inventory> {

	override val slotData = WeakHashMap<String, Any>()
	override val playerSlotData = WeakHashMap<Player, MutableMap<String, Any>>()


	override fun clone(item: ItemStack?, plugin: Plugin) =
		ChestSlot(plugin, item, cancelEvents, eventHandler.clone(plugin))

	override fun clone(plugin: Plugin) = clone(item, plugin)

}