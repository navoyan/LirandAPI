package lirand.api.collections.online

import org.bukkit.entity.Player

typealias PlayerQuitCollectionCallback = (Player) -> Unit

/**
 * A [MutableCollection] whose players (elements) are removed automatically
 * on their exit or plugin disable invoking provided [PlayerQuitCollectionCallback].
 */
interface OnlinePlayerCollection : MutableCollection<Player>, OnlinePlayerCallbackable<Boolean> {

	/**
	 * Callback that is invoked when no other callback is provided for the player.
	 */
	val defaultQuitCallback: PlayerQuitCollectionCallback?


	/**
	 * Registers provided [playerQuitCallback] for the [player].
	 */
	fun registerCallback(player: Player, playerQuitCallback: PlayerQuitCollectionCallback)

	/**
	 * Removes registered callback for the [player].
	 *
	 * @return the previous callback associated with [player], or null if there was no callback.
	 */
	fun removeCallback(player: Player): PlayerQuitCollectionCallback?


	/**
	 * Adds a new Player to the collection and registers [playerQuitCallback].
	 *
	 * @see registerCallback
	 */
	fun add(element: Player, playerQuitCallback: PlayerQuitCollectionCallback): Boolean

}
