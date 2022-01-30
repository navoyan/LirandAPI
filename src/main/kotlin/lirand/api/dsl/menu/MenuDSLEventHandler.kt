package lirand.api.dsl.menu

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.MenuEventHandler
import lirand.api.menu.PlayerMenuClose
import lirand.api.menu.PlayerMenuOpen
import lirand.api.menu.PlayerMenuPreOpen
import lirand.api.menu.PlayerMenuUpdate
import lirand.api.menu.PlayerMoveToMenu
import org.bukkit.plugin.Plugin

typealias PlayerMenuUpdateEvent = suspend PlayerMenuUpdate.() -> Unit
typealias PlayerMenuCloseEvent = suspend PlayerMenuClose.() -> Unit
typealias PlayerMenuMoveToEvent = PlayerMoveToMenu.() -> Unit

typealias PlayerMenuPreOpenEvent = suspend PlayerMenuPreOpen.() -> Unit
typealias PlayerMenuOpenEvent = suspend PlayerMenuOpen.() -> Unit

open class MenuDSLEventHandler(override val plugin: Plugin) : MenuEventHandler {

	val updateCallbacks = mutableListOf<PlayerMenuUpdateEvent>()
	val closeCallbacks = mutableListOf<PlayerMenuCloseEvent>()
	val moveToMenuCallbacks = mutableListOf<PlayerMenuMoveToEvent>()
	val preOpenCallbacks = mutableListOf<PlayerMenuPreOpenEvent>()
	val openCallbacks = mutableListOf<PlayerMenuOpenEvent>()

	override fun update(update: PlayerMenuUpdate) {
		for (callback in updateCallbacks) {
			plugin.launch {
				callback(update)
			}
		}
	}

	override fun close(close: PlayerMenuClose) {
		for (callback in closeCallbacks) {
			plugin.launch {
				callback(close)
			}
		}
	}

	override fun moveToMenu(moveToMenu: PlayerMoveToMenu) {
		for (callback in moveToMenuCallbacks) {
			callback(moveToMenu)
		}
	}

	override fun preOpen(preOpen: PlayerMenuPreOpen) {
		for (callback in preOpenCallbacks) {
			plugin.launch {
				callback(preOpen)
			}
		}
	}

	override fun open(open: PlayerMenuOpen) {
		for (callback in openCallbacks) {
			plugin.launch {
				callback(open)
			}
		}
	}

}