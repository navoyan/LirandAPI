package lirand.api.dsl.menu.dynamic

import lirand.api.dsl.menu.fixed.MenuDSLMarker
import lirand.api.dsl.menu.fixed.StaticMenuDSL
import lirand.api.menu.Menu
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

interface MenuDSL<S : SlotDSL<I>, I : Inventory> : Menu<S, I>, StaticMenuDSL<S, I> {

	@MenuDSLMarker
	fun title(render: (Player?) -> String?)

}