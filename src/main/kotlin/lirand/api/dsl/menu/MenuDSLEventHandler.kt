package lirand.api.dsl.menu

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lirand.api.menu.MenuEventHandler
import lirand.api.menu.PlayerMenuCloseEvent
import lirand.api.menu.PlayerMenuOpenEvent
import lirand.api.menu.PlayerMenuPreOpenEvent
import lirand.api.menu.PlayerMenuUpdateEvent
import lirand.api.menu.PlayerMoveToMenuEvent
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias PlayerMenuUpdateCallback<I> = PlayerMenuUpdateEvent<I>.(scope: CoroutineScope) -> Unit
typealias PlayerMenuCloseCallback = PlayerMenuCloseEvent.(scope: CoroutineScope) -> Unit
typealias PlayerMenuMoveCallback<I> = PlayerMoveToMenuEvent<I>.(scope: CoroutineScope) -> Unit

typealias PlayerMenuPreOpenCallback = PlayerMenuPreOpenEvent.(scope: CoroutineScope) -> Unit
typealias PlayerMenuOpenCallback<I> = PlayerMenuOpenEvent<I>.(scope: CoroutineScope) -> Unit

open class MenuDSLEventHandler<I : Inventory>(override val plugin: Plugin) : MenuEventHandler<I> {
	protected val scope = CoroutineScope(
		plugin.minecraftDispatcher + SupervisorJob() +
				CoroutineExceptionHandler { _, exception -> exception.printStackTrace() }
	)

	val updateCallbacks = mutableListOf<PlayerMenuUpdateCallback<I>>()
	val closeCallbacks = mutableListOf<PlayerMenuCloseCallback>()
	val moveToMenuCallbacks = mutableListOf<PlayerMenuMoveCallback<I>>()
	val preOpenCallbacks = mutableListOf<PlayerMenuPreOpenCallback>()
	val openCallbacks = mutableListOf<PlayerMenuOpenCallback<I>>()

	override fun handleUpdate(updateEvent: PlayerMenuUpdateEvent<I>) {
		for (callback in updateCallbacks) {
			scope.launch {
				callback(updateEvent, this)
			}
		}
	}

	override fun handleClose(closeEvent: PlayerMenuCloseEvent) {
		for (callback in closeCallbacks) {
			scope.launch {
				callback(closeEvent, this)
			}
		}
	}

	override fun handleMoveToMenu(moveToMenuEvent: PlayerMoveToMenuEvent<I>) {
		for (callback in moveToMenuCallbacks) {
			scope.launch {
				callback(moveToMenuEvent, this)
			}
		}
	}

	override fun handlePreOpen(preOpenEvent: PlayerMenuPreOpenEvent) {
		for (callback in preOpenCallbacks) {
			scope.launch {
				callback(preOpenEvent, this)
			}
		}
	}

	override fun handleOpen(openEvent: PlayerMenuOpenEvent<I>) {
		for (callback in openCallbacks) {
			scope.launch {
				callback(openEvent, this)
			}
		}
	}

}