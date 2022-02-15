package lirand.api.dsl.menu.fixed

import lirand.api.dsl.menu.MenuDSLEventHandler
import lirand.api.dsl.menu.PlayerMenuCloseCallback
import lirand.api.dsl.menu.PlayerMenuMoveCallback
import lirand.api.dsl.menu.PlayerMenuOpenCallback
import lirand.api.dsl.menu.PlayerMenuPreOpenCallback
import lirand.api.dsl.menu.PlayerMenuUpdateCallback
import lirand.api.menu.StaticMenu
import org.bukkit.inventory.Inventory

@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class MenuDSLMarker

@MenuDSLMarker
interface StaticMenuDSL<S : StaticSlotDSL<I>, I : Inventory> : StaticMenu<S, I> {

	override val eventHandler: MenuDSLEventHandler<I>

	fun onUpdate(updateCallback: PlayerMenuUpdateCallback<I>) {
		eventHandler.updateCallbacks.add(updateCallback)
	}

	fun onClose(closeCallback: PlayerMenuCloseCallback) {
		eventHandler.closeCallbacks.add(closeCallback)
	}

	fun onMoveToMenu(moveToMenuCallback: PlayerMenuMoveCallback<I>) {
		eventHandler.moveToMenuCallbacks.add(moveToMenuCallback)
	}

	fun preOpen(preOpenCallback: PlayerMenuPreOpenCallback) {
		eventHandler.preOpenCallbacks.add(preOpenCallback)
	}

	fun onOpen(openCallback: PlayerMenuOpenCallback<I>) {
		eventHandler.openCallbacks.add(openCallback)
	}
}