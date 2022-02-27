package lirand.api.dsl.scoreboard

import lirand.api.extensions.chat.translatedColor
import org.bukkit.entity.Player

typealias LineRenderCallback = LineRenderEvent.() -> Unit
typealias LineUpdateCallback = LineUpdateEvent.() -> Unit

@ScoreboardBuilderDSLMarker
class ScoreboardLineController(text: String) {
	var text: String by translatedColor(text)
		internal set

	internal var renderEvent: LineRenderCallback? = null
	internal var updateEvent: LineUpdateCallback? = null

	fun onRender(renderCallback: LineRenderCallback) {
		renderEvent = renderCallback
	}

	fun onUpdate(updateCallback: LineUpdateCallback) {
		updateEvent = updateCallback
	}
}

interface LineChangeEvent {
	val player: Player
	var text: String
}

class LineRenderEvent(
	override val player: Player,
	override var text: String
) : LineChangeEvent

class LineUpdateEvent(
	override val player: Player,
	override var text: String
) : LineChangeEvent