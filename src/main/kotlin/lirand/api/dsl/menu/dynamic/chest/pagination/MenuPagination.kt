package lirand.api.dsl.menu.dynamic.chest.pagination

import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.dynamic.chest.ChestMenu
import lirand.api.dsl.menu.dynamic.chest.pagination.slot.PaginationSlot
import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.menu.PlayerInventoryMenu
import lirand.api.menu.PlayerMenu
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.util.*

typealias ItemsProvider<T> = () -> Collection<T>
typealias ItemsAdapter<T> = PlayerMenu.(List<T>) -> List<T>

enum class PaginationOrientation {
	HORIZONTAL, VERTICAL
}

@MenuDSLMarker
inline fun <T> MenuPagination<T>.slot(
	builder: PaginationSlot<T>.() -> Unit
) {
	for (paginationSlot in paginationSlots.values) {
		paginationSlot.builder()
	}
}

fun ChestMenu.setPlayerOpenPage(player: Player, page: Int) {
	playerData[player] = WeakHashMap<String, Any>().apply {
		put(
			PAGINATION_OPEN_PAGE_KEY,
			page
		)
	}
}

@MenuDSLMarker
inline fun <T> ChestMenu.pagination(
	itemsProvider: Collection<T>,
	nextPageSlot: SlotDSL<Inventory>,
	previousPageSlot: SlotDSL<Inventory>,
	autoUpdateSwitchPageSlot: Boolean = true,
	startLine: Int = 1,
	endLine: Int = lines - 1,
	startSlot: Int = 1,
	endSlot: Int = 9,
	orientation: PaginationOrientation = PaginationOrientation.HORIZONTAL,
	noinline itemsAdapterOnOpen: ItemsAdapter<T>? = null,
	noinline itemsAdapterOnUpdate: ItemsAdapter<T>? = null,
	builder: MenuPaginationImplementation<T>.() -> Unit
): MenuPaginationImplementation<T> {
	return pagination(
		{ itemsProvider },
		nextPageSlot,
		previousPageSlot,
		autoUpdateSwitchPageSlot,
		startLine,
		endLine,
		startSlot,
		endSlot,
		orientation,
		itemsAdapterOnOpen,
		itemsAdapterOnUpdate,
		builder
	)
}

@MenuDSLMarker
inline fun <T> ChestMenu.pagination(
	noinline itemsProvider: ItemsProvider<T>,
	nextPageSlot: SlotDSL<Inventory>,
	previousPageSlot: SlotDSL<Inventory>,
	autoUpdateSwitchPageSlot: Boolean = true,
	startLine: Int = 1,
	endLine: Int = lines - 1,
	startSlot: Int = 1,
	endSlot: Int = 9,
	orientation: PaginationOrientation = PaginationOrientation.HORIZONTAL,
	noinline itemsAdapterOnOpen: ItemsAdapter<T>? = null,
	noinline itemsAdapterOnUpdate: ItemsAdapter<T>? = null,
	builder: MenuPaginationImplementation<T>.() -> Unit
): MenuPaginationImplementation<T> {
	if (startSlot > endSlot) throw IllegalArgumentException()
	if (startLine > endLine) throw IllegalArgumentException()

	return MenuPaginationImplementation(
		this,
		itemsProvider,
		nextPageSlot,
		previousPageSlot,
		autoUpdateSwitchPageSlot,
		startLine,
		endLine,
		startSlot,
		endSlot,
		orientation,
		itemsAdapterOnOpen,
		itemsAdapterOnUpdate
	).apply(builder)
}

@MenuDSLMarker
interface MenuPagination<T> {
	val menu: ChestMenu
	val paginationSlots: TreeMap<Int, PaginationSlot<T>>
	val paginationEventHandler: PaginationEventHandler

	val itemsProvider: ItemsProvider<T>

	val nextPageSlot: SlotDSL<Inventory>
	val previousPageSlot: SlotDSL<Inventory>

	val autoUpdateSwitchPageSlot: Boolean

	val startLine: Int
	val endLine: Int

	val startSlot: Int
	val endSlot: Int

	val orientation: PaginationOrientation

	val itemsAdapterOnOpen: ItemsAdapter<T>?
	val itemsAdapterOnUpdate: ItemsAdapter<T>?

	@MenuDSLMarker
	fun onPageChange(pageChange: MenuPlayerPageChangeEvent) {
		paginationEventHandler.pageChangeCallbacks.add(pageChange)
	}

	@MenuDSLMarker
	fun onPageAvailable(pageAvailable: MenuPlayerPageAvailableEvent) {
		paginationEventHandler.pageAvailableCallbacks.add(pageAvailable)
	}

	fun hasPreviousPage(player: Player): Boolean

	fun hasNextPage(player: Player): Boolean

	fun getPlayerCurrentPage(player: Player): Int

	fun updateItemsToPlayer(menuPlayer: PlayerInventoryMenu<Inventory>)
}