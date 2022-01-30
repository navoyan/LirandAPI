package lirand.api.dsl.scoreboard

import org.bukkit.entity.Player

typealias TitleRenderEvent = TitleRender.() -> Unit
typealias TitleUpdateEvent = TitleUpdate.() -> Unit

@ScoreboardBuilderDSLMarker
class ScoreboardTitle(private val scoreboard: ScoreboardDSLBuilder) {
	internal var renderEvent: TitleRenderEvent? = null
	internal var updateEvent: TitleUpdateEvent? = null

	@ScoreboardBuilderDSLMarker
	fun onRender(block: TitleRenderEvent) {
		renderEvent = block
	}

	@ScoreboardBuilderDSLMarker
	fun onUpdate(block: TitleUpdateEvent) {
		updateEvent = block
	}
}

interface ChangeableTitle {
	var newTitle: String
}

class TitleRender(override val player: Player, override var newTitle: String) : PlayerScoreboardComponent, ChangeableTitle
class TitleUpdate(override val player: Player, override var newTitle: String) : PlayerScoreboardComponent, ChangeableTitle