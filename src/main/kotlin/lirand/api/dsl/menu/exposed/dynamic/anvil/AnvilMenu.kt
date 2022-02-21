package lirand.api.dsl.menu.exposed.dynamic.anvil

import lirand.api.dsl.menu.exposed.dynamic.Menu
import lirand.api.dsl.menu.exposed.dynamic.Slot
import org.bukkit.inventory.AnvilInventory

interface AnvilMenu : Menu<Slot<AnvilInventory>, AnvilInventory> {

	override val eventHandler: AnvilMenuEventHandler

	override fun getInventory(): AnvilInventory

}