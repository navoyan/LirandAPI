package lirand.api.dsl.menu.dynamic.chest

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.dynamic.SlotDSLEventHandler
import lirand.api.dsl.menu.dynamic.chest.slot.ChestSlot
import lirand.api.extensions.inventory.Inventory
import lirand.api.extensions.inventory.set
import lirand.api.menu.PlayerMenuCloseEvent
import lirand.api.menu.PlayerMenuOpenEvent
import lirand.api.menu.PlayerMenuPreOpenEvent
import lirand.api.menu.PlayerMenuUpdateEvent
import lirand.api.menu.getSlotOrBaseSlot
import lirand.api.menu.slot.MenuSlotRenderEvent
import lirand.api.menu.slot.PlayerMenuSlotUpdateEvent
import lirand.api.menu.viewersFromPlayers
import lirand.api.utilities.ifTrue
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import java.util.*

class ChestMenuImpl(
	override val plugin: Plugin,
	override var lines: Int,
	override var cancelEvents: Boolean,
) : ChestMenu {

	private var dynamicTitle: (Player?) -> String? = { "" }

	override var title: String
		get() = dynamicTitle(null) ?: ""
		set(value) {
			dynamicTitle = { value }
		}

	private val scope = CoroutineScope(
		plugin.minecraftDispatcher + SupervisorJob() +
				CoroutineExceptionHandler { _, exception -> exception.printStackTrace() }
	)
	override var updateDelay: Long = 0
		set(value) {
			field = value.takeIf { it >= 0 } ?: 0
			removeUpdateTask()
			if (value > 0 && viewers.isNotEmpty())
				setUpdateTask()
		}


	private val _viewers = WeakHashMap<Player, Inventory>()
	override val viewers: Map<Player, Inventory> get() = _viewers

	override val rangeOfSlots: IntRange get() = 0 until lines * 9

	private val _slots = TreeMap<Int, SlotDSL<Inventory>>()
	override val slots: Map<Int, SlotDSL<Inventory>> get() = _slots

	override val data = WeakHashMap<String, Any>()
	override val playerData = WeakHashMap<Player, WeakHashMap<String, Any>>()

	override val eventHandler = MenuDSLEventHandler<Inventory>(plugin)

	override var baseSlot: SlotDSL<Inventory> =
		ChestSlot(null, cancelEvents, SlotDSLEventHandler(plugin))


	override fun title(render: (Player?) -> String?) {
		dynamicTitle = render
	}

	override fun setSlot(index: Int, slot: SlotDSL<Inventory>) {
		if (index in rangeOfSlots)
			_slots[index] = slot
	}

	override fun removeSlot(index: Int) {
		_slots.remove(index)
	}

	override fun clearSlots() {
		_slots.clear()
	}

	override fun update(players: Set<Player>) {
		val viewers = viewersFromPlayers(players)

		for ((player, inventory) in viewers) {
			val update = PlayerMenuUpdateEvent(this, player, inventory)
			eventHandler.handleUpdate(update)

			for (index in rangeOfSlots) {
				val slot = getSlotOrBaseSlot(index)
				updateSlotOnly(index, slot, player, inventory)
			}
		}
	}

	override fun update() = update(viewers.keys)

	override fun updateSlot(slot: SlotDSL<Inventory>, players: Set<Player>) {
		val slots: Map<Int, SlotDSL<Inventory>> = if (slot === baseSlot) {
			rangeOfSlots.mapNotNull { if (slots[it] == null) it to slot else null }.toMap()
		}
		else {
			rangeOfSlots.mapNotNull { if (slot === slots[it]) it to slot else null }.toMap()
		}

		for ((player, inventory) in viewersFromPlayers(players)) {
			for ((index, slot) in slots) {
				updateSlotOnly(index, slot, player, inventory)
			}
		}
	}

	override fun updateSlot(slot: SlotDSL<Inventory>) = updateSlot(slot, viewers.keys)

	override fun openTo(vararg players: Player) {
		for (player in players) {
			close(player, false)

			try {
				val inventory = inventory

				val preOpen = PlayerMenuPreOpenEvent(this, player)
				eventHandler.handlePreOpen(preOpen)

				if (preOpen.canceled) return

				_viewers[player] = inventory

				for (index in rangeOfSlots) {
					val slot = getSlotOrBaseSlot(index)

					val render = MenuSlotRenderEvent(this, index, slot, player, inventory)

					slot.eventHandler.handleRender(render)
				}

				player.openInventory(inventory)

				val open = PlayerMenuOpenEvent(this, player, inventory)
				eventHandler.handleOpen(open)

				if (updateDelay > 0 && viewers.size == 1)
					setUpdateTask()

			} catch (exception: Throwable) {
				exception.printStackTrace()
				removePlayer(player, true)
			}
		}
	}

	override fun getInventory(): Inventory {
		val slotIndexes = rangeOfSlots
		val inventory = Inventory(this, slotIndexes.last + 1, title)

		for (index in slotIndexes) {
			val slot = getSlotOrBaseSlot(index)

			val item = slot.item?.clone()
			inventory[index] = item
		}

		return inventory
	}

	override fun close(player: Player, closeInventory: Boolean) {
		removePlayer(player, closeInventory).ifTrue {
			val menuClose = PlayerMenuCloseEvent(this, player)
			eventHandler.handleClose(menuClose)

			if (updateDelay > 0 && viewers.isEmpty())
				removeUpdateTask()
		}
	}


	private fun removePlayer(player: Player, closeInventory: Boolean): Boolean {
		if (closeInventory) player.closeInventory()

		val viewing = _viewers.remove(player) != null
		if (viewing)
			clearPlayerData(player)

		return viewing
	}

	private fun updateSlotOnly(index: Int, slot: SlotDSL<Inventory>, player: Player, inventory: Inventory) {
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