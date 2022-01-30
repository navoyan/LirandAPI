package lirand.api.dsl.scoreboard

import org.bukkit.entity.Player

typealias LineRenderEvent = LineRender.() -> Unit
typealias LineUpdateEvent = LineUpdate.() -> Unit

@ScoreboardBuilderDSLMarker
class ScoreboardLine(private val scoreboard: ScoreboardDSLBuilder, text: String) {
	var text: String = text
		internal set

	internal var renderEvent: LineRenderEvent? = null
	internal var updateEvent: LineUpdateEvent? = null

	@ScoreboardBuilderDSLMarker
	fun onRender(block: LineRenderEvent) {
		renderEvent = block
	}

	@ScoreboardBuilderDSLMarker
	fun onUpdate(block: LineUpdateEvent) {
		updateEvent = block
	}
}

interface ChangeableLine {
	var newText: String
}

class LineRender(override val player: Player, override var newText: String) : PlayerScoreboardComponent, ChangeableLine
class LineUpdate(override val player: Player, override var newText: String) : PlayerScoreboardComponent, ChangeableLine