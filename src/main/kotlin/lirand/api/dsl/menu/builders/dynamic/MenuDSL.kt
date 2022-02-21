package lirand.api.dsl.menu.builders.dynamic

import lirand.api.dsl.menu.builders.fixed.StaticMenuDSL
import lirand.api.dsl.menu.exposed.dynamic.Menu
import lirand.api.dsl.menu.exposed.dynamic.Slot
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

interface MenuDSL<S : Slot<I>, I : Inventory> : Menu<S, I>, StaticMenuDSL<S, I> {

	fun title(render: (Player?) -> String?)

}