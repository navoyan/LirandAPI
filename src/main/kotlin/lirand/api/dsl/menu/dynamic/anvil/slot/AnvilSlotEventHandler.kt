package lirand.api.dsl.menu.dynamic.anvil.slot

import lirand.api.dsl.menu.dynamic.SlotDSLEventHandler
import lirand.api.dsl.menu.dynamic.anvil.AnvilMenuEventHandler
import lirand.api.menu.slot.PlayerMenuSlotInteractEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.plugin.Plugin

class AnvilSlotEventHandler(
	plugin: Plugin,
	private val menuEventHandler: AnvilMenuEventHandler
) : SlotDSLEventHandler<AnvilInventory>(plugin) {

	override fun handleInteract(interactEvent: PlayerMenuSlotInteractEvent<AnvilInventory>) {
		if (interactEvent.slotIndex == AnvilSlot.RESULT) {
			menuEventHandler.handleComplete(interactEvent)
		}
		super.handleInteract(interactEvent)
	}

	override fun clone(plugin: Plugin): AnvilSlotEventHandler {
		return AnvilSlotEventHandler(plugin, menuEventHandler).also {
			it.interactCallbacks.addAll(interactCallbacks)
			it.updateCallbacks.addAll(updateCallbacks)
			it.renderCallbacks.addAll(renderCallbacks)
		}
	}

}