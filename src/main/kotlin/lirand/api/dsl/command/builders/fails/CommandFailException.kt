package lirand.api.dsl.command.builders.fails

import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.CommandSender

class CommandFailException(
	val failMessage: BaseComponent? = null,
	val source: CommandSender
) : RuntimeException() {

	override val message: String?
		get() = failMessage?.toLegacyText()

}