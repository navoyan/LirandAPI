package lirand.api.extensions.events

import lirand.api.extensions.server.server
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

inline fun <reified T : Event> Listener.listen(
	plugin: Plugin,
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	noinline block: T.() -> Unit
) {
	listen(plugin, T::class, priority, ignoreCancelled, block)
}

fun <T : Event> Listener.listen(
	plugin: Plugin,
	type: KClass<T>,
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	block: T.() -> Unit
) {
	server.pluginManager.registerEvent(
		type.java,
		this,
		priority,
		{ _, event ->
			if (type.isInstance(event))
				(event as? T)?.block()
		},
		plugin,
		ignoreCancelled
	)
}

inline fun <reified T : Event> Plugin.listen(
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	listener: Listener = SimpleListener(this),
	noinline onEvent: T.() -> Unit,
): Listener {
	return listener.apply {
		listen(this@listen, priority, ignoreCancelled, onEvent)
	}
}


fun Listener.unregister() = HandlerList.unregisterAll(this)

inline fun Plugin.events(
	listener: Listener = SimpleListener(this),
	crossinline block: Listener.() -> Unit
) = listener.apply(block)


open class SimpleListener(open val plugin: Plugin) : Listener