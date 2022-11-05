package lirand.api.dsl.menu.builders.dynamic.chest

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lirand.api.dsl.menu.builders.MenuDSLEventHandler
import lirand.api.dsl.menu.builders.dynamic.SlotDSLEventHandler
import lirand.api.dsl.menu.builders.dynamic.chest.slot.ChestSlot
import lirand.api.dsl.menu.exposed.*
import lirand.api.dsl.menu.exposed.dynamic.Slot
import lirand.api.dsl.menu.exposed.fixed.*
import lirand.api.extensions.inventory.Inventory
import lirand.api.extensions.inventory.clone
import lirand.api.extensions.inventory.set
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.time.Duration

class ChestMenuImpl(
	override val plugin: Plugin,
	override var lines: Int,
	override var cancelEvents: Boolean,
) : ChestMenuDSL {

	private var dynamicTitle: (PlayerMenuEvent.() -> String?)? = null
	override var title: String? = null


	private val scope = CoroutineScope(
		plugin.minecraftDispatcher + SupervisorJob() +
				CoroutineExceptionHandler { _, exception -> exception.printStackTrace() }
	)
	override var updateDelay: Duration = Duration.ZERO
		set(value) {
			field = value.takeIf { it >= Duration.ZERO } ?: Duration.ZERO
			removeUpdateTask()
			if (value > Duration.ZERO && views.isNotEmpty())
				setUpdateTask()
		}


	private val _views = WeakHashMap<Player, MenuView<Inventory>>()
	override val views: Map<Player, MenuView<Inventory>> get() = _views

	override val rangeOfSlots: IntRange = 0 until lines * 9

	private val _slots = TreeMap<Int, Slot<Inventory>>()
	override val slots: Map<Int, Slot<Inventory>> get() = _slots

	override val data = MenuTypedDataMap()
	override val playerData = MenuPlayerDataMap()

	override val eventHandler = MenuDSLEventHandler<Inventory>(plugin)

	override var baseSlot: Slot<Inventory> =
		ChestSlot(plugin, null, cancelEvents, SlotDSLEventHandler(plugin))


	override fun title(render: PlayerMenuEvent.() -> String?) {
		dynamicTitle = render
	}

	override fun setSlot(index: Int, slot: Slot<Inventory>) {
		if (index in rangeOfSlots)
			_slots[index] = slot
	}

	override fun removeSlot(index: Int) {
		_slots.remove(index)
	}

	override fun clearSlots() {
		_slots.clear()
	}

	override fun update(player: Player) {
		if (!hasPlayer(player)) return

		val view = views.getValue(player)
		val updateEvent = PlayerMenuUpdateEvent(this, player, view.inventory)
		eventHandler.handleUpdate(updateEvent)

		for (index in rangeOfSlots) {
			val slot = getSlotOrBaseSlot(index)
			callSlotUpdateEvent(index, slot, player, view.inventory)
		}
	}

	override fun update() {
		for (player in views.keys) {
			update(player)
		}
	}

	override fun updateSlot(slot: Slot<Inventory>, player: Player) {
		if (!hasPlayer(player)) return

		val slots = if (slot === baseSlot) {
			rangeOfSlots.mapNotNull { if (slots[it] == null) it to slot else null }.toMap()
		}
		else {
			rangeOfSlots.mapNotNull { if (slot === slots[it]) it to slot else null }.toMap()
		}

		val view = views.getValue(player)
		for ((index, slot) in slots) {
			callSlotUpdateEvent(index, slot, player, view.inventory)
		}
	}

	override fun updateSlot(slot: Slot<Inventory>) {
		for (player in views.keys) {
			updateSlot(slot, player)
		}
	}

	override fun open(player: Player, backStack: MenuBackStack?) {
		close(player, false)

		try {
			backStack?.takeIf { !it.lastBacked }
				?.push(MenuBackStackFrame(this, player, MenuTypedDataMap(playerData[player])))
				?: run { backStack?.lastBacked = false }

			val inventory = inventory.clone(
				false, title = title ?: dynamicTitle?.invoke(PlayerMenuEvent(this, player))
			)

			val preOpenEvent = PlayerMenuPreOpenEvent(this, player)
			eventHandler.handlePreOpen(preOpenEvent)
			if (preOpenEvent.isCanceled) return

			_views[player] = MenuView(this, player, inventory, backStack)

			scope.launch {
				delay(1.ticks)
				player.closeInventory()

				for (index in rangeOfSlots) {
					val slot = getSlotOrBaseSlot(index)
					val render = PlayerMenuSlotRenderEvent(this@ChestMenuImpl, index, slot, player, inventory)
					slot.eventHandler.handleRender(render)
				}

				player.openInventory(inventory)

				val openEvent = PlayerMenuOpenEvent(this@ChestMenuImpl, player, inventory)
				eventHandler.handleOpen(openEvent)

				if (updateDelay > Duration.ZERO && views.size == 1)
					setUpdateTask()
			}

		} catch (exception: Throwable) {
			exception.printStackTrace()
			removePlayer(player, true)
		}
	}

	override fun getInventory(): Inventory {
		val inventory = Inventory(rangeOfSlots.last + 1, this)

		for (index in rangeOfSlots) {
			val slot = getSlotOrBaseSlot(index)
			val item = slot.item?.clone()
			inventory[index] = item
		}

		return inventory
	}

	override fun close(player: Player, closeInventory: Boolean) {
		if (player !in _views) return

		val menuClose = PlayerMenuCloseEvent(this, player)
		eventHandler.handleClose(menuClose)

		removePlayer(player, closeInventory)

		if (updateDelay > Duration.ZERO && views.isEmpty())
			removeUpdateTask()
	}

	override fun back(player: Player, key: String?) {
		val backStack = views[player]?.backStack?.takeIf { it.isNotEmpty() } ?: return

		if (key != null) {
			if (backStack.none { it.key == key }) return

			while (backStack.peek().key != key) {
				backStack.pop()
			}
		}
		else {
			backStack.pop()
		}
		val frame = backStack.peek()
		frame.menu.playerData[player].putAll(frame.playerData)
		backStack.lastBacked = true
		frame.menu.open(player, backStack)
	}


	private fun removePlayer(player: Player, closeInventory: Boolean) {
		if (closeInventory) player.closeInventory()

		val viewing = _views.remove(player) != null
		if (viewing)
			clearPlayerData(player)
	}

	private fun callSlotUpdateEvent(index: Int, slot: Slot<Inventory>, player: Player, inventory: Inventory) {
		val slotUpdate = PlayerMenuSlotUpdateEvent(this, index, slot, player, inventory)
		slot.eventHandler.handleUpdate(slotUpdate)
	}

	private fun setUpdateTask() {
		scope.launch {
			while (isActive) {
				delay(updateDelay)
				update()
			}
		}
	}

	private fun removeUpdateTask() {
		scope.coroutineContext.cancelChildren()
	}
}