package lirand.api.dsl.menu.dynamic.anvil

import lirand.api.dsl.menu.dynamic.MenuDSL
import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.dsl.menu.fixed.StaticSlotDSL
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

@MenuDSLMarker
inline fun Plugin.anvilMenu(
	cancelOnClick: Boolean = true,
	builder: AnvilMenu.() -> Unit = {}
): AnvilMenu = AnvilMenuImplementation(this, cancelOnClick).apply(builder)

@MenuDSLMarker
inline fun AnvilMenu.slot(
	slot: Int,
	item: ItemStack? = null,
	builder: StaticSlotDSL.() -> Unit = {}
): SlotDSL = baseSlot.clone(item).apply(builder).also {
	setSlot(slot, it)
}

interface AnvilMenu : MenuDSL<SlotDSL> {

	override val eventHandler: AnvilMenuEventHandler

	@MenuDSLMarker
	fun onComplete(complete: PlayerMenuCompleteEvent) {
		eventHandler.completeCallbacks.add(complete)
	}

	@MenuDSLMarker
	fun onPrepare(prepare: PlayerAnvilMenuPrepareEvent) {
		eventHandler.prepareCallbacks.add(prepare)
	}
}