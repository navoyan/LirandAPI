package lirand.api.dsl.menu.dynamic.chest.pagination.slot

import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.fixed.MenuDSLMarker
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

@MenuDSLMarker
interface PaginationSlot<T> {
	val slotRoot: SlotDSL<Inventory>

	val paginationEventHandler: PaginationSlotEventHandler<T>

	val slotData: WeakHashMap<String, Any>
	val playerSlotData: WeakHashMap<Player, WeakHashMap<String, Any>>

	/**
	 * Cancel the interaction with this slot
	 */
	var cancelEvents: Boolean

	fun onPageChange(pageChangeCallback: MenuPlayerSlotPageChangeCallback<T>) {
		paginationEventHandler.pageChangeCallbacks.add(pageChangeCallback)
	}

	fun onInteract(clickCallback: MenuPlayerPageSlotInteractCallback<T>) {
		paginationEventHandler.interactCallbacks.add(clickCallback)
	}

	fun onRender(renderCallback: MenuPlayerPageSlotRenderCallback<T>) {
		paginationEventHandler.renderCallbacks.add(renderCallback)
	}

	fun onUpdate(updateCallback: MenuPlayerPageSlotUpdateCallback<T>) {
		paginationEventHandler.updateCallbacks.add(updateCallback)
	}
}

