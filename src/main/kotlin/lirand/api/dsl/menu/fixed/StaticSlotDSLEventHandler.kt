package lirand.api.dsl.menu.fixed

import com.github.shynixn.mccoroutine.launch
import lirand.api.menu.slot.PlayerMenuSlotInteract
import lirand.api.menu.slot.PlayerMenuSlotUpdate
import lirand.api.menu.slot.StaticSlotEventHandler
import org.bukkit.plugin.Plugin

typealias MenuPlayerSlotUpdateEvent = suspend PlayerMenuSlotUpdate.() -> Unit
typealias MenuPlayerSlotInteractEvent = PlayerMenuSlotInteract.() -> Unit

open class StaticSlotDSLEventHandler(override val plugin: Plugin) : StaticSlotEventHandler {

	val interactCallbacks = mutableListOf<MenuPlayerSlotInteractEvent>()
	val updateCallbacks = mutableListOf<MenuPlayerSlotUpdateEvent>()

	override fun interact(interact: PlayerMenuSlotInteract) {
		for (callback in interactCallbacks) {
			callback(interact)
		}
	}

	override fun update(update: PlayerMenuSlotUpdate) {
		for (callback in updateCallbacks) {
			plugin.launch {
				callback(update)
			}
		}
	}

	override fun clone(plugin: Plugin): StaticSlotDSLEventHandler {
		return StaticSlotDSLEventHandler(plugin).also {
			it.interactCallbacks.addAll(interactCallbacks)
			it.updateCallbacks.addAll(updateCallbacks)
		}
	}
}