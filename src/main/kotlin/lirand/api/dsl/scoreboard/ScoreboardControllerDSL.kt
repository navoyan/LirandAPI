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
import lirand.api.extensions.server.server
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

@ScoreboardBuilderDSLMarker
class ScoreboardControllerDSL(internal val plugin: Plugin, var title: String) : ScoreboardController {

	private val lineControllers = mutableMapOf<Int, ScoreboardLineController>()

	private val _players = plugin.onlinePlayerMapOf<Objective>()
	override val players: Map<Player, Objective> = _players

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
			if (value > 0 && _players.isNotEmpty())
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
			if (value > 0 && _players.isNotEmpty())
				lineJob = scope.launch {
					while (isActive) {
						delay(value)
						updateLines()
					}
				}
		}

	override fun dispose() {
		titleJob?.cancel()
		lineJob?.cancel()

		for (objective in players.values) {
			objective.unregister()
		}
	}

	fun setLine(line: Int, scoreboardLineController: ScoreboardLineController) {
		if (line in linesBounds)
			lineControllers[line] = scoreboardLineController
	}

	/**
	 * set a [text] to specified [line] (1 to 16) of the scoreboard with a builder.
	 *
	 * In the builder you can use [ScoreboardLineController.onRender] to change the value
	 * when the line renders to the player, or [ScoreboardLineController.onUpdate] when you call [updateLine] or
	 * specify a value greater than 0 to [updateTitleDelay] to update all lines periodic.
	 *
	 * If [line] be greater than 16 or less than 1 the line will be ignored.
	 */
	inline fun line(
		line: Int,
		text: String,
		crossinline builder: ScoreboardLineController.() -> Unit = {}
	) = setLine(line, ScoreboardLineController(this, text).apply(builder))

	/**
	 * Add an array of lines at scoreboard starting at the [startInLine] value with a builder.
	 *
	 * In the builder you can use [ScoreboardLineController.onRender] to change the value
	 * when the line renders to the player, or [ScoreboardLineController.onUpdate] when you call [updateLine] or
	 * specify a value greater than 0 to [updateTitleDelay] to update all lines periodic.
	 */
	inline fun lines(
		vararg lines: String,
		startInLine: Int = 1,
		crossinline builder: ScoreboardLineController.() -> Unit = {}
	) {
		for ((index, line) in lines.withIndex()) {
			line(index + startInLine, line, builder)
		}
	}

	fun removeLine(line: Int): Boolean {
		return lineControllers.remove(line) != null
	}

	fun setTitleController(title: ScoreboardTitleController) = title.also {
		titleController = it
	}

	/**
	 * The DSL block to manage how the title of the scoreboard will be displayed to a specific player.
	 */
	inline fun title(crossinline block: ScoreboardTitleController.() -> Unit) =
		setTitleController(ScoreboardTitleController(this).apply(block))


	override fun showTo(player: Player) {
		val max = lineControllers.keys.maxOrNull() ?: return

		if (_players[player]?.scoreboard != null) return
		val scoreboard = server.scoreboardManager?.newScoreboard ?: return

		val objective = scoreboard.getObjective(DisplaySlot.SIDEBAR)
			?: scoreboard.registerNewObjective(plugin.name, "dummy", title).apply {
				displaySlot = DisplaySlot.SIDEBAR
				displayName = TitleRenderEvent(player, title).also { titleController?.renderEvent?.invoke(it) }.title
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
		_players.put(player, objective) {
			if (_players.isEmpty()) {
				titleJob?.cancel(); titleJob = null
				lineJob?.cancel(); lineJob = null
			}
		}

		if (titleJob == null && updateTitleDelay > 0)
			updateTitleDelay = updateTitleDelay
		if (lineJob == null && updateLinesDelay > 0)
			updateLinesDelay = updateLinesDelay
	}

	private val lineColors = (0..15).map {
		it.toByte().toString(2).take(4).map {
			if (it == '0') ChatColor.RESET.toString()
			else ChatColor.WHITE.toString()
		}.joinToString("")
	}

	private fun getEntryByLine(line: Int) = lineColors[line]

	private inline fun buildLine(objective: Objective, line: Int, lineTextTransformer: (ScoreboardLineController) -> String) {
		val sb = objective.scoreboard ?: return
		val sbLine = lineControllers[line] ?: ScoreboardLineController(this, "")

		val lineEntry = getEntryByLine(line)
		val realScoreLine = 17 - line

		val text = lineTextTransformer(sbLine)

		val team = sb.getTeam("line_$line") ?: sb.registerNewTeam("line_$line")

		if (text.isEmpty()) {
			if (team.prefix.isNotEmpty()) team.prefix = ""
			if (team.suffix.isNotEmpty()) team.suffix = ""
			return
		}

		if (text.length > 16) {
			val fixedText = if (text.length > 32) text.take(32) else text
			val prefix = fixedText.substring(0, 16)
			val suffix = fixedText.substring(16, fixedText.length - 1)
			if (team.prefix != prefix || team.suffix != suffix) {
				team.prefix = prefix
				team.suffix = suffix
			}
		}
		else {
			if (team.prefix != text) {
				team.prefix = text
				team.suffix = ""
			}
		}

		if (!team.hasEntry(lineEntry)) team.addEntry(lineEntry)

		val score = objective.getScore(lineEntry)

		if (!(score.isScoreSet && score.score == realScoreLine)) {
			score.score = realScoreLine
		}
	}

	override fun updateTitle() {
		for ((player, objective) in _players) {
			val titleUpdateEvent = TitleUpdateEvent(player, title).also { titleController?.updateEvent?.invoke(it) }
			objective.displayName = titleUpdateEvent.title
		}
	}

	override fun updateLine(line: Int): Boolean {
		if (lineControllers[line] == null) return false
		for ((player, objective) in _players) {
			buildLine(objective, line) { scoreboardLine ->
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
}