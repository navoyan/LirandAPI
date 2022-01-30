@file:OptIn(ExperimentalCoroutinesApi::class)

package lirand.api.flow

import com.github.shynixn.mccoroutine.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onStart
import lirand.api.extensions.events.SimpleListener
import lirand.api.extensions.events.listen
import lirand.api.extensions.events.unregister
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.reflect.KClass

/**
 * Create an Event Flow that receives the specified Event [T].
 *
 * [assign] is use for auto cancel the Flow when the Player disconnects.
 */
inline fun <reified T : Event> Plugin.eventFlow(
	assign: Player? = null,
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	channel: Channel<T> = Channel(Channel.CONFLATED),
	listener: Listener = SimpleListener(this),
	assignListener: Listener = SimpleListener(this)
): EventFlow<T> = eventFlow(T::class, assign, priority, ignoreCancelled, channel, listener, assignListener)


/**
 * Create an Event Flow that receives the specified Event [type].
 *
 * [assign] is use for auto cancel the Flow when the Player disconnects.
 */
fun <T : Event> Plugin.eventFlow(
	type: KClass<T>,
	assign: Player? = null,
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	channel: Channel<T> = Channel(Channel.CONFLATED),
	listener: Listener = SimpleListener(this),
	assignListener: Listener = SimpleListener(this),
): EventFlow<T> = EventFlow(this, type, assign, channel, priority, ignoreCancelled, listener, assignListener)


/**
 * Creates a [eventFlow] for [PlayerEvent] that auto filter for only events from [player].
 */
inline fun <reified T : PlayerEvent> Plugin.playerEventFlow(
	player: Player,
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	channel: Channel<T> = Channel(Channel.CONFLATED),
	listener: Listener = SimpleListener(this),
) = eventFlow(T::class, player, priority, ignoreCancelled, channel, listener)
	.filter { it.player.name == player.name }



class EventFlow<T : Event> internal constructor(
	val plugin: Plugin,
	type: KClass<T>,
	assign: Player?,
	val channel: Channel<T>,
	priority: EventPriority,
	ignoreCancelled: Boolean,
	listener: Listener,
	assignListener: Listener
) : Flow<T> by channel.buildEventFlow(
	plugin, type, channel,
	priority, ignoreCancelled, listener
) {

	init {
		if (assign != null) {
			assignListener.apply {
				listen<PlayerQuitEvent>(plugin) { closeChannel(assign.uniqueId) }
				listen<PlayerKickEvent>(plugin) { closeChannel(assign.uniqueId) }
			}
		}

		channel.invokeOnClose {
			listener.unregister()
			assignListener.unregister()
		}
	}

	fun close() = channel.close()


	private fun PlayerEvent.closeChannel(assignId: UUID) {
		if (!channel.isClosedForSend && player.uniqueId == assignId)
			channel.close()
	}

}

private fun <T : Event> Channel<T>.buildEventFlow(
	plugin: Plugin,
	type: KClass<T>,
	channel: Channel<T>,
	priority: EventPriority,
	ignoreCancelled: Boolean,
	listener: Listener,
): Flow<T> =
	consumeAsFlow().onStart {
		listener.listen(plugin, type, priority, ignoreCancelled) {
			plugin.launch {
				if (!channel.isClosedForSend && !channel.isClosedForReceive)
					channel.send(this@listen)
			}
		}
	}