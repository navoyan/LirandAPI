package lirand.api.dsl.menu.fixed.chest

import lirand.api.dsl.menu.dynamic.SlotDSLEventHandler
import lirand.api.dsl.menu.dynamic.chest.slot.ChestSlot
import lirand.api.dsl.menu.fixed.StaticMenuDSL
import lirand.api.dsl.menu.fixed.StaticSlotDSL
import lirand.api.extensions.inventory.set
import lirand.api.menu.calculateSlot
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

inline fun Plugin.staticChestMenu(
	lines: Int,
	title: String,
	cancelEvents: Boolean = false,
	crossinline builder: StaticChestMenu.() -> Unit = {}
): StaticChestMenu = StaticChestMenuImpl(this, lines, title, cancelEvents).apply(builder)

inline fun Plugin.staticChestMenu(
	inventory: Inventory,
	title: String,
	cancelEvents: Boolean = false,
	crossinline builder: StaticChestMenu.() -> Unit = {}
): StaticChestMenu {
	require(inventory.type == InventoryType.CHEST) { "Illegal inventory type." }

	return StaticChestMenuImpl(this, inventory.size / 9, title, cancelEvents).apply {
		this.inventory = inventory
		builder()
	}
}


inline fun StaticChestMenu.slot(
	line: Int,
	slot: Int,
	crossinline builder: StaticSlotDSL<Inventory>.() -> Unit = {}
): StaticSlotDSL<Inventory> = slot(calculateSlot(line, slot), builder)

inline fun StaticChestMenu.slot(
	line: Int,
	slot: Int,
	item: ItemStack?,
	crossinline builder: StaticSlotDSL<Inventory>.() -> Unit = {}
): StaticSlotDSL<Inventory> = slot(calculateSlot(line, slot), item, builder)

inline fun StaticChestMenu.slot(
	slot: Int,
	crossinline builder: StaticSlotDSL<Inventory>.() -> Unit = {}
): StaticSlotDSL<Inventory> = baseSlot.clone().apply(builder).also {
	setSlot(slot, it)
}

inline fun StaticChestMenu.slot(
	slot: Int,
	item: ItemStack?,
	crossinline builder: StaticSlotDSL<Inventory>.() -> Unit = {}
): StaticSlotDSL<Inventory> = ChestSlot(item, cancelEvents, SlotDSLEventHandler(plugin)).apply(builder).also {
	inventory[slot] = item
	setSlot(slot, it)
}


interface StaticChestMenu : StaticMenuDSL<StaticSlotDSL<Inventory>, Inventory> {

	val lines: Int

	fun setInventory(inventory: Inventory)

}