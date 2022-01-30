package lirand.api.dsl.menu.fixed

import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.dsl.menu.PlayerMenuCloseEvent
import lirand.api.dsl.menu.PlayerMenuMoveToEvent
import lirand.api.dsl.menu.PlayerMenuOpenEvent
import lirand.api.dsl.menu.PlayerMenuPreOpenEvent
import lirand.api.dsl.menu.PlayerMenuUpdateEvent
import lirand.api.menu.StaticMenu

@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class MenuDSLMarker

@MenuDSLMarker
interface StaticMenuDSL<S : StaticSlotDSL> : StaticMenu<S> {

	override val eventHandler: MenuDSLEventHandler

	@MenuDSLMarker
	fun onUpdate(update: PlayerMenuUpdateEvent) {
		eventHandler.updateCallbacks.add(update)
	}

	@MenuDSLMarker
	fun onClose(close: PlayerMenuCloseEvent) {
		eventHandler.closeCallbacks.add(close)
	}

	@MenuDSLMarker
	fun onMoveToMenu(moveToMenu: PlayerMenuMoveToEvent) {
		eventHandler.moveToMenuCallbacks.add(moveToMenu)
	}

	@MenuDSLMarker
	fun preOpen(preOpen: PlayerMenuPreOpenEvent) {
		eventHandler.preOpenCallbacks.add(preOpen)
	}

	@MenuDSLMarker
	fun onOpen(open: PlayerMenuOpenEvent) {
		eventHandler.openCallbacks.add(open)
	}
}