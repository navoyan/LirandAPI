package lirand.api.dsl.command.builders.fails

import net.md_5.bungee.api.chat.BaseComponent

class CommandFailException(
	val failMessage: BaseComponent? = null
) : RuntimeException() {

	override val message: String?
		get() = failMessage?.toLegacyText()

}