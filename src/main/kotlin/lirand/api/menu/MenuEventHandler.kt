package lirand.api.menu

import org.bukkit.plugin.Plugin

interface MenuEventHandler {

	val plugin: Plugin

	fun update(update: PlayerMenuUpdate)

	fun close(close: PlayerMenuClose)

	fun moveToMenu(moveToMenu: PlayerMoveToMenu)

	fun preOpen(preOpen: PlayerMenuPreOpen)

	fun open(open: PlayerMenuOpen)

}