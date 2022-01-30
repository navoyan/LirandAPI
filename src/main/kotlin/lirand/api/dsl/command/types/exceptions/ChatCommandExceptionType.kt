package lirand.api.dsl.command.types.exceptions

import com.mojang.brigadier.ImmutableStringReader
import com.mojang.brigadier.exceptions.CommandExceptionType
import net.md_5.bungee.api.chat.BaseComponent

class ChatCommandExceptionType(
	val messageBuilder: (Array<out Any>) -> BaseComponent
) : CommandExceptionType {

	constructor(message: BaseComponent) : this({ message })

	fun create(vararg args: Any): ChatCommandSyntaxException {
		return ChatCommandSyntaxException(this, messageBuilder(args))
	}

	fun createWithContext(
		reader: ImmutableStringReader,
		vararg args: Any
	): ChatCommandSyntaxException {
		return ChatCommandSyntaxException(this, messageBuilder(args), reader.string, reader.cursor)
	}
}