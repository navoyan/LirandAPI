package lirand.api.menu

import org.bukkit.inventory.Inventory
import org.bukkit.plugin.Plugin

interface MenuEventHandler<I : Inventory> {

	val plugin: Plugin

	fun update(update: PlayerMenuUpdate<I>)

	fun close(close: PlayerMenuClose)

	fun moveToMenu(moveToMenu: PlayerMoveToMenu<I>)

	fun preOpen(preOpen: PlayerMenuPreOpen)

	fun open(open: PlayerMenuOpen<I>)

}