package lirand.api.collections.online

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

typealias PlayerQuitMapCallback<V> = (Player, V) -> Unit


/**
 * Returns an empty new [OnlinePlayerMap].
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerMap.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun <V> Plugin.onlinePlayerMapOf(
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitMapCallback<V>? = null
) = OnlinePlayerMap(this, defaultQuitCallback, quitAllOnPluginDisable)

/**
 * Returns a new [OnlinePlayerMap] with specified elements given as **key - value** pairs.
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerMap.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun <V> Plugin.onlinePlayerMapOf(
	vararg elements: Pair<Player, V>,
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitMapCallback<V>? = null
) = OnlinePlayerMap(this, defaultQuitCallback, quitAllOnPluginDisable).apply { putAll(elements) }

/**
 * Returns a new [OnlinePlayerMap] with specified elements given as **key - value - quit callback** triples.
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerMap.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun <V> Plugin.onlinePlayerMapOf(
	vararg elements: Triple<Player, V, PlayerQuitMapCallback<V>>,
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitMapCallback<V>? = null
) = OnlinePlayerMap(this, defaultQuitCallback, quitAllOnPluginDisable).apply {
	elements.forEach { (player, value, playerQuitCallback) ->
		put(player, value, playerQuitCallback)
	}
}



/**
 * A [HashMap] whose players (elements) are removed automatically
 * on their exit or plugin disable invoking provided [PlayerQuitMapCallback].
 */
class OnlinePlayerMap<V>(
	override val plugin: Plugin,
	/**
	 * Callback that is invoked when no other callback is provided for the player.
	 */
	val defaultQuitCallback: PlayerQuitMapCallback<V>? = null,
	/**
	 * Determines whether call [quitAll] or not when plugin gets disabled.
	 */
	override val quitAllOnPluginDisable: Boolean = false
) : HashMap<Player, V>(), OnlinePlayerCallbackable<V?> {

	private val quitCallbacks = hashMapOf<Player, PlayerQuitMapCallback<V>>()


	init {
		OnlinePlayerCallbackable.register(this)
	}


	/**
	 * Registers [playerQuitCallback] for the [player].
	 */
	fun registerCallback(player: Player, playerQuitCallback: PlayerQuitMapCallback<V>) {
		quitCallbacks[player] = playerQuitCallback
	}

	/**
	 * Removes registered callback for the [player].
	 *
	 * @return the previous callback associated with [player], or null if there was no callback.
	 */
	fun removeCallback(player: Player): PlayerQuitMapCallback<V>? {
		return quitCallbacks.remove(player)
	}


	/**
	 * Puts a Player to the map with a [value] and registers [quitCallback].
	 *
	 * @return the previous value associated with [key], or null if there was no mapping for it.
	 * @see registerCallback
	 */
	fun put(key: Player, value: V, quitCallback: PlayerQuitMapCallback<V>): V? {
		return put(key, value).also {
			registerCallback(key, quitCallback)
		}
	}

	/**
	 * Puts a Player to the map with a [value] and registers [quitCallback].
	 *
	 * @see	put
	 */
	operator fun set(key: Player, value: V, quitCallback: PlayerQuitMapCallback<V>) {
		put(key, value, quitCallback)
	}


	/**
	 * Removes the player from the map, invoking provided [PlayerQuitMapCallback] and deleting it.
	 *
	 * @return the previous value associated with [player], or null if there was no mapping for it.
	 * @see removeCallback
	 */
	override fun quit(player: Player): V? {
		return if (containsKey(player)) {
			remove(player).also {
				quitCallbacks[player]?.invoke(player, it as V)
					?: defaultQuitCallback?.invoke(player, it as V)

				removeCallback(player)
			}
		} else {
			null
		}
	}

	/**
	 * Clears the map invoking all [PlayerQuitMapCallback]s for the players.
	 *
	 * @see quit
	 */
	override fun quitAll() {
		keys.toList().forEach {
			quit(it)
		}
	}
}