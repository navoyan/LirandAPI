package lirand.api.coroutines.flow

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.job
import lirand.api.extensions.events.SimpleListener
import lirand.api.extensions.events.listen
import lirand.api.extensions.events.unregister
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Automatically cancels the coroutine in which the flow is collected on [player] exit.
 *
 * @param listener listens [PlayerQuitEvent]. Unregisters on player exit.
 */
fun <T : Event> SharedEventFlow<T>.assign(
	player: Player,
	listener: Listener = SimpleListener()
) = flow {
	val job = currentCoroutineContext().job

	plugin.listen<PlayerQuitEvent>(listener) { event ->
		if (event.player == player) {
			job.cancel()
			listener.unregister()
		}
	}

	emitAll(this@assign)
}