package lirand.api.dsl.menu.exposed.fixed

import org.bukkit.entity.Player
import java.util.WeakHashMap

class MenuPlayerDataMap : WeakHashMap<Player, MenuTypedDataMap>() {
	override fun get(key: Player): MenuTypedDataMap {
		return super.get(key) ?: MenuTypedDataMap().also {
			put(key, it)
		}
	}
}