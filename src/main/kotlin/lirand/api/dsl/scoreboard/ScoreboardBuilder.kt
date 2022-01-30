package lirand.api.dsl.scoreboard

import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Objective

@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class ScoreboardBuilderDSLMarker


/**
 *
 * onRender events: Called when show/set the scoreboard to a player
 * onUpdate events: Called when updateDelay trigger or force update by using [ScoreboardBuilder.updateTitle],
 * [ScoreboardBuilder.updateLine], [ScoreboardBuilder.updateLines].
 */
@ScoreboardBuilderDSLMarker
inline fun Plugin.scoreboard(
	title: String,
	block: ScoreboardDSLBuilder.() -> Unit
) = ScoreboardDSLBuilder(this, title).apply(block)

val linesBounds = 1..16

@ScoreboardBuilderDSLMarker
interface ScoreboardBuilder {

	val players: Map<Player, Objective>

	/**
	 * Show/set the built scoreboard to a [player]
	 */
	fun show(player: Player)

	/**
	 * Update the title to all players with the scoreboard set see [show])
	 */
	fun updateTitle()

	/**
	 * Update a specific line to all players with the scoreboard set see [show])
	 *
	 * Returns false if the line doesn't exist, true if the line was founded and update.
	 */
	fun updateLine(line: Int): Boolean

	/**
	 * Update all lines to all players with the scoreboard set (see [show])
	 */
	fun updateLines()

	/**
	 * Remove all scoreboard of the players and cancel all internal tasks
	 */
	fun dispose()
}

interface PlayerScoreboardComponent {
	val player: Player
}