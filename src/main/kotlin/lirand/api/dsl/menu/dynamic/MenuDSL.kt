package lirand.api.dsl.menu.dynamic

import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.dsl.menu.fixed.StaticMenuDSL
import lirand.api.menu.Menu
import org.bukkit.entity.Player

interface MenuDSL<S : SlotDSL> : Menu<S>, StaticMenuDSL<S> {

	@MenuDSLMarker
	fun title(render: (Player?) -> String?)

}