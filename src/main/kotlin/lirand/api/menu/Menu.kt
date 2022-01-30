package lirand.api.menu

import lirand.api.menu.slot.Slot
import org.bukkit.entity.Player

interface Menu<S : Slot> : StaticMenu<S> {

	override var title: String

	fun update(players: Set<Player>)
	fun updateSlot(slot: S, players: Set<Player>)

	fun update(vararg players: Player) = update(players.toSet())
	fun updateSlot(slot: S, vararg players: Player) = updateSlot(slot, players.toSet())

}