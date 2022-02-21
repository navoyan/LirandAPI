package lirand.api.dsl.menu.builders.dynamic.anvil

import lirand.api.dsl.menu.builders.dynamic.MenuDSL
import lirand.api.dsl.menu.builders.dynamic.SlotDSL
import lirand.api.dsl.menu.exposed.dynamic.Slot
import lirand.api.dsl.menu.exposed.dynamic.anvil.AnvilMenu
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

inline fun Plugin.anvilMenu(
	cancelOnClick: Boolean = true,
	crossinline builder: AnvilMenuDSL.() -> Unit = {}
): AnvilMenu = AnvilMenuImpl(this, cancelOnClick).apply(builder)

inline fun AnvilMenuDSL.slot(
	slot: Int,
	item: ItemStack? = null,
	crossinline builder: SlotDSL<AnvilInventory>.() -> Unit = {}
): Slot<AnvilInventory> = (baseSlot as SlotDSL<AnvilInventory>).clone(item)
	.apply(builder).also {
		setSlot(slot, it)
	}


interface AnvilMenuDSL : AnvilMenu, MenuDSL<Slot<AnvilInventory>, AnvilInventory> {

	override val eventHandler: AnvilMenuDSLEventHandler

	fun onComplete(completeCallback: AnvilMenuCompleteCallback) {
		eventHandler.completeCallbacks.add(completeCallback)
	}

	fun onPrepare(prepareCallback: AnvilMenuPrepareCallback) {
		eventHandler.prepareCallbacks.add(prepareCallback)
	}

}