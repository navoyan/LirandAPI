package lirand.api.dsl.menu.builders.dynamic.anvil

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.*
import lirand.api.dsl.menu.builders.dynamic.anvil.slot.AnvilSlot
import lirand.api.dsl.menu.builders.dynamic.anvil.slot.AnvilSlotEventHandler
import lirand.api.dsl.menu.exposed.*
import lirand.api.dsl.menu.exposed.dynamic.Slot
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

	private var dynamicTitle: ((Player) -> String?)? = null
	override var title: String? = null

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


	override fun title(render: (Player) -> String?) {
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

	override fun update(player: Player) {
		if (!hasPlayer(player)) return

		val inventory = viewers.getValue(player)
		val updateEvent = PlayerMenuUpdateEvent(this, player, inventory)
		eventHandler.handleUpdate(updateEvent)

		for (index in rangeOfSlots) {
			val slot = getSlotOrBaseSlot(index)
			callSlotUpdateEvent(index, slot, player, inventory)
		}
	}

	override fun update() {
		for (player in viewers.keys) {
			update(player)
		}
	}

	override fun updateSlot(slot: Slot<AnvilInventory>, player: Player) {
		if (!hasPlayer(player)) return

		val slots = if (slot === baseSlot) {
			rangeOfSlots.mapNotNull { if (_slots[it] == null) it to slot else null }.toMap()
		}
		else {
			rangeOfSlots.mapNotNull { if (slot === _slots[it]) it to slot else null }.toMap()
		}

		val inventory = viewers.getValue(player)
		for ((index, slot) in slots) {
			callSlotUpdateEvent(index, slot, player, inventory)
		}
	}

	override fun updateSlot(slot: Slot<AnvilInventory>) {
		for (player in viewers.keys) {
			updateSlot(slot, player)
		}
	}

	override fun getInventory() = Inventory(InventoryType.ANVIL, this).apply {
		for (index in rangeOfSlots) {
			val slot = getSlotOrBaseSlot(index)
			setItem(index, slot.item?.clone())
		}
	}

	override fun openTo(player: Player) {
		close(player, false)

		try {
			val preOpenEvent = PlayerMenuPreOpenEvent(this, player)
			eventHandler.handlePreOpen(preOpenEvent)
			if (preOpenEvent.isCanceled) return


			val title = title ?: dynamicTitle?.invoke(player)
			val container = anvilWrapper.newContainerAnvil(player, title)?.apply {
				verifyObfuscatedFields(this)
			}
			val inventory = anvilWrapper.toBukkitInventory(container)?.apply {
				contents = inventory.contents
			} as AnvilInventory

			bukkitOwnerField.set(inventoryField.get(container), this)
			_viewers[player] = inventory

			scope.launch {
				delay(1.ticks)
				player.closeInventory()

				for (index in rangeOfSlots) {
					val slot = getSlotOrBaseSlot(index)
					val render = MenuSlotRenderEvent(this@AnvilMenuImpl, index, slot, player, inventory)
					slot.eventHandler.handleRender(render)
				}

				with(anvilWrapper) {
					val containerId = getNextContainerId(player, container)

					sendPacketOpenWindow(player, getNextContainerId(player, container), title)
					setActiveContainer(player, container)
					setActiveContainerId(container, containerId)
					addActiveContainerSlotListener(container, player)
				}


				val openEvent = PlayerMenuOpenEvent(this@AnvilMenuImpl, player, inventory)
				eventHandler.handleOpen(openEvent)

				if (updateDelay > Duration.ZERO && viewers.size == 1)
					setUpdateTask()
			}

		} catch (exception: Throwable) {
			exception.printStackTrace()
			removePlayer(player, true)
		}
	}

	override fun close(player: Player, closeInventory: Boolean) {
		if (player !in _viewers) return

		val menuClose = PlayerMenuCloseEvent(this, player)
		eventHandler.handleClose(menuClose)

		scope.launch {
			delay(1)
			removePlayer(player, closeInventory)
		}

		if (updateDelay > Duration.ZERO && viewers.isEmpty())
			removeUpdateTask()
	}


	private fun callSlotUpdateEvent(index: Int, slot: Slot<AnvilInventory>, player: Player, inventory: AnvilInventory) {
		val slotUpdate = PlayerMenuSlotUpdateEvent(this, index, slot, player, inventory)
		slot.eventHandler.handleUpdate(slotUpdate)
	}

	private fun removePlayer(player: Player, closeInventory: Boolean) {
		if (closeInventory) player.closeInventory()

		val viewing = _viewers.remove(player) != null
		if (viewing)
			clearPlayerData(player)
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