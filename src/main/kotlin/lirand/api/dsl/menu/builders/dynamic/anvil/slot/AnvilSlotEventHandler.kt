package lirand.api.dsl.menu.builders.dynamic.anvil.slot

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lirand.api.dsl.menu.builders.dynamic.SlotDSLEventHandler
import lirand.api.dsl.menu.builders.dynamic.anvil.AnvilMenuDSLEventHandler
import lirand.api.dsl.menu.exposed.MenuSlotInteractEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.plugin.Plugin

class AnvilSlotEventHandler(
	plugin: Plugin,
	private val menuEventHandler: AnvilMenuDSLEventHandler
) : SlotDSLEventHandler<AnvilInventory>(plugin) {

	override fun handleInteract(interactEvent: MenuSlotInteractEvent<AnvilInventory>) {
		scope.launch {
			delay(1)

			if (interactEvent.slotIndex == AnvilSlot.RESULT) {
				menuEventHandler.handleComplete(interactEvent)
			}
			super.handleInteract(interactEvent)
		}
	}

	override fun clone(plugin: Plugin): AnvilSlotEventHandler {
		return AnvilSlotEventHandler(plugin, menuEventHandler).also {
			it.interactCallbacks.addAll(interactCallbacks)
			it.updateCallbacks.addAll(updateCallbacks)
			it.renderCallbacks.addAll(renderCallbacks)
		}
	}

}