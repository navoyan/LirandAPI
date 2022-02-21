package lirand.api.dsl.scoreboard

import org.bukkit.entity.Player

typealias TitleRenderCallback = TitleRenderEvent.() -> Unit
typealias TitleUpdateCallback = TitleUpdateEvent.() -> Unit

@ScoreboardBuilderDSLMarker
class ScoreboardTitleController(private val scoreboard: ScoreboardControllerDSL) {
	internal var renderEvent: TitleRenderCallback? = null
	internal var updateEvent: TitleUpdateCallback? = null

	fun onRender(renderCallback: TitleRenderCallback) {
		renderEvent = renderCallback
	}

	fun onUpdate(updateCallback: TitleUpdateCallback) {
		updateEvent = updateCallback
	}
}

interface TitleChangeEvent {
	val player: Player
	var title: String
}

class TitleRenderEvent(
	override val player: Player,
	override var title: String
) : TitleChangeEvent

class TitleUpdateEvent(
	override val player: Player,
	override var title: String
) : TitleChangeEvent