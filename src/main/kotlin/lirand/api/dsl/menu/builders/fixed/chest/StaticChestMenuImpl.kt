package lirand.api.dsl.menu.builders.fixed.chest

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lirand.api.dsl.menu.builders.MenuDSLEventHandler
import lirand.api.dsl.menu.builders.dynamic.chest.slot.ChestSlot
import lirand.api.dsl.menu.builders.fixed.StaticSlotDSLEventHandler
import lirand.api.dsl.menu.builders.fixed.chest.slot.StaticChestSlot
import lirand.api.dsl.menu.exposed.PlayerMenuCloseEvent
import lirand.api.dsl.menu.exposed.PlayerMenuOpenEvent
import lirand.api.dsl.menu.exposed.PlayerMenuPreOpenEvent
import lirand.api.dsl.menu.exposed.PlayerMenuSlotUpdateEvent
import lirand.api.dsl.menu.exposed.PlayerMenuUpdateEvent
import lirand.api.dsl.menu.exposed.fixed.StaticSlot
import lirand.api.dsl.menu.exposed.getSlotOrBaseSlot
import lirand.api.extensions.inventory.Inventory
import lirand.api.utilities.ifTrue
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.time.Duration

class StaticChestMenuImpl(
	override val plugin: Plugin,
	override val lines: Int,
	override var title: String,
	override var cancelEvents: Boolean
) : StaticChestMenuDSL {

	private var _inventory: Inventory = Inventory(lines * 9, this, title)

	private val scope = CoroutineScope(
		plugin.minecraftDispatcher + SupervisorJob() +
				CoroutineExceptionHandler { _, exception -> exception.printStackTrace() }
	)
	override var updateDelay: Duration = Duration.ZERO
		set(value) {
			field = value.takeIf { it >= Duration.ZERO } ?: Duration.ZERO
			removeUpdateTask()
			if (value > Duration.ZERO && viewers.isNotEmpty())
				setUpdateTask()
		}


	private val _viewers = WeakHashMap<Player, Inventory>()
	override val viewers: Map<Player, Inventory> get() = _viewers

	override val rangeOfSlots: IntRange get() = 0 until lines * 9

	private val _slots = TreeMap<Int, StaticSlot<Inventory>>()
	override val slots: Map<Int, StaticSlot<Inventory>> get() = _slots

	override val data = WeakHashMap<String, Any>()
	override val playerData = WeakHashMap<Player, MutableMap<String, Any>>()

	override val eventHandler = MenuDSLEventHandler<Inventory>(plugin)

	override var baseSlot: StaticSlot<Inventory> =
		StaticChestSlot(plugin, cancelEvents, StaticSlotDSLEventHandler(plugin))


	override fun setSlot(index: Int, slot: StaticSlot<Inventory>) {
		if (index !in rangeOfSlots) return

		if (slot is StaticChestSlot && slot.menu == null && slot.slotIndex <= 0) {
			slot.menu = this
			slot.slotIndex = index
		}

		_slots[index] = slot
	}

	override fun removeSlot(index: Int) {
		_slots.remove(index)?.let {
			if (it is ChestSlot) {
				inventory.clear(index)
			}
		}
	}

	override fun clearSlots() {
		for ((index, slot) in slots) {
			_slots.remove(index)

			if (slot is ChestSlot) {
				inventory.clear(index)
			}
		}
	}

	override fun update() {
		for (player in viewers.keys) {
			val update = PlayerMenuUpdateEvent(this, player, inventory)
			eventHandler.handleUpdate(update)

			for (index in rangeOfSlots) {
				val slot = getSlotOrBaseSlot(index)
				updateSlotOnly(index, slot, player, inventory)
			}
		}
	}

	override fun updateSlot(slot: StaticSlot<Inventory>) {
		val slots = if (slot === baseSlot) {
			rangeOfSlots.mapNotNull { if (slots[it] == null || slots[it] === baseSlot) it to slot else null }.toMap()
		}
		else {
			rangeOfSlots.mapNotNull { if (slot === slots[it]) it to slot else null }.toMap()
		}

		for (player in viewers.keys) {
			for ((index, slot) in slots) {
				updateSlotOnly(index, slot, player, inventory)
			}
		}
	}

	override fun getInventory() = _inventory

	override fun setInventory(inventory: Inventory) {
		_inventory.storageContents = inventory.storageContents
	}

	override fun openTo(players: Collection<Player>) {
		for (player in players) {
			close(player, false)

			try {
				val preOpen = PlayerMenuPreOpenEvent(this, player)
				eventHandler.handlePreOpen(preOpen)

				if (preOpen.canceled) return

				_viewers[player] = inventory
				player.openInventory(inventory)

				if (updateDelay > Duration.ZERO && viewers.size == 1)
					setUpdateTask()

				val open = PlayerMenuOpenEvent(this, player, inventory)
				eventHandler.handleOpen(open)

			} catch (exception: Throwable) {
				exception.printStackTrace()
				removePlayer(player, true)
			}
		}
	}

	override fun close(player: Player, closeInventory: Boolean) {
		removePlayer(player, closeInventory).ifTrue {
			val menuClose = PlayerMenuCloseEvent(this, player)
			eventHandler.handleClose(menuClose)

			if (updateDelay > Duration.ZERO && viewers.isEmpty())
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

	private fun updateSlotOnly(index: Int, slot: StaticSlot<Inventory>, player: Player, inventory: Inventory) {
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