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
import lirand.api.menu.PlayerMenuClose
import lirand.api.menu.PlayerMenuOpen
import lirand.api.menu.PlayerMenuPreOpen
import lirand.api.menu.PlayerMenuUpdate
import lirand.api.menu.getSlotOrBaseSlot
import lirand.api.menu.rangeOfSlots
import lirand.api.menu.slot.PlayerMenuSlotUpdate
import lirand.api.utilities.ifTrue
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class StaticChestMenuImplementation(
	override val plugin: Plugin,
	override val lines: Int,
	override var title: String,
	override var cancelEvents: Boolean
) : StaticChestMenu {

	private var inventory: Inventory = Inventory(this, lines * 9, title)

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

	private val _slots = TreeMap<Int, StaticSlotDSL<Inventory>>()
	override val slots: Map<Int, StaticSlotDSL<Inventory>> get() = _slots

	override val data = WeakHashMap<String, Any>()
	override val playerData = WeakHashMap<Player, WeakHashMap<String, Any>>()

	override val eventHandler = MenuDSLEventHandler<Inventory>(plugin)

	override var baseSlot: StaticSlotDSL<Inventory> = StaticChestSlot(cancelEvents, StaticSlotDSLEventHandler(plugin))


	override fun setSlot(index: Int, slot: StaticSlotDSL<Inventory>) {
		if (index !in rangeOfSlots) return

		if (slot is StaticChestSlot && slot.menu == null
			&& slot.slotIndex <= 0
		) {

			slot.menu = this
			slot.slotIndex = index

			_slots[index] = slot
		}
		else if (slot !is StaticChestSlot) {
			_slots[index] = slot
		}
	}

	override fun removeSlot(index: Int) {
		_slots.remove(index)?.let {
			if (it is ChestSlot) {
				inventory.clear(index - 1)
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
			val update = PlayerMenuUpdate(this, player, inventory)
			eventHandler.update(update)

			for (i in rangeOfSlots) {
				val slot = getSlotOrBaseSlot(i)
				updateSlotOnly(i, slot, player, inventory)
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
				updateSlotOnly(index, slot, player, inventory)
			}
		}
	}

	override fun getInventory() = inventory

	override fun setInventory(inventory: Inventory) {
		this.inventory.storageContents = inventory.storageContents
	}

	override fun openTo(vararg players: Player) {
		for (player in players) {
			close(player, false)

			try {
				val preOpen = PlayerMenuPreOpen(this, player)
				eventHandler.preOpen(preOpen)

				if (preOpen.canceled) return

				_viewers[player] = inventory
				player.openInventory(inventory)

				if (job == null && updateDelay > 0 && viewers.isNotEmpty())
					setUpdateTask()

				val open = PlayerMenuOpen(this, player, inventory)
				eventHandler.open(open)

			} catch (exception: Throwable) {
				exception.printStackTrace()
				removePlayer(player, true)
			}
		}
	}

	override fun close(player: Player, closeInventory: Boolean) {
		removePlayer(player, closeInventory).ifTrue {
			val menuClose = PlayerMenuClose(this, player)
			eventHandler.close(menuClose)

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
		val slotUpdate = PlayerMenuSlotUpdate(this, index, slot, player, inventory)
		slot.eventHandler.update(slotUpdate)
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