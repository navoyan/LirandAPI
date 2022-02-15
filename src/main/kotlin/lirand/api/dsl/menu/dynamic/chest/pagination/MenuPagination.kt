package lirand.api.dsl.menu.dynamic.chest.pagination

import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.dynamic.chest.ChestMenu
import lirand.api.dsl.menu.dynamic.chest.pagination.slot.PaginationSlot
import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.menu.PlayerInventoryMenuEvent
import lirand.api.menu.PlayerMenuEvent
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

typealias ItemsProvider<T> = PlayerMenuEvent.() -> Collection<T>
typealias ItemsAdapter<T> = PlayerInventoryMenuEvent<Inventory>.(List<T>) -> List<T>

@MenuDSLMarker
inline fun <T> MenuPagination<T>.slot(
	crossinline builder: PaginationSlot<T>.() -> Unit
) {
	for (paginationSlot in paginationSlots.values) {
		paginationSlot.builder()
	}
}

fun ChestMenu.setPlayerOpenPage(player: Player, page: Int) {
	playerData[player] = WeakHashMap<String, Any>().apply {
		put(PAGINATION_OPEN_PAGE_KEY, page)
	}
}

@MenuDSLMarker
inline fun <T> ChestMenu.pagination(
	noinline itemsProvider: ItemsProvider<T>,
	previousPageSlot: SlotDSL<Inventory>,
	nextPageSlot: SlotDSL<Inventory>,
	linesRange: IntRange = 1 until lines,
	slotsRange: IntRange = 1..9,
	autoUpdateSwitchPageSlot: Boolean = true,
	crossinline builder: MenuPagination<T>.() -> Unit
): MenuPagination<T> = MenuPaginationImpl(
	this,
	itemsProvider,
	previousPageSlot, nextPageSlot,
	linesRange, slotsRange,
	autoUpdateSwitchPageSlot
).apply(builder)



@MenuDSLMarker
interface MenuPagination<T> {
	val menu: ChestMenu
	val paginationSlots: TreeMap<Int, PaginationSlot<T>>
	val paginationEventHandler: PaginationEventHandler

	val itemsProvider: ItemsProvider<T>

	val nextPageSlot: SlotDSL<Inventory>
	val previousPageSlot: SlotDSL<Inventory>

	val autoUpdateSwitchPageSlots: Boolean

	val linesRange: IntRange
	val slotsRange: IntRange

	val itemsAdapter: ItemsAdapter<T>?

	@MenuDSLMarker
	fun onPageChange(pageChangeCallback: MenuPlayerPageChangeCallback) {
		paginationEventHandler.pageChangeCallbacks.add(pageChangeCallback)
	}

	@MenuDSLMarker
	fun onPageAvailable(pageAvailableCallback: MenuPlayerPageAvailableCallback) {
		paginationEventHandler.pageAvailableCallbacks.add(pageAvailableCallback)
	}

	@MenuDSLMarker
	fun adaptOnUpdate(adapter: ItemsAdapter<T>)

	fun hasPreviousPage(player: Player): Boolean
	fun hasNextPage(player: Player): Boolean
	fun getPlayerCurrentPage(player: Player): Int

	fun updateItems(player: Player)
}