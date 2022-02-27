package lirand.api.coroutines.flow

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import lirand.api.extensions.events.SimpleListener
import lirand.api.extensions.events.listen
import lirand.api.extensions.events.unregister
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import kotlin.reflect.KClass

/**
 * Creates a SharedFlow that receives the specified [T] Event.
 *
 * @param considerSubscriptionCount determines whether to cancel or start
 * event listening when the number of subscriptions changes.
 */
inline fun <reified T : Event> Plugin.eventFlow(
	considerSubscriptionCount: Boolean = false,
	replay: Int = 0,
	extraBufferCapacity: Int = 1,
	onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	listener: Listener = SimpleListener(this)
): SharedFlow<T> = eventFlow(
	T::class, considerSubscriptionCount,
	replay, extraBufferCapacity, onBufferOverflow,
	priority, ignoreCancelled, listener
)


/**
 * Creates a SharedFlow that receives the specified [type] Event.
 *
 * @param considerSubscriptionCount determines whether to cancel or start
 * event listening when the number of subscriptions changes.
 */
fun <T : Event> Plugin.eventFlow(
	type: KClass<T>,
	considerSubscriptionCount: Boolean = false,
	replay: Int = 0,
	extraBufferCapacity: Int = 1,
	onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
	priority: EventPriority = EventPriority.NORMAL,
	ignoreCancelled: Boolean = false,
	listener: Listener = SimpleListener(this)
): SharedFlow<T> {

	val mutableEventFlow = MutableSharedFlow<T>(replay, extraBufferCapacity, onBufferOverflow)

	val emitterScope = CoroutineScope(minecraftDispatcher)

	if (!considerSubscriptionCount) {
		listener.listen(this, type, priority, ignoreCancelled) {
			emitterScope.launch {
				mutableEventFlow.emit(this@listen)
			}
		}
		return mutableEventFlow.asSharedFlow()
	}


	val subscriptionScope = CoroutineScope(minecraftDispatcher)
	val subscriptionCountState = mutableEventFlow.subscriptionCount

	subscriptionScope.launch {
		var lastSubscriptionCount = 0

		subscriptionCountState.collectLatest { count ->
			if (count == 0 && lastSubscriptionCount != 0) {
				listener.unregister()
			}
			else if (count == 1 && lastSubscriptionCount == 0) {
				listener.listen(this@eventFlow, type, priority, ignoreCancelled) {
					emitterScope.launch {
						mutableEventFlow.emit(this@listen)
					}
				}
			}
			lastSubscriptionCount = count
		}
	}
	return mutableEventFlow.asSharedFlow()
}