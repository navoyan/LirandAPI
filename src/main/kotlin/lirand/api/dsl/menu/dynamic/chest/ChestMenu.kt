package lirand.api.dsl.menu.dynamic.chest

import lirand.api.dsl.menu.dynamic.MenuDSL
import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.menu.calculateSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

@MenuDSLMarker
inline fun Plugin.chestMenu(
	lines: Int,
	cancelOnClick: Boolean = true,
	crossinline builder: ChestMenu.() -> Unit = {}
): ChestMenu = ChestMenuImpl(this, lines, cancelOnClick).apply(builder)

@MenuDSLMarker
inline fun ChestMenu.slot(
	line: Int,
	slot: Int,
	item: ItemStack? = null,
	crossinline builder: SlotDSL<Inventory>.() -> Unit = {}
): SlotDSL<Inventory> = slot(calculateSlot(line, slot), item, builder)

@MenuDSLMarker
inline fun ChestMenu.slot(
	slot: Int,
	item: ItemStack? = null,
	crossinline builder: SlotDSL<Inventory>.() -> Unit = {}
): SlotDSL<Inventory> = baseSlot.clone(item).apply(builder).also {
	setSlot(slot, it)
}

interface ChestMenu : MenuDSL<SlotDSL<Inventory>, Inventory> {

	var lines: Int

}