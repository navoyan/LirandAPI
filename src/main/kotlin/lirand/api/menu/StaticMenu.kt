package lirand.api.menu

import lirand.api.menu.slot.StaticSlot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import java.util.*

interface StaticMenu<S : StaticSlot<I>, I : Inventory> : InventoryHolder {

	val plugin: Plugin
	val title: String
	var cancelEvents: Boolean

	val rangeOfSlots: IntRange
	val slots: Map<Int, S>

	var baseSlot: S
	var updateDelay: Long

	val viewers: Map<Player, I>

	val data: WeakHashMap<String, Any>
	val playerData: WeakHashMap<Player, WeakHashMap<String, Any>>

	val eventHandler: MenuEventHandler<I>

	fun setSlot(index: Int, slot: S)
	fun removeSlot(index: Int)
	fun clearSlots()

	fun update()
	fun updateSlot(slot: S)

	fun openTo(vararg players: Player)

	fun clearData() {
		data.clear()
		for (slot in slotsWithBaseSlot())
			slot.clearSlotData()
	}

	fun clearPlayerData(player: Player) {
		playerData.remove(player)
		for (slot in slotsWithBaseSlot())
			slot.clearPlayerData(player)
	}

	fun close(player: Player, closeInventory: Boolean = true)

}