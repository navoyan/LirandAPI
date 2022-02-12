package lirand.api.dsl.menu.dynamic.anvil.slot

import lirand.api.dsl.menu.dynamic.SlotDSLEventHandler
import lirand.api.dsl.menu.dynamic.anvil.AnvilMenuEventHandler
import lirand.api.menu.slot.PlayerMenuSlotInteract
import org.bukkit.inventory.AnvilInventory
import org.bukkit.plugin.Plugin

class AnvilSlotEventHandler(
	plugin: Plugin,
	private val menuEventHandler: AnvilMenuEventHandler
) : SlotDSLEventHandler<AnvilInventory>(plugin) {

	override fun interact(interact: PlayerMenuSlotInteract<AnvilInventory>) {
		if (interact.slotIndex == AnvilSlot.RESULT) {
			menuEventHandler.complete(interact)
		}
		super.interact(interact)
	}

	override fun clone(plugin: Plugin): AnvilSlotEventHandler {
		return AnvilSlotEventHandler(plugin, menuEventHandler).also {
			it.interactCallbacks.addAll(interactCallbacks)
			it.updateCallbacks.addAll(updateCallbacks)
			it.renderCallbacks.addAll(renderCallbacks)
		}
	}

}