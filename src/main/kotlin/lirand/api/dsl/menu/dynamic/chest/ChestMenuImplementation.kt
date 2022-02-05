package lirand.api.dsl.menu.dynamic.chest

import com.github.shynixn.mccoroutine.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.dynamic.SlotDSLEventHandler
import lirand.api.dsl.menu.dynamic.chest.slot.ChestSlot
import lirand.api.extensions.inventory.Inventory
import lirand.api.menu.PlayerMenuClose
import lirand.api.menu.PlayerMenuOpen
import lirand.api.menu.PlayerMenuPreOpen
import lirand.api.menu.PlayerMenuUpdate
import lirand.api.menu.getSlotOrBaseSlot
import lirand.api.menu.rangeOfSlots
import lirand.api.menu.slot.PlayerMenuSlotRender
import lirand.api.menu.slot.PlayerMenuSlotUpdate
import lirand.api.menu.viewersFromPlayers
import lirand.api.utilities.ifTrue
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import java.util.*

class ChestMenuImplementation(
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

	private val _slots = TreeMap<Int, SlotDSL<Inventory>>()
	override val slots: Map<Int, SlotDSL<Inventory>> get() = _slots

	override val data = WeakHashMap<String, Any>()
	override val playerData = WeakHashMap<Player, WeakHashMap<String, Any>>()

	override val eventHandler = MenuDSLEventHandler<Inventory>(plugin)

	override var baseSlot: SlotDSL<Inventory> =
		ChestSlot(null, cancelEvents, SlotDSLEventHandler<Inventory>(plugin))


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
			val update = PlayerMenuUpdate(this, player, inventory)
			eventHandler.update(update)

			for (i in rangeOfSlots) {
				val slot = getSlotOrBaseSlot(i)
				updateSlotOnly(i, slot, player, inventory)
			}
		}
	}

	override fun update() = update(_viewers.keys)

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

	override fun updateSlot(slot: SlotDSL<Inventory>) = updateSlot(slot, _viewers.keys)

	override fun openTo(vararg players: Player) {
		for (player in players) {
			close(player, false)

			try {
				val inventory = inventory

				val preOpen = PlayerMenuPreOpen(this, player)
				eventHandler.preOpen(preOpen)

				if (preOpen.canceled) return

				_viewers[player] = inventory

				for (index in rangeOfSlots) {
					val slot = getSlotOrBaseSlot(index)

					val render = PlayerMenuSlotRender(this, index, slot, player, inventory)

					slot.eventHandler.render(render)
				}

				player.openInventory(inventory)

				val open = PlayerMenuOpen(this, player, inventory)
				eventHandler.open(open)

				if (job == null && updateDelay > 0 && _viewers.isNotEmpty())
					setUpdateTask()

			} catch (exception: Throwable) {
				exception.printStackTrace()
				removePlayer(player, true)
			}
		}
	}

	override fun getInventory(): Inventory {
		val slots = rangeOfSlots
		val inventory = Inventory(this, slots.last, title)

		for (i in slots) {
			val slot = getSlotOrBaseSlot(i)

			val item = slot.item?.clone()
			inventory.setItem(i - 1, item)
		}

		return inventory
	}

	private fun removePlayer(player: Player, closeInventory: Boolean): Boolean {
		if (closeInventory) player.closeInventory()

		val viewing = _viewers.remove(player) != null
		if (viewing)
			clearPlayerData(player)

		return viewing
	}

	override fun close(player: Player, closeInventory: Boolean) {
		removePlayer(player, closeInventory).ifTrue {
			val menuClose = PlayerMenuClose(this, player)
			eventHandler.close(menuClose)

			if (job != null && updateDelay > 0 && _viewers.isEmpty())
				removeUpdateTask()
		}
	}

	private fun updateSlotOnly(index: Int, slot: SlotDSL<Inventory>, player: Player, inventory: Inventory) {
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