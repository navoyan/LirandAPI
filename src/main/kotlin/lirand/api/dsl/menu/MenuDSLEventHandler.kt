package lirand.api.dsl.menu

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.MenuEventHandler
import lirand.api.menu.PlayerMenuClose
import lirand.api.menu.PlayerMenuOpen
import lirand.api.menu.PlayerMenuPreOpen
import lirand.api.menu.PlayerMenuUpdate
import lirand.api.menu.PlayerMoveToMenu
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias PlayerMenuUpdateEvent<I> = suspend PlayerMenuUpdate<I>.() -> Unit
typealias PlayerMenuCloseEvent = suspend PlayerMenuClose.() -> Unit
typealias PlayerMenuMoveToEvent<I> = PlayerMoveToMenu<I>.() -> Unit

typealias PlayerMenuPreOpenEvent = suspend PlayerMenuPreOpen.() -> Unit
typealias PlayerMenuOpenEvent<I> = suspend PlayerMenuOpen<I>.() -> Unit

open class MenuDSLEventHandler<I : Inventory>(override val plugin: Plugin) : MenuEventHandler<I> {

	val updateCallbacks = mutableListOf<PlayerMenuUpdateEvent<I>>()
	val closeCallbacks = mutableListOf<PlayerMenuCloseEvent>()
	val moveToMenuCallbacks = mutableListOf<PlayerMenuMoveToEvent<I>>()
	val preOpenCallbacks = mutableListOf<PlayerMenuPreOpenEvent>()
	val openCallbacks = mutableListOf<PlayerMenuOpenEvent<I>>()

	override fun update(update: PlayerMenuUpdate<I>) {
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

	override fun moveToMenu(moveToMenu: PlayerMoveToMenu<I>) {
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

	override fun open(open: PlayerMenuOpen<I>) {
		for (callback in openCallbacks) {
			plugin.launch {
				callback(open)
			}
		}
	}

}