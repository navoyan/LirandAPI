package lirand.api.dsl.menu

import lirand.api.dsl.menu.builders.dynamic.anvil.AnvilMenuDSL
import lirand.api.dsl.menu.builders.fixed.StaticSlotDSL
import lirand.api.dsl.menu.exposed.MenuSlotInteractEvent
import lirand.api.dsl.menu.exposed.PlayerAnvilMenuPrepareEvent
import lirand.api.dsl.menu.exposed.PlayerMoveToMenuEvent
import lirand.api.dsl.menu.exposed.asMenu
import lirand.api.dsl.menu.exposed.fixed.StaticMenu
import lirand.api.dsl.menu.exposed.getMenu
import lirand.api.dsl.menu.exposed.getSlotOrBaseSlot
import lirand.api.dsl.menu.exposed.takeIfHasPlayer
import lirand.api.extensions.inventory.get
import lirand.api.extensions.inventory.isNotEmpty
import lirand.api.extensions.server.registerEvents
import lirand.api.extensions.server.server
import lirand.api.utilities.Initializable
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

internal class MenuController(val plugin: Plugin) : Listener, Initializable {

	private val supportedInventoryTypes = listOf(
		InventoryType.CHEST, InventoryType.ANVIL
	)


	override fun initialize() {
		plugin.registerEvents(this)
	}


	@EventHandler
	fun onPluginDisableEvent(event: PluginDisableEvent) {
		for (player in server.onlinePlayers) {
			val holder = player.openInventory.topInventory.holder

			if (holder is StaticMenu<*, *>) {
				holder.close(player, true)
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	fun onClickEvent(event: InventoryClickEvent) {
		if (event.view.type !in supportedInventoryTypes)
			return

		val player = event.whoClicked as Player
		val inventory = event.inventory

		val menu = inventory.asMenu()?.takeIfHasPlayer(player) ?: return

		if (inventory is AnvilInventory) {
			menu as AnvilMenuDSL

			handleMenuClick(menu, event, inventory)
			handleMenuMove(menu, event, inventory)
		}
		else {
			menu as StaticMenu<*, Inventory>

			handleMenuClick(menu, event, inventory)
			handleMenuMove(menu, event, inventory)
		}
	}

	private fun <I : Inventory> handleMenuClick(menu: StaticMenu<*, I>, event: InventoryClickEvent, inventory: I) {
		if (event.slot != event.rawSlot) return

		val slot = menu.getSlotOrBaseSlot(event.slot) as StaticSlotDSL<I>

		val interactEvent = MenuSlotInteractEvent(
			menu, inventory, event.whoClicked as Player,
			event.slot, slot, slot.cancelEvents,
			event.click, event.action,
			event.currentItem, event.cursor,
			event.hotbarButton
		)

		slot.eventHandler.handleInteract(interactEvent)

		if (interactEvent.isCanceled) event.isCancelled = true
	}

	private fun <I : Inventory> handleMenuMove(menu: StaticMenu<*, I>, event: InventoryClickEvent, inventory: I) {
		val slotIndex = event.rawSlot
		val slot = menu.getSlotOrBaseSlot(slotIndex) as StaticSlotDSL<I>

		with(event) {
			if ((this.slot == rawSlot && (cursor.isNotEmpty ||
						(hotbarButton != -1 && view.bottomInventory[hotbarButton].isNotEmpty)))
				|| (this.slot != rawSlot && this.click.isShiftClick && this.inventory.any { it.isNotEmpty })
			) {
				val movedItem = if (cursor.isNotEmpty)
					cursor
				else if (hotbarButton != -1)
					view.bottomInventory[hotbarButton]
				else
					currentItem

				val moveEvent = PlayerMoveToMenuEvent(
					menu, whoClicked as Player, inventory,
					slot.cancelEvents, movedItem ?: return, hotbarButton
				)

				menu.eventHandler.handleMoveToMenu(moveEvent)
			}
		}
	}

	@EventHandler
	fun onPrepareEvent(event: PrepareAnvilEvent) {
		val inventory = event.inventory
		val menu = inventory.asMenu() as? AnvilMenuDSL ?: return

		val player = menu.viewers.firstNotNullOfOrNull { (player, menuInventory) ->
			if (inventory == menuInventory) player
			else null
		} ?: return

		val prepareEvent = PlayerAnvilMenuPrepareEvent(menu, player, inventory)

		menu.eventHandler.handlePrepare(prepareEvent)
	}

	@EventHandler(ignoreCancelled = true)
	fun onDragEvent(event: InventoryDragEvent) {
		if (event.view.type !in supportedInventoryTypes)
			return

		val player = event.whoClicked as Player
		val menu = event.inventory.asMenu()?.takeIfHasPlayer(player)
		if (menu != null) {
			val pass = event.inventorySlots.firstOrNull { it in event.rawSlots }
			if (pass != null) event.isCancelled = true
		}
	}

	@EventHandler
	fun onCloseEvent(event: InventoryCloseEvent) {
		if (event.view.type !in supportedInventoryTypes)
			return

		val player = event.player as Player
		player.getMenu()?.close(player, false)
	}

	@EventHandler(ignoreCancelled = true)
	fun onPickupItemEvent(event: EntityPickupItemEvent) {
		val player = event.entity as? Player ?: return

		if (player.getMenu() != null)
			event.isCancelled = true
	}
}