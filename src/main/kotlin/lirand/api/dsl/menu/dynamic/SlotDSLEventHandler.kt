package lirand.api.dsl.menu.dynamic

import lirand.api.dsl.menu.fixed.StaticSlotDSLEventHandler
import lirand.api.menu.slot.PlayerMenuSlotRenderEvent
import lirand.api.menu.slot.SlotEventHandler
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerSlotRenderCallback<I> = PlayerMenuSlotRenderEvent<I>.() -> Unit

open class SlotDSLEventHandler<I : Inventory>(plugin: Plugin)
	: StaticSlotDSLEventHandler<I>(plugin), SlotEventHandler<I> {

	val renderCallbacks = mutableListOf<MenuPlayerSlotRenderCallback<I>>()

	override fun handleRender(renderEvent: PlayerMenuSlotRenderEvent<I>) {
		for (callback in renderCallbacks) {
			callback(renderEvent)
		}
	}

	override fun clone(plugin: Plugin): SlotDSLEventHandler<I> {
		return SlotDSLEventHandler<I>(plugin).also {
			it.interactCallbacks.addAll(interactCallbacks)
			it.updateCallbacks.addAll(updateCallbacks)
			it.renderCallbacks.addAll(renderCallbacks)
		}
	}

}