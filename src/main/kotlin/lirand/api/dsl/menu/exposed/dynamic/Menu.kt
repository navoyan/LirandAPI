package lirand.api.dsl.menu.exposed.dynamic

import lirand.api.dsl.menu.exposed.fixed.StaticMenu
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

interface Menu<S : Slot<I>, I : Inventory> : StaticMenu<S, I> {

	override var title: String

	fun update(players: Collection<Player>)
	fun updateSlot(slot: S, players: Collection<Player>)

	fun update(vararg players: Player) = update(players.toList())
	fun updateSlot(slot: S, vararg players: Player) = updateSlot(slot, players.toList())

}