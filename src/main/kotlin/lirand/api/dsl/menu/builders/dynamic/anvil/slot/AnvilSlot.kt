package lirand.api.dsl.menu.builders.dynamic.anvil.slot

import lirand.api.dsl.menu.builders.dynamic.SlotDSL
import lirand.api.dsl.menu.exposed.fixed.MenuPlayerDataMap
import lirand.api.dsl.menu.exposed.fixed.MenuTypedDataMap
import org.bukkit.entity.Player
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

class AnvilSlot(
	override val plugin: Plugin,
	override val item: ItemStack?,
	override var cancelEvents: Boolean,
	override val eventHandler: AnvilSlotEventHandler
) : SlotDSL<AnvilInventory> {

	override val slotData = MenuTypedDataMap()
	override val playerSlotData = MenuPlayerDataMap()


	override fun clone(item: ItemStack?, plugin: Plugin) =
		AnvilSlot(plugin, item, cancelEvents, eventHandler.clone(plugin))

	override fun clone(plugin: Plugin) = clone(item, plugin)


	companion object Index {
		const val LEFT = 0
		const val RIGHT = 1
		const val RESULT = 2
	}
}