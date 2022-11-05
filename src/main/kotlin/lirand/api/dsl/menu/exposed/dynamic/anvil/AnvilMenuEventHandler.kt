package lirand.api.dsl.menu.exposed.dynamic.anvil

import lirand.api.dsl.menu.exposed.MenuEventHandler
import lirand.api.dsl.menu.exposed.PlayerMenuSlotInteractEvent
import lirand.api.dsl.menu.exposed.PlayerAnvilMenuPrepareEvent
import org.bukkit.inventory.AnvilInventory

interface AnvilMenuEventHandler : MenuEventHandler<AnvilInventory> {

	fun handleComplete(completeEvent: PlayerMenuSlotInteractEvent<AnvilInventory>)

	fun handlePrepare(prepareEvent: PlayerAnvilMenuPrepareEvent)

}