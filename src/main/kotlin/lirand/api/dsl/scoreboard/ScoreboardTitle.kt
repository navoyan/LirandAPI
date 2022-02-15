package lirand.api.dsl.scoreboard

import org.bukkit.entity.Player

typealias TitleRenderCallback = TitleRenderEvent.() -> Unit
typealias TitleUpdateCallback = TitleUpdateEvent.() -> Unit

@ScoreboardBuilderDSLMarker
class ScoreboardTitle(private val scoreboard: ScoreboardDSLBuilder) {
	internal var renderEvent: TitleRenderCallback? = null
	internal var updateEvent: TitleUpdateCallback? = null

	fun onRender(renderCallback: TitleRenderCallback) {
		renderEvent = renderCallback
	}

	fun onUpdate(updateCallback: TitleUpdateCallback) {
		updateEvent = updateCallback
	}
}

interface ChangeableTitleEvent {
	var newTitle: String
}

class TitleRenderEvent(
	override val player: Player,
	override var newTitle: String
) : PlayerScoreboardComponent, ChangeableTitleEvent

class TitleUpdateEvent(
	override val player: Player,
	override var newTitle: String
) : PlayerScoreboardComponent, ChangeableTitleEvent