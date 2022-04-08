package lirand.api.dsl.menu.exposed.fixed

import lirand.api.dsl.menu.exposed.MenuEventHandler
import lirand.api.dsl.menu.exposed.getSlotsWithBaseSlot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import kotlin.time.Duration

interface StaticMenu<S : StaticSlot<I>, I : Inventory> : InventoryHolder {

	val plugin: Plugin
	val title: String?

	val rangeOfSlots: IntRange
	val slots: Map<Int, S>

	var baseSlot: S
	var updateDelay: Duration

	val viewers: Map<Player, I>

	val data: MutableMap<String, Any>
	val playerData: MutableMap<Player, MutableMap<String, Any>>

	val eventHandler: MenuEventHandler<I>

	fun setSlot(index: Int, slot: S)
	fun removeSlot(index: Int)
	fun clearSlots()

	fun update()
	fun updateSlot(slot: S)

	fun openTo(player: Player)

	fun clearData() {
		data.clear()
		for (slot in getSlotsWithBaseSlot())
			slot.clearSlotData()
	}

	fun clearPlayerData(player: Player) {
		playerData.remove(player)
		for (slot in getSlotsWithBaseSlot())
			slot.clearPlayerData(player)
	}

	fun close(player: Player, closeInventory: Boolean = true)

}