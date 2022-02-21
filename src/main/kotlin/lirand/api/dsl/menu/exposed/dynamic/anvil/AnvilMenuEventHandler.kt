package lirand.api.dsl.menu.exposed.dynamic.anvil

import lirand.api.dsl.menu.exposed.MenuEventHandler
import lirand.api.dsl.menu.exposed.MenuSlotInteractEvent
import lirand.api.dsl.menu.exposed.PlayerAnvilMenuPrepareEvent
import org.bukkit.inventory.AnvilInventory

interface AnvilMenuEventHandler : MenuEventHandler<AnvilInventory> {

	fun handleComplete(completeEvent: MenuSlotInteractEvent<AnvilInventory>)

	fun handlePrepare(prepareEvent: PlayerAnvilMenuPrepareEvent)

}