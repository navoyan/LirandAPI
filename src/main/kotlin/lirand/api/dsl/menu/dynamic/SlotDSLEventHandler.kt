package lirand.api.dsl.menu.dynamic

import lirand.api.dsl.menu.fixed.StaticSlotDSLEventHandler
import lirand.api.menu.slot.PlayerMenuSlotRender
import lirand.api.menu.slot.SlotEventHandler
import org.bukkit.plugin.Plugin

typealias MenuPlayerSlotRenderEvent = PlayerMenuSlotRender.() -> Unit

open class SlotDSLEventHandler(plugin: Plugin) : StaticSlotDSLEventHandler(plugin), SlotEventHandler {

	val renderCallbacks = mutableListOf<MenuPlayerSlotRenderEvent>()

	override fun render(render: PlayerMenuSlotRender) {
		for (callback in renderCallbacks) {
			callback(render)
		}
	}

	override fun clone(plugin: Plugin): SlotDSLEventHandler {
		return SlotDSLEventHandler(plugin).also {
			it.interactCallbacks.addAll(interactCallbacks)
			it.updateCallbacks.addAll(updateCallbacks)
			it.renderCallbacks.addAll(renderCallbacks)
		}
	}

}