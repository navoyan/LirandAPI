package lirand.api.collections.online

import lirand.api.utilities.ifTrue
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Returns an empty new [OnlinePlayerList].
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerList.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun Plugin.onlinePlayerListOf(
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitCollectionCallback? = null
) = OnlinePlayerList(this, defaultQuitCallback, quitAllOnPluginDisable)

/**
 * Returns a new [OnlinePlayerList] with specified players.
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerList.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun Plugin.onlinePlayerListOf(
	vararg elements: Player,
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitCollectionCallback? = null
) = OnlinePlayerList(this, defaultQuitCallback, quitAllOnPluginDisable)
	.apply { addAll(elements) }

/**
 * Returns a new [OnlinePlayerList] with specified elements given as **player - quit callback** pairs.
 *
 * @param quitAllOnPluginDisable determines whether call [OnlinePlayerList.quitAll] or not when plugin gets disabled.
 * @param defaultQuitCallback callback that is invoked when no other callback is provided for the player.
 */
fun Plugin.onlinePlayerListOf(
	vararg elements: Pair<Player, PlayerQuitCollectionCallback>,
	quitAllOnPluginDisable: Boolean = false,
	defaultQuitCallback: PlayerQuitCollectionCallback? = null
) = OnlinePlayerList(this, defaultQuitCallback, quitAllOnPluginDisable)
	.apply {
		elements.forEach { (player, playerQuitCallback) -> add(player, playerQuitCallback) }
	}



/**
 * An [ArrayList] whose players (elements) are removed automatically
 * on their exit or plugin disable invoking provided [PlayerQuitCollectionCallback].
 */
class OnlinePlayerList(
	override val plugin: Plugin,
	/**
	 * Callback that is invoked when no other callback is provided for the player.
	 */
	override val defaultQuitCallback: PlayerQuitCollectionCallback? = null,
	/**
	 * Determines whether call [quitAll] or not when plugin gets disabled.
	 */
	override val quitAllOnPluginDisable: Boolean = false
) : ArrayList<Player>(), OnlinePlayerCollection {

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
	 * Adds a new Player to the list and registers [playerQuitCallback].
	 *
	 * @return true
	 * @see registerCallback
	 */
	override fun add(element: Player, playerQuitCallback: PlayerQuitCollectionCallback): Boolean {
		return add(element).also {
			registerCallback(element, playerQuitCallback)
		}
	}


	/**
	 * Replaces the element at the specified position in this list with the specified element
	 * and registers [playerQuitCallback].
	 *
	 * @return the player previously at the specified position.
	 * @see registerCallback
	 */
	operator fun set(index: Int, player: Player, playerQuitCallback: PlayerQuitCollectionCallback): Player {
		return set(index, player).also {
			registerCallback(player, playerQuitCallback)
		}
	}


	/**
	 * Removes the player from the list invoking provided [PlayerQuitCollectionCallback] and deleting it.
	 *
	 * @return true if the list changed as a result of the call.
	 * @see removeCallback
	 */
	override fun quit(player: Player): Boolean {
		return super.removeAll(setOf(player)).ifTrue {
			quitCallbacks[player]?.invoke(player)
				?: defaultQuitCallback?.invoke(player)

			quitCallbacks.remove(player)
		}
	}


	/**
	 * Clears the list invoking all [PlayerQuitCollectionCallback]s for the players.
	 */
	override fun quitAll() {
		toSet().forEach {
			quit(it)
		}
	}

}