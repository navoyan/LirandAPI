package lirand.api.dsl.menu.builders.dynamic.anvil

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lirand.api.dsl.menu.builders.dynamic.anvil.slot.AnvilSlot
import lirand.api.dsl.menu.builders.dynamic.anvil.slot.AnvilSlotEventHandler
import lirand.api.dsl.menu.exposed.MenuSlotRenderEvent
import lirand.api.dsl.menu.exposed.PlayerMenuCloseEvent
import lirand.api.dsl.menu.exposed.PlayerMenuOpenEvent
import lirand.api.dsl.menu.exposed.PlayerMenuPreOpenEvent
import lirand.api.dsl.menu.exposed.PlayerMenuSlotUpdateEvent
import lirand.api.dsl.menu.exposed.PlayerMenuUpdateEvent
import lirand.api.dsl.menu.exposed.dynamic.Slot
import lirand.api.dsl.menu.exposed.getSlotOrBaseSlot
import lirand.api.dsl.menu.exposed.getViewersFromPlayers
import lirand.api.extensions.inventory.Inventory
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
import kotlin.time.Duration


class AnvilMenuImpl(
	override val plugin: Plugin,
	override var cancelEvents: Boolean = true
) : AnvilMenuDSL {

	private companion object {
		val anvilWrapper: VersionWrapper = VersionMatcher().match()

		lateinit var inventoryField: Field
		lateinit var bukkitOwnerField: Field

		fun verifyObfuscatedFields(container: Any) {
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
	override var updateDelay: Duration = Duration.ZERO
		set(value) {
			field = value.takeIf { it >= Duration.ZERO } ?: Duration.ZERO
			removeUpdateTask()
			if (value > Duration.ZERO && viewers.isNotEmpty())
				setUpdateTask()
		}


	override val eventHandler: AnvilMenuDSLEventHandler = AnvilMenuDSLEventHandler(plugin)

	private val _viewers = HashMap<Player, AnvilInventory>()
	override val viewers: Map<Player, AnvilInventory> get() = _viewers

	override val rangeOfSlots: IntRange = 0..1

	private val _slots = TreeMap<Int, Slot<AnvilInventory>>()
	override val slots: Map<Int, Slot<AnvilInventory>> get() = _slots

	override var baseSlot: Slot<AnvilInventory> =
		AnvilSlot(plugin, null, cancelEvents, AnvilSlotEventHandler(plugin, eventHandler))

	override val data = WeakHashMap<String, Any>()
	override val playerData = WeakHashMap<Player, MutableMap<String, Any>>()


	override fun title(render: (Player?) -> String?) {
		dynamicTitle = render
	}

	override fun setSlot(index: Int, slot: Slot<AnvilInventory>) {
		if (index in rangeOfSlots)
			_slots[index] = slot
	}

	override fun removeSlot(index: Int) {
		_slots.remove(index)
	}

	override fun clearSlots() {
		_slots.clear()
	}

	override fun update(players: Collection<Player>) {
		val viewers = getViewersFromPlayers(players)

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

	override fun updateSlot(slot: Slot<AnvilInventory>, players: Collection<Player>) {
		val slots = if (slot === baseSlot) {
			rangeOfSlots.mapNotNull { if (_slots[it] == null) it to slot else null }.toMap()
		}
		else {
			rangeOfSlots.mapNotNull { if (slot === _slots[it]) it to slot else null }.toMap()
		}

		for ((player, inventory) in getViewersFromPlayers(players)) {
			for ((index, slot) in slots) {
				updateSlotOnly(index, slot, player, inventory)
			}
		}
	}

	override fun updateSlot(slot: Slot<AnvilInventory>) = updateSlot(slot, viewers.keys)

	override fun getInventory() = Inventory<AnvilInventory>(InventoryType.ANVIL, this)

	override fun openTo(players: Collection<Player>) {
		for (player in players) {
			close(player, true)

			try {
				val preOpenEvent = PlayerMenuPreOpenEvent(this, player)
				eventHandler.handlePreOpen(preOpenEvent)
				if (preOpenEvent.canceled) return


				val title = dynamicTitle(player)
				val currentContainer = anvilWrapper.newContainerAnvil(player, title)?.apply {
					verifyObfuscatedFields(this)
				}
				val currentInventory = anvilWrapper.toBukkitInventory(this)?.apply {
					for (index in rangeOfSlots) {
						val slot = _slots[index] ?: baseSlot
						setItem(index, slot.item)
					}
				} as AnvilInventory

				bukkitOwnerField.set(inventoryField.get(currentContainer), this)
				_viewers[player] = currentInventory


				for (index in rangeOfSlots) {
					val slot = getSlotOrBaseSlot(index)
					val render = MenuSlotRenderEvent(this, index, slot, player, inventory)
					slot.eventHandler.handleRender(render)
				}


				with(anvilWrapper) {
					val containerId = getNextContainerId(player, currentContainer)

					sendPacketOpenWindow(player, getNextContainerId(player, currentContainer), title)
					setActiveContainer(player, currentContainer)
					setActiveContainerId(currentContainer, containerId)
					addActiveContainerSlotListener(currentContainer, player)
				}


				if (updateDelay > Duration.ZERO && viewers.size == 1)
					setUpdateTask()


				val openEvent = PlayerMenuOpenEvent(this, player, inventory)
				eventHandler.handleOpen(openEvent)

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

	private fun updateSlotOnly(index: Int, slot: Slot<AnvilInventory>, player: Player, inventory: AnvilInventory) {
		val slotUpdate = PlayerMenuSlotUpdateEvent(this, index, slot, player, inventory)
		slot.eventHandler.handleUpdate(slotUpdate)
	}

	private fun removePlayer(player: Player, closeInventory: Boolean): Boolean {
		if (closeInventory) player.closeInventory()

		val viewing = _viewers.remove(player) != null
		if (viewing)
			clearPlayerData(player)

		return viewing
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