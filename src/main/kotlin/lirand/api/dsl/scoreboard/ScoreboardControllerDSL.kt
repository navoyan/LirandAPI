package lirand.api.dsl.scoreboard

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lirand.api.collections.onlinePlayerMapOf
import lirand.api.dsl.scoreboard.ScoreboardController.Companion.linesBounds
import lirand.api.extensions.server.server
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

@ScoreboardBuilderDSLMarker
class ScoreboardControllerDSL(internal val plugin: Plugin, var title: String) : ScoreboardController {

	private companion object {
		val uniqueLineEntries = (0..15).map {
			it.toByte().toString(2).take(4).map {
				if (it == '0') ChatColor.RESET.toString()
				else ChatColor.WHITE.toString()
			}.joinToString("")
		}
	}


	private val lineControllers = mutableMapOf<Int, ScoreboardLineController>()

	private val _viewers = plugin.onlinePlayerMapOf<Objective>()
	override val viewers: Map<Player, Objective> = _viewers

	private var titleController: ScoreboardTitleController? = null

	private val scope = CoroutineScope(
		plugin.minecraftDispatcher + SupervisorJob() +
				CoroutineExceptionHandler { _, exception -> exception.printStackTrace() }
	)
	private var titleJob: Job? = null
	private var lineJob: Job? = null

	var updateTitleDelay: Long = 0
		set(value) {
			field = value
			titleJob?.cancel()
			titleJob = null
			if (value > 0 && _viewers.isNotEmpty())
				titleJob = scope.launch {
					while (isActive) {
						delay(value)
						updateTitle()
					}
				}
		}

	var updateLinesDelay: Long = 0
		set(value) {
			field = value
			lineJob?.cancel()
			lineJob = null
			if (value > 0 && _viewers.isNotEmpty())
				lineJob = scope.launch {
					while (isActive) {
						delay(value)
						updateLines()
					}
				}
		}


	/**
	 * The DSL block to manage how the title of the scoreboard will be displayed to a specific player.
	 */
	inline fun title(crossinline builder: ScoreboardTitleController.() -> Unit) =
		setTitleController(ScoreboardTitleController().apply(builder))

	/**
	 * set a [text] to specified [index] (1 to 16) of the scoreboard with a builder.
	 *
	 * In the builder you can use [ScoreboardLineController.onRender] to change the value
	 * when the line renders to the player, or [ScoreboardLineController.onUpdate] when you call [updateLine] or
	 * specify a value greater than 0 to [updateTitleDelay] to update all lines periodic.
	 *
	 * If [index] be greater than 16 or less than 1 the line will be ignored.
	 */
	inline fun line(
		index: Int,
		text: String,
		crossinline builder: ScoreboardLineController.() -> Unit = {}
	) = setLineController(index, ScoreboardLineController(text).apply(builder))

	/**
	 * Add an array of lines at scoreboard starting at the [startIndex] value with a builder.
	 *
	 * In the builder you can use [ScoreboardLineController.onRender] to change the value
	 * when the line renders to the player, or [ScoreboardLineController.onUpdate] when you call [updateLine] or
	 * specify a value greater than 0 to [updateTitleDelay] to update all lines periodic.
	 */
	inline fun lines(
		startIndex: Int = 1,
		vararg lines: String,
		crossinline builder: ScoreboardLineController.() -> Unit = {}
	) {
		for ((index, line) in lines.withIndex()) {
			line(index + startIndex, line, builder)
		}
	}


	override fun showTo(player: Player) {
		val max = lineControllers.keys.maxOrNull() ?: return

		if (_viewers[player]?.scoreboard != null) return
		val scoreboard = server.scoreboardManager?.newScoreboard ?: return

		val objective = scoreboard.getObjective(DisplaySlot.SIDEBAR)
			?: scoreboard.registerNewObjective(plugin.name, "dummy", title).apply {
				displaySlot = DisplaySlot.SIDEBAR
				displayName = TitleRenderEvent(player, title)
					.also { titleController?.renderEvent?.invoke(it) }.title
			}

		for (i in 1..max) {
			buildLine(objective, i) { line ->
				if (line.renderEvent != null) {
					LineRenderEvent(player, line.text).also { line.renderEvent?.invoke(it) }.text
				}
				else line.text
			}
		}

		player.scoreboard = scoreboard
		_viewers.put(player, objective) {
			if (_viewers.isEmpty()) {
				titleJob?.cancel()
				titleJob = null
				lineJob?.cancel()
				lineJob = null
			}
		}

		if (titleJob == null && updateTitleDelay > 0)
			updateTitleDelay = updateTitleDelay
		if (lineJob == null && updateLinesDelay > 0)
			updateLinesDelay = updateLinesDelay
	}

	override fun dispose() {
		titleJob?.cancel()
		lineJob?.cancel()

		for (objective in viewers.values) {
			objective.unregister()
		}
	}


	override fun updateTitle() {
		for ((player, objective) in _viewers) {
			val titleUpdateEvent = TitleUpdateEvent(player, title).also { titleController?.updateEvent?.invoke(it) }
			objective.displayName = titleUpdateEvent.title
		}
	}

	override fun updateLine(index: Int): Boolean {
		if (lineControllers[index] == null) return false
		for ((player, objective) in _viewers) {
			buildLine(objective, index) { scoreboardLine ->
				if (scoreboardLine.updateEvent != null) {
					LineUpdateEvent(player, scoreboardLine.text).also { scoreboardLine.updateEvent?.invoke(it) }.text
				}
				else scoreboardLine.text
			}
		}
		return true
	}

	override fun updateLines() {
		val max = lineControllers.keys.maxOrNull() ?: return

		for (i in 1..max) {
			updateLine(i)
		}
	}


	fun setTitleController(index: ScoreboardTitleController) = index.also {
		titleController = it
	}

	fun setLineController(index: Int, scoreboardLineController: ScoreboardLineController) {
		if (index in linesBounds)
			lineControllers[index] = scoreboardLineController
	}

	fun removeLineController(index: Int): Boolean {
		return lineControllers.remove(index) != null
	}



	private inline fun buildLine(
		objective: Objective,
		index: Int,
		lineTextTransformer: (ScoreboardLineController) -> String
	) {
		val scoreboard = objective.scoreboard ?: return
		val scoreboardLine = lineControllers[index] ?: ScoreboardLineController("")
		val lineEntry = uniqueLineEntries[index - 1]
		val realLineScore = (lineControllers.size) - index

		val text = lineTextTransformer(scoreboardLine)

		val team = scoreboard.getTeam("line_$index") ?: scoreboard.registerNewTeam("line_$index")

		team.prefix = text

		if (!team.hasEntry(lineEntry)) team.addEntry(lineEntry)

		val score = objective.getScore(lineEntry)

		if (!score.isScoreSet || score.score != realLineScore) {
			score.score = realLineScore
		}
	}
}