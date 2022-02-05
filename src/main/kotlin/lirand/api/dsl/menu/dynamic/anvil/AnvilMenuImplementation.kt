package lirand.api.dsl.menu.dynamic.anvil

import com.github.shynixn.mccoroutine.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import lirand.api.dsl.menu.dynamic.SlotDSL
import lirand.api.dsl.menu.dynamic.anvil.slot.AnvilSlot
import lirand.api.dsl.menu.dynamic.anvil.slot.AnvilSlotEventHandler
import lirand.api.extensions.inventory.Inventory
import lirand.api.menu.PlayerMenuClose
import lirand.api.menu.PlayerMenuOpen
import lirand.api.menu.PlayerMenuPreOpen
import lirand.api.menu.PlayerMenuUpdate
import lirand.api.menu.getSlotOrBaseSlot
import lirand.api.menu.rangeOfSlots
import lirand.api.menu.rawSlot
import lirand.api.menu.slot.PlayerMenuSlotRender
import lirand.api.menu.slot.PlayerMenuSlotUpdate
import lirand.api.menu.viewersFromPlayers
import lirand.api.utilities.allFields
import lirand.api.utilities.ifTrue
import net.wesjd.anvilgui.version.VersionMatcher
import net.wesjd.anvilgui.version.VersionWrapper
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.plugin.Plugin
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


class AnvilMenuImplementation(
	override val plugin: Plugin,
	override var cancelEvents: Boolean = true
) : AnvilMenu {

	companion object {
		private val anvilWrapper: VersionWrapper = VersionMatcher().match()

		private lateinit var inventoryField: Field
		private lateinit var bukkitOwnerField: Field

		private fun verifyObfuscatedFields(container: Any) {
			if (::inventoryField.isInitialized) return

			inventoryField = container::class.java.allFields
				.find {
					if (it.type.simpleName != "IInventory") return@find false
					it.isAccessible = true
					val inventoryClass = it.get(container)::class.java.superclass

					return@find (inventoryClass.simpleName == "InventorySubcontainer").ifTrue {
						bukkitOwnerField = inventoryClass.getDeclaredField("bukkitOwner").apply {
							isAccessible = true
						}
					}
				}!!
		}

	}

	private var currentInventory: AnvilInventory? = null
	private var currentContainer: Any? = null
		set(value) {
			field = value

			if (value == null) return
			verifyObfuscatedFields(value)

			currentInventory = anvilWrapper.toBukkitInventory(value)?.apply {
				for (index in 1..2) {
					val slot = _slots[index] ?: baseSlot
					setItem(rawSlot(index), slot.item)
				}
			} as AnvilInventory
		}

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


	override val eventHandler: AnvilMenuEventHandler = AnvilMenuEventHandler(plugin)

	private val _viewers = HashMap<Player, AnvilInventory>()

	override val viewers: Map<Player, AnvilInventory> get() = _viewers
	private val _slots = TreeMap<Int, SlotDSL<AnvilInventory>>()

	override val slots: Map<Int, SlotDSL<AnvilInventory>> get() = _slots

	override var baseSlot: SlotDSL<AnvilInventory> =
		AnvilSlot(null, cancelEvents, AnvilSlotEventHandler(plugin, eventHandler))
	override val data = WeakHashMap<String, Any>()

	override val playerData = WeakHashMap<Player, WeakHashMap<String, Any>>()


	override fun title(render: (Player?) -> String?) {
		dynamicTitle = render
	}

	override fun setSlot(index: Int, slot: SlotDSL<AnvilInventory>) {
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

			for (index in rangeOfSlots) {
				val slot = getSlotOrBaseSlot(index)
				updateSlotOnly(index, slot, player, inventory)
			}
		}
	}

	override fun update() = update(viewers.keys)

	override fun updateSlot(slot: SlotDSL<AnvilInventory>, players: Set<Player>) {
		val slots: Map<Int, SlotDSL<AnvilInventory>> = if (slot === baseSlot) {
			rangeOfSlots.mapNotNull { if (_slots[it] == null) it to slot else null }.toMap()
		}
		else {
			rangeOfSlots.mapNotNull { if (slot === _slots[it]) it to slot else null }.toMap()
		}

		for ((player, inventory) in viewersFromPlayers(players)) {
			for ((index, slot) in slots) {
				updateSlotOnly(index, slot, player, inventory)
			}
		}
	}

	override fun updateSlot(slot: SlotDSL<AnvilInventory>) = updateSlot(slot, viewers.keys)

	override fun getInventory(): AnvilInventory {
		return currentInventory ?: Inventory<AnvilInventory>(this, InventoryType.ANVIL)
	}

	override fun openTo(vararg players: Player) {
		for (player in players) {
			close(player, true)

			try {
				anvilWrapper.handleInventoryCloseEvent(player)
				anvilWrapper.setActiveContainerDefault(player)

				val preOpen = PlayerMenuPreOpen(this, player)
				eventHandler.preOpen(preOpen)

				if (preOpen.canceled) return

				val title = dynamicTitle(player)

				currentContainer = anvilWrapper.newContainerAnvil(player, title)

				bukkitOwnerField.set(inventoryField.get(currentContainer), this)

				_viewers[player] = inventory

				for (index in rangeOfSlots) {
					val slot = getSlotOrBaseSlot(index)

					val render = PlayerMenuSlotRender(this, index, slot, player, inventory)

					slot.eventHandler.render(render)
				}

				val containerId = anvilWrapper.getNextContainerId(player, currentContainer)

				anvilWrapper.sendPacketOpenWindow(player, containerId, title)
				anvilWrapper.setActiveContainer(player, currentContainer)
				anvilWrapper.setActiveContainerId(currentContainer, containerId)
				anvilWrapper.addActiveContainerSlotListener(currentContainer, player)

				if (job == null && updateDelay > 0 && _viewers.isNotEmpty())
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

	private fun updateSlotOnly(index: Int, slot: SlotDSL<AnvilInventory>, player: Player, inventory: AnvilInventory) {
		val slotUpdate = PlayerMenuSlotUpdate(this, index, slot, player, inventory)
		slot.eventHandler.update(slotUpdate)
	}

	private fun removePlayer(player: Player, closeInventory: Boolean): Boolean {
		if (closeInventory) player.closeInventory()

		val viewing = _viewers.remove(player) != null
		if (viewing)
			clearPlayerData(player)

		return viewing
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