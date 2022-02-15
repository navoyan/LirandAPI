package lirand.api.dsl.menu

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.MenuEventHandler
import lirand.api.menu.PlayerMenuCloseEvent
import lirand.api.menu.PlayerMenuOpenEvent
import lirand.api.menu.PlayerMenuPreOpenEvent
import lirand.api.menu.PlayerMenuUpdateEvent
import lirand.api.menu.PlayerMoveToMenuEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias PlayerMenuUpdateCallback<I> = suspend PlayerMenuUpdateEvent<I>.() -> Unit
typealias PlayerMenuCloseCallback = suspend PlayerMenuCloseEvent.() -> Unit
typealias PlayerMenuMoveCallback<I> = PlayerMoveToMenuEvent<I>.() -> Unit

typealias PlayerMenuPreOpenCallback = suspend PlayerMenuPreOpenEvent.() -> Unit
typealias PlayerMenuOpenCallback<I> = suspend PlayerMenuOpenEvent<I>.() -> Unit

open class MenuDSLEventHandler<I : Inventory>(override val plugin: Plugin) : MenuEventHandler<I> {

	val updateCallbacks = mutableListOf<PlayerMenuUpdateCallback<I>>()
	val closeCallbacks = mutableListOf<PlayerMenuCloseCallback>()
	val moveToMenuCallbacks = mutableListOf<PlayerMenuMoveCallback<I>>()
	val preOpenCallbacks = mutableListOf<PlayerMenuPreOpenCallback>()
	val openCallbacks = mutableListOf<PlayerMenuOpenCallback<I>>()

	override fun handleUpdate(updateEvent: PlayerMenuUpdateEvent<I>) {
		for (callback in updateCallbacks) {
			plugin.launch {
				callback(updateEvent)
			}
		}
	}

	override fun handleClose(closeEvent: PlayerMenuCloseEvent) {
		for (callback in closeCallbacks) {
			plugin.launch {
				callback(closeEvent)
			}
		}
	}

	override fun handleMoveToMenu(moveToMenuEvent: PlayerMoveToMenuEvent<I>) {
		for (callback in moveToMenuCallbacks) {
			callback(moveToMenuEvent)
		}
	}

	override fun handlePreOpen(preOpenEvent: PlayerMenuPreOpenEvent) {
		for (callback in preOpenCallbacks) {
			plugin.launch {
				callback(preOpenEvent)
			}
		}
	}

	override fun handleOpen(openEvent: PlayerMenuOpenEvent<I>) {
		for (callback in openCallbacks) {
			plugin.launch {
				callback(openEvent)
			}
		}
	}

}