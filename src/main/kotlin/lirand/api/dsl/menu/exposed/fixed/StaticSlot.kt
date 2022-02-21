package lirand.api.dsl.menu.exposed.fixed

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

interface StaticSlot<I : Inventory> {

	val plugin: Plugin

	val item: ItemStack?

	val eventHandler: StaticSlotEventHandler<I>

	val slotData: MutableMap<String, Any>
	val playerSlotData: MutableMap<Player, MutableMap<String, Any>>

	fun clearSlotData() {
		slotData.clear()
	}

	fun clearPlayerData(player: Player) {
		playerSlotData.remove(player)
	}

	fun clone(plugin: Plugin = this.plugin): StaticSlot<I>

}