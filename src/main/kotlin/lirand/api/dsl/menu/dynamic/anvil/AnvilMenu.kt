package lirand.api.dsl.menu.dynamic.anvil

import lirand.api.dsl.menu.dynamic.MenuDSL
import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.fixed.StaticSlotDSL
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

inline fun Plugin.anvilMenu(
	cancelOnClick: Boolean = true,
	crossinline builder: AnvilMenu.() -> Unit = {}
): AnvilMenu = AnvilMenuImpl(this, cancelOnClick).apply(builder)

inline fun AnvilMenu.slot(
	slot: Int,
	item: ItemStack? = null,
	crossinline builder: StaticSlotDSL<AnvilInventory>.() -> Unit = {}
): SlotDSL<AnvilInventory> = baseSlot.clone(item).apply(builder).also {
	setSlot(slot, it)
}

interface AnvilMenu : MenuDSL<SlotDSL<AnvilInventory>, AnvilInventory> {

	override val eventHandler: AnvilMenuEventHandler

	override fun getInventory(): AnvilInventory

	fun onComplete(completeCallback: AnvilMenuCompleteCallback) {
		eventHandler.completeCallbacks.add(completeCallback)
	}

	fun onPrepare(prepareCallback: AnvilMenuPrepareCallback) {
		eventHandler.prepareCallbacks.add(prepareCallback)
	}
}