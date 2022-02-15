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

	@MenuDSLMarker
	fun onPageChange(pageChange: MenuPlayerSlotPageChangeEvent<T>) {
		paginationEventHandler.pageChangeCallbacks.add(pageChange)
	}

	@MenuDSLMarker
	fun onClick(click: MenuPlayerPageSlotInteractEvent<T>) {
		paginationEventHandler.interactCallbacks.add(click)
	}

	@MenuDSLMarker
	fun onRender(render: MenuPlayerPageSlotRenderEvent<T>) {
		paginationEventHandler.renderCallbacks.add(render)
	}

	@MenuDSLMarker
	fun onUpdate(update: MenuPlayerPageSlotUpdateEvent<T>) {
		paginationEventHandler.updateCallbacks.add(update)
	}
}

