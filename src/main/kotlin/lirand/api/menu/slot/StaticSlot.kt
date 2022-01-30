package lirand.api.menu.slot

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

interface StaticSlot {

	val item: ItemStack?

	val eventHandler: StaticSlotEventHandler

	val slotData: WeakHashMap<String, Any>
	val playerSlotData: WeakHashMap<Player, WeakHashMap<String, Any>>

	var cancelEvents: Boolean

	fun clearSlotData() {
		slotData.clear()
	}

	fun clearPlayerData(player: Player) {
		playerSlotData.remove(player)
	}

	fun clone(): StaticSlot
}