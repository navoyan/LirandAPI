package lirand.api.dsl.menu.exposed.fixed

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

data class MenuView<out I : Inventory>(
	val menu: StaticMenu<*, *>,
	val player: Player,
	val inventory: I,
	val backStack: MenuBackStack? = null
)