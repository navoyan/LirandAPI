package lirand.api.collections.online

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * An entity that has some kind of callbacks that are executed on player exit or plugin disable.
 * Must be explicitly registered to use the internal [OnlineCollectionController] listener.
 *
 * @param V return type of [quit].
 */
interface OnlinePlayerCallbackable<V> {

	companion object {
		/**
		 * Registers [callbackable] in [OnlineCollectionController]
		 * to call [quit] on player exit or [quitAll] on plugin disable.
		 */
		fun register(callbackable: OnlinePlayerCallbackable<*>) {
			val plugin = callbackable.plugin

			provideOnlineCollectionController(plugin)?.register(callbackable)
				?: throw IllegalStateException("Api for this plugin is not initialized.")
		}
	}


	val plugin: Plugin

	/**
	 * Determines whether call [quitAll] or not when plugin gets disabled.
	 */
	val quitAllOnPluginDisable: Boolean


	/**
	 * Removes the player from the callbackable invoking the quit callback provided.
	 *
	 * @return the value callbackable provides after calling this.
	 */
	fun quit(player: Player): V

	/**
	 * Clears the callbackable invoking all provided callbacks.
	 */
	fun quitAll()

}