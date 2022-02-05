package lirand.api.dsl.menu.dynamic.chest.pagination.slot

import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.dynamic.chest.pagination.MenuPaginationImplementation
import lirand.api.menu.PlayerInventoryMenu
import lirand.api.menu.slot.PlayerMenuSlotRender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

class PaginationSlotImplementation<T>(
	private val pagination: MenuPaginationImplementation<T>,
	override val slotRoot: SlotDSL<Inventory>
) : PaginationSlot<T> {
	override val paginationEventHandler = PaginationSlotEventHandler<T>(pagination.menu.plugin)

	override var cancel: Boolean
		get() = slotRoot.cancelEvents
		set(value) {
			slotRoot.cancelEvents = value
		}

	override val slotData: WeakHashMap<String, Any>
		get() = slotRoot.slotData
	override val playerSlotData: WeakHashMap<Player, WeakHashMap<String, Any>>
		get() = slotRoot.playerSlotData

	internal fun updateSlot(
		actualItem: T?,
		nextItem: T?,
		slotPos: Int,
		menuPlayerInventory: PlayerInventoryMenu<Inventory>,
		isPageChange: Boolean = false
	) {
		if (isPageChange) {
			relocateSlotData(actualItem, nextItem)

			// triggering event
			paginationEventHandler.handlePageChange(
				actualItem,
				PlayerMenuSlotPageChange(
					pagination.menu,
					slotPos,
					slotRoot,
					menuPlayerInventory.player,
					menuPlayerInventory.inventory
				)
			)
		}

		// cleaning item in the inventory slot
		menuPlayerInventory.setItem(slotPos, null)

		paginationEventHandler.handleRender(
			nextItem,
			PlayerMenuSlotRender(
				pagination.menu,
				slotPos,
				slotRoot,
				menuPlayerInventory.player,
				menuPlayerInventory.inventory
			)
		)
	}

	internal fun relocateSlotData(actualItem: T?, nextItem: T?) {
		if (actualItem != null) {
			// caching the current Data from Slot
			val slotData = WeakHashMap(slotData)
			val playerSlotData = WeakHashMap(playerSlotData)

			if (slotData.isNotEmpty())
				pagination.itemSlotData[actualItem] = slotData

			if (playerSlotData.isNotEmpty())
				pagination.itemPlayerSlotData[actualItem] = playerSlotData
		}

		// cleaning current data in the Slot
		slotData.clear()
		playerSlotData.clear()

		if (nextItem != null) {
			val nextSlotData = pagination.itemSlotData[nextItem]
			val nextPlayerSlotData = pagination.itemPlayerSlotData[nextItem]

			if (nextSlotData != null)
				slotData.putAll(nextSlotData)
			if (nextPlayerSlotData != null)
				playerSlotData.putAll(nextPlayerSlotData)
		}
	}
}