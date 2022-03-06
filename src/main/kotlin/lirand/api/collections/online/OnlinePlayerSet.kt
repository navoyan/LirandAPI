package lirand.api.collections.online

import lirand.api.utilities.ifTrue
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import kotlin.collections.HashSet

/**
 * Returns an empty new [OnlinePlayerSet].
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerSet.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun Plugin.onlinePlayerSetOf(
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitCollectionCallback? = null
) = OnlinePlayerSet(this, defaultQuitCallback, quitAllOnPluginDisable)

/**
 * Returns a new [OnlinePlayerSet] with specified players.
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerSet.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun Plugin.onlinePlayerSetOf(
	vararg elements: Player,
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitCollectionCallback? = null
) = OnlinePlayerSet(this, defaultQuitCallback, quitAllOnPluginDisable)
	.apply { addAll(elements) }

/**
 * Returns a new [OnlinePlayerSet] with specified elements given as **player - quit callback** pairs.
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerSet.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun Plugin.onlinePlayerSetOf(
	vararg elements: Pair<Player, PlayerQuitCollectionCallback>,
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitCollectionCallback? = null
) = OnlinePlayerSet(this, defaultQuitCallback, quitAllOnPluginDisable)
	.apply {
		elements.forEach { (player, playerQuitCallback) -> add(player, playerQuitCallback) }
	}



/**
 * A [HashSet] whose players (elements) are removed automatically
 * on their exit or plugin disable invokin provided [PlayerQuitCollectionCallback].
 */
class OnlinePlayerSet(
	override val plugin: Plugin,
	/**
	 * Callback that is invoked when no other callback is provided for the player.
	 */
	override val defaultQuitCallback: PlayerQuitCollectionCallback? = null,
	/**
	 * Determines whether call [quitAll] or not when plugin gets disabled.
	 */
	override val quitAllOnPluginDisable: Boolean = false
) : HashSet<Player>(), OnlinePlayerCollection {

	private val quitCallbacks = hashMapOf<Player, PlayerQuitCollectionCallback>()


	init {
		OnlinePlayerCallbackable.register(this)
	}


	/**
	 * Registers [playerQuitCallback] for the [player].
	 */
	override fun registerCallback(player: Player, playerQuitCallback: PlayerQuitCollectionCallback) {
		quitCallbacks[player] = playerQuitCallback
	}

	/**
	 * Removes registered callback for the [player].
	 *
	 * @return the previous callback associated with [player], or null if there was no callback.
	 */
	override fun removeCallback(player: Player): PlayerQuitCollectionCallback? {
		return quitCallbacks.remove(player)
	}


	/**
	 * Adds a new Player to the set and registers [playerQuitCallback].
	 *
	 * @return true if this set did not already contain the [element].
	 * @see registerCallback
	 */
	override fun add(element: Player, playerQuitCallback: PlayerQuitCollectionCallback): Boolean {
		return add(element).also {
			registerCallback(element, playerQuitCallback)
		}
	}


	/**
	 * Removes the player from the set invoking provided [PlayerQuitCollectionCallback] and deleting it.
	 *
	 * @return true if the set changed as a result of the call.
	 * @see removeCallback
	 */
	override fun quit(player: Player): Boolean {
		return super.remove(player).ifTrue {
			quitCallbacks[player]?.invoke(player)
				?: defaultQuitCallback?.invoke(player)

			removeCallback(player)
		}
	}


	/**
	 * Clears the set invoking all [PlayerQuitCollectionCallback]s for the players.
	 */
	override fun quitAll() {
		toList().forEach {
			quit(it)
		}
	}
}