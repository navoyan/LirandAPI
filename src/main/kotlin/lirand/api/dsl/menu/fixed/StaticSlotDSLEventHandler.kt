package lirand.api.dsl.menu.fixed

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.slot.PlayerMenuSlotInteract
import lirand.api.menu.slot.PlayerMenuSlotUpdate
import lirand.api.menu.slot.StaticSlotEventHandler
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerSlotUpdateEvent<I> = suspend PlayerMenuSlotUpdate<I>.() -> Unit
typealias MenuPlayerSlotInteractEvent<I> = PlayerMenuSlotInteract<I>.() -> Unit

open class StaticSlotDSLEventHandler<I : Inventory>(override val plugin: Plugin) : StaticSlotEventHandler<I> {

	val interactCallbacks = mutableListOf<MenuPlayerSlotInteractEvent<I>>()
	val updateCallbacks = mutableListOf<MenuPlayerSlotUpdateEvent<I>>()

	override fun interact(interact: PlayerMenuSlotInteract<I>) {
		for (callback in interactCallbacks) {
			callback(interact)
		}
	}

	override fun update(update: PlayerMenuSlotUpdate<I>) {
		for (callback in updateCallbacks) {
			plugin.launch {
				callback(update)
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