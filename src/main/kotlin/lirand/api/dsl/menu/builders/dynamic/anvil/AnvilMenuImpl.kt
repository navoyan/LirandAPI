package lirand.api.dsl.menu.builders.dynamic.anvil

import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.*
import lirand.api.dsl.menu.builders.dynamic.AbstractMenuDSL
import lirand.api.dsl.menu.builders.dynamic.anvil.slot.AnvilSlot
import lirand.api.dsl.menu.builders.dynamic.anvil.slot.AnvilSlotEventHandler
import lirand.api.dsl.menu.exposed.*
import lirand.api.dsl.menu.exposed.dynamic.Slot
import lirand.api.dsl.menu.exposed.fixed.*
import lirand.api.extensions.inventory.Inventory
import lirand.api.utilities.allFields
import lirand.api.utilities.ifTrue
import net.wesjd.anvilgui.version.VersionMatcher
import net.wesjd.anvilgui.version.VersionWrapper
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin
import java.lang.reflect.Field
import kotlin.collections.set
import kotlin.time.Duration


class AnvilMenuImpl(
	plugin: Plugin,
	cancelEvents: Boolean = true
) : AbstractMenuDSL<Slot<AnvilInventory>, AnvilInventory>(plugin, cancelEvents), AnvilMenuDSL {

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

	override val eventHandler: AnvilMenuDSLEventHandler = AnvilMenuDSLEventHandler(plugin)

	override val rangeOfSlots: IntRange = 0..1

	override var baseSlot: Slot<AnvilInventory> = AnvilSlot(plugin, null, cancelEvents, AnvilSlotEventHandler(plugin, eventHandler))


	override fun open(player: Player, backStack: MenuBackStack?) {
		close(player, false)

		try {
			backStack?.takeIf { !it.lastBacked }
				?.push(MenuBackStackFrame(this, player, MenuTypedDataMap(playerData[player])))
				?: run { backStack?.lastBacked = false }

			val preOpenEvent = PlayerMenuPreOpenEvent(this, player)
			eventHandler.handlePreOpen(preOpenEvent)
			if (preOpenEvent.isCanceled) return


			val title = title ?: dynamicTitle?.invoke(PlayerMenuEvent(this, player))
			val container = anvilWrapper.newContainerAnvil(player, title)?.apply {
				verifyObfuscatedFields(this)
			}
			val inventory = anvilWrapper.toBukkitInventory(container)?.apply {
				contents = inventory.contents
			} as AnvilInventory

			bukkitOwnerField.set(inventoryField.get(container), this)
			_views[player] = MenuView(this, player, inventory, backStack)

			scope.launch {
				delay(1.ticks)
				player.closeInventory()

				for (index in rangeOfSlots) {
					val slot = getSlotOrBaseSlot(index)
					val render = PlayerMenuSlotRenderEvent(this@AnvilMenuImpl, index, slot, player, inventory)
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

				if (updateDelay > Duration.ZERO && views.size == 1)
					setUpdateTask()
			}
		} catch (exception: Throwable) {
			exception.printStackTrace()
			removePlayer(player, true)
		}
	}

	override fun getInventory(): Inventory {
		return Inventory(InventoryType.ANVIL, this).apply {
			for (index in rangeOfSlots) {
				val slot = getSlotOrBaseSlot(index)
				setItem(index, slot.item?.clone())
			}
		}
	}
}