package lirand.api.dsl.menu.builders.dynamic.chest.pagination.slot

import lirand.api.dsl.menu.builders.dynamic.SlotDSL
import lirand.api.dsl.menu.builders.dynamic.chest.pagination.PaginationMenuImpl
import lirand.api.dsl.menu.exposed.MenuSlotRenderEvent
import lirand.api.dsl.menu.exposed.PlayerMenuSlotPageChangeEvent
import lirand.api.extensions.inventory.set
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

class PaginationSlotImpl<T>(
	private val pagination: PaginationMenuImpl<T>,
	override val slotRoot: SlotDSL<Inventory>
) : PaginationSlotDSL<T> {
	override val eventHandler = PaginationSlotDSLEventHandler<T>(pagination.menu.plugin)

	override var cancelEvents: Boolean
		get() = slotRoot.cancelEvents
		set(value) {
			slotRoot.cancelEvents = value
		}

	override val slotData: MutableMap<String, Any>
		get() = slotRoot.slotData
	override val playerSlotData: MutableMap<Player, MutableMap<String, Any>>
		get() = slotRoot.playerSlotData

	internal fun updateSlot(
		actualItem: T?,
		nextItem: T?,
		slotPos: Int,
		player: Player,
		inventory: Inventory,
		isPageChange: Boolean = false
	) {
		if (isPageChange) {
			relocateSlotData(actualItem, nextItem)

			eventHandler.handlePageChange(
				actualItem,
				PlayerMenuSlotPageChangeEvent(
					pagination.menu,
					slotPos,
					slotRoot,
					player,
					inventory
				)
			)
		}

		inventory[slotPos] = null

		eventHandler.handleRender(
			nextItem,
			MenuSlotRenderEvent(
				pagination.menu,
				slotPos,
				slotRoot,
				player,
				inventory
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