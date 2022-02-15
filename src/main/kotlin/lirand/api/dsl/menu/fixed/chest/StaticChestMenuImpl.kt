package lirand.api.dsl.menu.fixed.chest

import com.github.shynixn.mccoroutine.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.dsl.menu.dynamic.chest.slot.ChestSlot
import lirand.api.dsl.menu.fixed.StaticSlotDSL
import lirand.api.dsl.menu.fixed.StaticSlotDSLEventHandler
import lirand.api.dsl.menu.fixed.chest.slot.StaticChestSlot
import lirand.api.extensions.inventory.Inventory
import lirand.api.menu.PlayerMenuCloseEvent
import lirand.api.menu.PlayerMenuOpenEvent
import lirand.api.menu.PlayerMenuPreOpenEvent
import lirand.api.menu.PlayerMenuUpdateEvent
import lirand.api.menu.getSlotOrBaseSlot
import lirand.api.menu.slot.PlayerMenuSlotUpdateEvent
import lirand.api.utilities.ifTrue
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class StaticChestMenuImpl(
	override val plugin: Plugin,
	override val lines: Int,
	override var title: String,
	override var cancelEvents: Boolean
) : StaticChestMenu {

	private var _inventory: Inventory = Inventory(this, lines * 9, title)

	private var job: Job? = null
	override var updateDelay: Long = -1
		set(value) {
			field = value
			removeUpdateTask()
			if (value > 0 && _viewers.isNotEmpty())
				setUpdateTask()
		}


	private val _viewers = WeakHashMap<Player, Inventory>()
	override val viewers: Map<Player, Inventory> get() = _viewers

	override val rangeOfSlots: IntRange get() = 0 until lines * 9

	private val _slots = TreeMap<Int, StaticSlotDSL<Inventory>>()
	override val slots: Map<Int, StaticSlotDSL<Inventory>> get() = _slots

	override val data = WeakHashMap<String, Any>()
	override val playerData = WeakHashMap<Player, WeakHashMap<String, Any>>()

	override val eventHandler = MenuDSLEventHandler<Inventory>(plugin)

	override var baseSlot: StaticSlotDSL<Inventory> = StaticChestSlot(cancelEvents, StaticSlotDSLEventHandler(plugin))


	override fun setSlot(index: Int, slot: StaticSlotDSL<Inventory>) {
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
				_inventory.clear(index)
			}
		}
	}

	override fun clearSlots() {
		for ((index, slot) in slots) {
			_slots.remove(index)

			if (slot is ChestSlot) {
				_inventory.clear(index)
			}
		}
	}

	override fun update() {
		for (player in viewers.keys) {
			val update = PlayerMenuUpdateEvent(this, player, _inventory)
			eventHandler.handleUpdate(update)

			for (index in rangeOfSlots) {
				val slot = getSlotOrBaseSlot(index)
				updateSlotOnly(index, slot, player, _inventory)
			}
		}
	}

	override fun updateSlot(slot: StaticSlotDSL<Inventory>) {
		val slots: Map<Int, StaticSlotDSL<Inventory>> = if (slot === baseSlot) {
			rangeOfSlots.mapNotNull { if (slots[it] == null || slots[it] === baseSlot) it to slot else null }.toMap()
		}
		else {
			rangeOfSlots.mapNotNull { if (slot === slots[it]) it to slot else null }.toMap()
		}

		for (player in viewers.keys) {
			for ((index, slot) in slots) {
				updateSlotOnly(index, slot, player, _inventory)
			}
		}
	}

	override fun getInventory() = _inventory

	override fun setInventory(inventory: Inventory) {
		_inventory.storageContents = inventory.storageContents
	}

	override fun openTo(vararg players: Player) {
		for (player in players) {
			close(player, false)

			try {
				val preOpen = PlayerMenuPreOpenEvent(this, player)
				eventHandler.handlePreOpen(preOpen)

				if (preOpen.canceled) return

				_viewers[player] = _inventory
				player.openInventory(_inventory)

				if (job == null && updateDelay > 0 && viewers.isNotEmpty())
					setUpdateTask()

				val open = PlayerMenuOpenEvent(this, player, _inventory)
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

			if (job != null && updateDelay > 0 && _viewers.isEmpty())
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

	private fun updateSlotOnly(index: Int, slot: StaticSlotDSL<Inventory>, player: Player, inventory: Inventory) {
		val slotUpdate = PlayerMenuSlotUpdateEvent(this, index, slot, player, inventory)
		slot.eventHandler.handleUpdate(slotUpdate)
	}

	private fun setUpdateTask() {
		job = plugin.launch {
			while (isActive) {
				update()
				delay(updateDelay)
			}
		}
	}

	private fun removeUpdateTask() {
		job?.cancel()
		job = null
	}
}