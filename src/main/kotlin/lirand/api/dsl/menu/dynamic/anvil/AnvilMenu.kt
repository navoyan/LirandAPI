package lirand.api.dsl.menu.dynamic.anvil

import lirand.api.dsl.menu.dynamic.MenuDSL
import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.dsl.menu.fixed.StaticSlotDSL
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

@MenuDSLMarker
inline fun Plugin.anvilMenu(
	cancelOnClick: Boolean = true,
	crossinline builder: AnvilMenu.() -> Unit = {}
): AnvilMenu = AnvilMenuImpl(this, cancelOnClick).apply(builder)

@MenuDSLMarker
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

	@MenuDSLMarker
	fun onComplete(completeCallback: PlayerMenuCompleteCallback) {
		eventHandler.completeCallbacks.add(completeCallback)
	}

	@MenuDSLMarker
	fun onPrepare(prepareCallback: PlayerAnvilMenuPrepareCallback) {
		eventHandler.prepareCallbacks.add(prepareCallback)
	}
}