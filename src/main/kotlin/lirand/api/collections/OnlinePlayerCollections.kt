package lirand.api.collections

import lirand.api.extensions.events.listen
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import kotlin.collections.set

typealias PlayerQuitCollectionCallback = Player.() -> Unit
typealias PlayerQuitMapCallback<V> = Player.(V) -> Unit

// List

fun Plugin.onlinePlayerListOf() = OnlinePlayerList(this)

fun Plugin.onlinePlayerListOf(vararg players: Player) = OnlinePlayerList(this).apply { addAll(players) }

fun Plugin.onlinePlayerListOf(vararg elements: Pair<Player, PlayerQuitCollectionCallback>) =
	OnlinePlayerList(this).apply { elements.forEach { (player, whenPlayerQuit) -> add(player, whenPlayerQuit) } }


// Set

fun Plugin.onlinePlayerSetOf() = OnlinePlayerSet(this)

fun Plugin.onlinePlayerSetOf(vararg players: Player) = OnlinePlayerSet(this).apply { addAll(players) }

fun Plugin.onlinePlayerSetOf(vararg pair: Pair<Player, PlayerQuitCollectionCallback>) =
	OnlinePlayerSet(this).apply { pair.forEach { (player, whenPlayerQuit) -> add(player, whenPlayerQuit) } }


// Map

fun <V> Plugin.onlinePlayerMapOf() = OnlinePlayerMap<V>(this)

fun <V> Plugin.onlinePlayerMapOf(vararg elements: Pair<Player, V>) = OnlinePlayerMap<V>(this).apply { putAll(elements) }

fun <V> Plugin.onlinePlayerMapOf(vararg elements: Triple<Player, V, PlayerQuitMapCallback<V>>) =
	OnlinePlayerMap<V>(this).apply {
		elements.forEach { (player, value, whenPlayerQuit) ->
			put(
				player,
				value,
				whenPlayerQuit
			)
		}
	}


class OnlinePlayerList(override val plugin: Plugin) : ArrayList<Player>(), OnlinePlayerCollection {
	private val quitCallbacks: MutableList<Pair<Player, PlayerQuitCollectionCallback>> = mutableListOf()

	override fun add(player: Player, playerQuitCallback: Player.() -> Unit): Boolean {
		return if (super<OnlinePlayerCollection>.add(player, playerQuitCallback)) {
			quitCallbacks.add(player to playerQuitCallback)
			true
		}
		else false
	}

	override fun add(element: Player): Boolean {
		if (isEmpty()) plugin.registerEvents(this)
		return super<ArrayList>.add(element)
	}

	override fun quit(player: Player): Boolean {
		return if (super.quit(player)) {
			val iterator = quitCallbacks.iterator()
			for ((registeredPlayer, callback) in iterator) {
				if (registeredPlayer == player) {
					iterator.remove()
					callback.invoke(registeredPlayer)
				}
			}
			true
		}
		else false
	}

	override fun removeAt(index: Int): Player {
		return super.removeAt(index).also {
			checkRegistration(false)
		}
	}

	override fun remove(element: Player): Boolean {
		return if (super.remove(element)) {
			checkRegistration(false)
			true
		}
		else false
	}
}

class OnlinePlayerSet(override val plugin: Plugin) : HashSet<Player>(), OnlinePlayerCollection {
	private val quitCallbacks: MutableMap<Player, PlayerQuitCollectionCallback> = mutableMapOf()

	override fun add(player: Player, playerQuitCallback: PlayerQuitCollectionCallback): Boolean {
		return if (super<OnlinePlayerCollection>.add(player, playerQuitCallback)) {
			quitCallbacks[player] = playerQuitCallback

			checkRegistration(true)
			true
		}
		else false
	}

	override fun add(element: Player): Boolean {
		return super<HashSet>.add(element).also {
			if (it) checkRegistration(true)
		}
	}

	override fun remove(element: Player): Boolean {
		return super.remove(element).also {
			if (it) checkRegistration(false)
		}
	}

	override fun quit(player: Player): Boolean {
		return if (super.quit(player)) {
			quitCallbacks.remove(player)?.also { block ->
				block.invoke(player)
			}
			true
		}
		else false
	}
}

interface OnlinePlayerCollection : MutableCollection<Player>, Listener {

	val plugin: Plugin

	fun checkRegistration(isAdded: Boolean) {
		if (size == 1 && isAdded) {
			listen<PlayerQuitEvent>(plugin) { quit(player) }
			listen<PlayerKickEvent>(plugin) { quit(player) }
		}
		else if (size == 0 && !isAdded) {
			unregister()
		}
	}

	/**
	 * Adds a new Player to the collection with a callback for when the player quits the server.
	 */
	fun add(player: Player, playerQuitCallback: PlayerQuitCollectionCallback): Boolean {
		return add(player).also {
			if (it) checkRegistration(true)
		}
	}

	/**
	 * Removes the player from the collection, calling the [PlayerQuitCollectionCallback] provided.
	 */
	fun quit(player: Player): Boolean {
		return remove(player).also {
			if (it) checkRegistration(false)
		}
	}

	/**
	 * Clear the collection calling all [PlayerQuitCollectionCallback] from the Players.
	 */
	fun clearQuiting() {
		toMutableList().forEach {
			quit(it)
		}
	}
}

class OnlinePlayerMap<V>(val plugin: Plugin) : HashMap<Player, V>(), Listener {

	private val quitCallbacks: HashMap<Player, PlayerQuitMapCallback<V>> = hashMapOf()

	/**
	 * Puts a Player to the map with a [value] and a callback for when the player quits the server.
	 */
	fun put(key: Player, value: V, playerQuitCallback: PlayerQuitMapCallback<V>): V? {
		quitCallbacks[key] = playerQuitCallback
		return put(key, value).also {
			checkRegistration(true)
		}
	}

	operator fun set(key: Player, value: V, playerQuitCallback: PlayerQuitMapCallback<V>): V? =
		put(key, value, playerQuitCallback)

	/**
	 * Removes the player from the map, calling the [PlayerQuitMapCallback] provided.
	 */
	fun quit(player: Player) {
		remove(player)?.also {
			quitCallbacks.remove(player)?.also { block ->
				block.invoke(player, it)
			}
			checkRegistration(false)
		}
	}

	/**
	 * Clear the map calling all [PlayerQuitMapCallback] from the Players.
	 */
	fun clearQuitting() {
		keys.toMutableList().forEach {
			quit(it)
		}
	}

	override fun remove(key: Player): V? {
		return super.remove(key).also {
			checkRegistration(false)
		}
	}

	override fun remove(key: Player, value: V): Boolean {
		return super.remove(key, value).also {
			checkRegistration(false)
		}
	}

	private fun checkRegistration(isAdded: Boolean) {
		if (isAdded && size == 1) {
			plugin.listen<PlayerQuitEvent> { quit(player) }
			plugin.listen<PlayerKickEvent> { quit(player) }
		}
		else if (!isAdded && size == 0) {
			unregister()
		}
	}
}