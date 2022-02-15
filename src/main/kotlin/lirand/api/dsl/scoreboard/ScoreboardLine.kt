package lirand.api.dsl.scoreboard

import org.bukkit.entity.Player

typealias LineRenderCallback = LineRenderEvent.() -> Unit
typealias LineUpdateCallback = LineUpdateEvent.() -> Unit

@ScoreboardBuilderDSLMarker
class ScoreboardLine(private val scoreboard: ScoreboardDSLBuilder, text: String) {
	var text: String = text
		internal set

	internal var renderEvent: LineRenderCallback? = null
	internal var updateEvent: LineUpdateCallback? = null

	@ScoreboardBuilderDSLMarker
	fun onRender(renderCallback: LineRenderCallback) {
		renderEvent = renderCallback
	}

	@ScoreboardBuilderDSLMarker
	fun onUpdate(updateCallback: LineUpdateCallback) {
		updateEvent = updateCallback
	}
}

interface ChangeableLineEvent {
	var newText: String
}

class LineRenderEvent(
	override val player: Player,
	override var newText: String
) : PlayerScoreboardComponent, ChangeableLineEvent

class LineUpdateEvent(
	override val player: Player,
	override var newText: String
) : PlayerScoreboardComponent, ChangeableLineEvent