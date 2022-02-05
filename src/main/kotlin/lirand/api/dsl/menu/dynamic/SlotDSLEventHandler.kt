package lirand.api.dsl.menu.dynamic

import lirand.api.dsl.menu.fixed.StaticSlotDSLEventHandler
import lirand.api.menu.slot.PlayerMenuSlotRender
import lirand.api.menu.slot.SlotEventHandler
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

typealias MenuPlayerSlotRenderEvent<I> = PlayerMenuSlotRender<I>.() -> Unit

open class SlotDSLEventHandler<I : Inventory>(plugin: Plugin)
	: StaticSlotDSLEventHandler<I>(plugin), SlotEventHandler<I> {

	val renderCallbacks = mutableListOf<MenuPlayerSlotRenderEvent<I>>()

	override fun render(render: PlayerMenuSlotRender<I>) {
		for (callback in renderCallbacks) {
			callback(render)
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