package lirand.api.dsl.menu.fixed

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.slot.PlayerMenuSlotInteractEvent
import lirand.api.menu.slot.PlayerMenuSlotUpdateEvent
import lirand.api.menu.slot.StaticSlotEventHandler
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerSlotUpdateCallback<I> = suspend PlayerMenuSlotUpdateEvent<I>.() -> Unit
typealias MenuPlayerSlotInteractCallback<I> = PlayerMenuSlotInteractEvent<I>.() -> Unit

open class StaticSlotDSLEventHandler<I : Inventory>(override val plugin: Plugin) : StaticSlotEventHandler<I> {

	val interactCallbacks = mutableListOf<MenuPlayerSlotInteractCallback<I>>()
	val updateCallbacks = mutableListOf<MenuPlayerSlotUpdateCallback<I>>()

	override fun handleInteract(interactEvent: PlayerMenuSlotInteractEvent<I>) {
		for (callback in interactCallbacks) {
			callback(interactEvent)
		}
	}

	override fun handleUpdate(updateEvent: PlayerMenuSlotUpdateEvent<I>) {
		for (callback in updateCallbacks) {
			plugin.launch {
				callback(updateEvent)
			}
		}
	}

	override fun clone(plugin: Plugin): StaticSlotDSLEventHandler<I> {
		return StaticSlotDSLEventHandler<I>(plugin).also {
			it.interactCallbacks.addAll(interactCallbacks)
			it.updateCallbacks.addAll(updateCallbacks)
		}
	}
}