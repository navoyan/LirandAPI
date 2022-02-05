package lirand.api.dsl.menu.fixed

import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.dsl.menu.PlayerMenuCloseEvent
import lirand.api.dsl.menu.PlayerMenuMoveToEvent
import lirand.api.dsl.menu.PlayerMenuOpenEvent
import lirand.api.dsl.menu.PlayerMenuPreOpenEvent
import lirand.api.dsl.menu.PlayerMenuUpdateEvent
import lirand.api.menu.StaticMenu
import org.bukkit.inventory.Inventory

@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class MenuDSLMarker

@MenuDSLMarker
interface StaticMenuDSL<S : StaticSlotDSL<I>, I : Inventory> : StaticMenu<S, I> {

	override val eventHandler: MenuDSLEventHandler<I>

	@MenuDSLMarker
	fun onUpdate(update: PlayerMenuUpdateEvent<I>) {
		eventHandler.updateCallbacks.add(update)
	}

	@MenuDSLMarker
	fun onClose(close: PlayerMenuCloseEvent) {
		eventHandler.closeCallbacks.add(close)
	}

	@MenuDSLMarker
	fun onMoveToMenu(moveToMenu: PlayerMenuMoveToEvent<I>) {
		eventHandler.moveToMenuCallbacks.add(moveToMenu)
	}

	@MenuDSLMarker
	fun preOpen(preOpen: PlayerMenuPreOpenEvent) {
		eventHandler.preOpenCallbacks.add(preOpen)
	}

	@MenuDSLMarker
	fun onOpen(open: PlayerMenuOpenEvent<I>) {
		eventHandler.openCallbacks.add(open)
	}
}