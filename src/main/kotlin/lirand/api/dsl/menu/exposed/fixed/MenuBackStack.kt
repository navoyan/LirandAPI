package lirand.api.dsl.menu.exposed.fixed

import org.bukkit.entity.Player
import java.util.*

class MenuBackStack : ArrayDeque<MenuBackStackFrame>() {
	var lastBacked = false
	private var nextKey: String? = null

	fun nextKeyed(key: String): MenuBackStack {
		nextKey = key
		return this
	}

	override fun addFirst(e: MenuBackStackFrame) {
		if (nextKey != null) {
			e.key = nextKey
			nextKey = null
		}

		super.addFirst(e)
	}
}

data class MenuBackStackFrame(
	val menu: StaticMenu<*, *>,
	val player: Player,
	val playerData: MenuTypedDataMap,
	var key: String? = null
)