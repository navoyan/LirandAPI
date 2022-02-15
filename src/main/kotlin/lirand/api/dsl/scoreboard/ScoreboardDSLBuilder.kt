package lirand.api.dsl.scoreboard

import com.github.shynixn.mccoroutine.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import lirand.api.collections.onlinePlayerMapOf
import lirand.api.extensions.server.server
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

class ScoreboardDSLBuilder(internal val plugin: Plugin, var title: String) : ScoreboardBuilder {

	private val lines = mutableMapOf<Int, ScoreboardLine>()

	private val _players = plugin.onlinePlayerMapOf<Objective>()
	override val players: Map<Player, Objective> = _players

	private var titleController: ScoreboardTitle? = null

	private var titleJob: Job? = null
	private var lineJob: Job? = null

	var updateTitleDelay: Long = 0
		set(value) {
			field = value
			titleJob?.cancel()
			titleJob = null
			if (value > 0 && _players.isNotEmpty())
				titleJob = plugin.launch {
					while (isActive) {
						updateTitle()
						delay(value)
					}
				}
		}

	var updateLinesDelay: Long = 0
		set(value) {
			field = value
			lineJob?.cancel()
			lineJob = null
			if (value > 0 && _players.isNotEmpty())
				lineJob = plugin.launch {
					while (isActive) {
						updateLines()
						delay(value)
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

	fun setLine(line: Int, scoreboardLine: ScoreboardLine) {
		if (line in linesBounds)
			lines[line] = scoreboardLine
	}

	/**
	 * set a [text] to specified [line] (1 to 16) of the scoreboard with a builder.
	 *
	 * In the builder you can use [ScoreboardLine.onRender] to change the value
	 * when the line renders to the player, or [ScoreboardLine.onUpdate] when you call [updateLine] or
	 * specify a value greater than 0 to [updateTitleDelay] to update all lines periodic.
	 *
	 * If [line] be greater than 16 or less than 1 the line will be ignored.
	 */
	@ScoreboardBuilderDSLMarker
	inline fun line(
		line: Int,
		text: String,
		crossinline builder: ScoreboardLine.() -> Unit = {}
	) = setLine(line, ScoreboardLine(this, text).apply(builder))

	/**
	 * Add an array of lines at scoreboard starting at the [startInLine] value with a builder.
	 *
	 * In the builder you can use [ScoreboardLine.onRender] to change the value
	 * when the line renders to the player, or [ScoreboardLine.onUpdate] when you call [updateLine] or
	 * specify a value greater than 0 to [updateTitleDelay] to update all lines periodic.
	 */
	@ScoreboardBuilderDSLMarker
	inline fun lines(
		vararg lines: String,
		startInLine: Int = 1,
		crossinline builder: ScoreboardLine.() -> Unit = {}
	) {
		for ((index, line) in lines.withIndex()) {
			line(index + startInLine, line, builder)
		}
	}

	fun remove(line: Int): Boolean {
		return lines.remove(line) != null
	}

	fun titleController(title: ScoreboardTitle) = title.also {
		titleController = it
	}

	/**
	 * The DSL block to manage how the title of the scoreboard will be displayed to a specific player.
	 */
	@ScoreboardBuilderDSLMarker
	inline fun title(crossinline block: ScoreboardTitle.() -> Unit) =
		titleController(ScoreboardTitle(this).apply(block))


	override fun show(player: Player) {
		val max = lines.keys.maxOrNull() ?: return

		if (_players[player]?.scoreboard != null) return
		val scoreboard = server.scoreboardManager?.newScoreboard ?: return

		val objective = scoreboard.getObjective(DisplaySlot.SIDEBAR)
			?: scoreboard.registerNewObjective(plugin.name, "dummy", title).apply {
				displaySlot = DisplaySlot.SIDEBAR
				displayName = TitleRenderEvent(player, title).also { titleController?.renderEvent?.invoke(it) }.newTitle
			}

		for (i in 1..max) {
			lineBuild(objective, i) { line ->
				if (line.renderEvent != null) {
					LineRenderEvent(player, line.text).also { line.renderEvent?.invoke(it) }.newText
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

	private fun entryByLine(line: Int) = lineColors[line]

	private inline fun lineBuild(objective: Objective, line: Int, lineTextTransformer: (ScoreboardLine) -> String) {
		val sb = objective.scoreboard ?: return
		val sbLine = lines[line] ?: ScoreboardLine(this, "")

		val lineEntry = entryByLine(line)
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
			objective.displayName = titleUpdateEvent.newTitle
		}
	}

	override fun updateLine(line: Int): Boolean {
		if (lines[line] == null) return false
		for ((player, objective) in _players) {
			lineBuild(objective, line) { sbLine ->
				if (sbLine.updateEvent != null) {
					LineUpdateEvent(player, sbLine.text).also { sbLine.updateEvent?.invoke(it) }.newText
				}
				else sbLine.text
			}
		}
		return true
	}

	override fun updateLines() {
		val max = lines.keys.maxOrNull() ?: return

		for (i in 1..max) {
			updateLine(i)
		}
	}
}