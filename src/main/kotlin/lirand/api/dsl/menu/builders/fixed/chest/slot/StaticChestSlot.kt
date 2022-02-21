package lirand.api.dsl.menu.builders.fixed.chest.slot

import lirand.api.dsl.menu.builders.fixed.StaticSlotDSL
import lirand.api.dsl.menu.builders.fixed.StaticSlotDSLEventHandler
import lirand.api.dsl.menu.builders.fixed.chest.StaticChestMenuDSL
import lirand.api.extensions.inventory.get
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class StaticChestSlot(
	override val plugin: Plugin,
	override var cancelEvents: Boolean,
	override val eventHandler: StaticSlotDSLEventHandler<Inventory>,
) : StaticSlotDSL<Inventory> {

	internal var menu: StaticChestMenuDSL? = null
	internal var slotIndex = 0

	override val item: ItemStack?
		get() = menu?.inventory?.get(slotIndex - 1)

	override val slotData = WeakHashMap<String, Any>()
	override val playerSlotData = WeakHashMap<Player, MutableMap<String, Any>>()

	override fun clone(plugin: Plugin) = StaticChestSlot(plugin, cancelEvents, eventHandler.clone(plugin))

}